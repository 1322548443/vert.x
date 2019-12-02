/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.core.http.impl;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http2.EmptyHttp2Headers;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2Stream;
import io.netty.util.concurrent.FutureListener;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.FileSystem;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpFrame;
import io.vertx.core.http.StreamPriority;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.streams.impl.InboundBuffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
abstract class VertxHttp2Stream<C extends Http2ConnectionBase> {

  private static final MultiMap EMPTY = new Http2HeadersAdaptor(EmptyHttp2Headers.INSTANCE);

  protected final C conn;
  protected final VertxInternal vertx;
  protected final ContextInternal context;

  protected Http2Stream stream;

  private final InboundBuffer<Object> pending;
  private MultiMap trailers;
  private boolean writable; // SHOULD BE CLEAR ABOUT VISIBILITY : currently it's modified by connection and read by stream
  private StreamPriority priority;
  private long bytesRead;
  private long bytesWritten;

  VertxHttp2Stream(C conn, ContextInternal context) {
    this.conn = conn;
    this.vertx = conn.vertx();
    this.context = context;
    this.pending = new InboundBuffer<>(context, 5);
    this.priority = HttpUtils.DEFAULT_STREAM_PRIORITY;

    pending.handler(buff -> {
      if (buff == InboundBuffer.END_SENTINEL) {
        conn.reportBytesRead(bytesRead);
        handleEnd(trailers);
      } else {
        Buffer data = (Buffer) buff;
        int len = data.length();
        conn.getContext().dispatch(null, v -> conn.consumeCredits(this.stream, len));
        bytesRead += len;
        handleData(data);
      }
    });
    pending.exceptionHandler(context::reportException);

    pending.resume();
  }

  void init(Http2Stream stream) {
    this.stream = stream;
    this.writable = this.conn.handler.encoder().flowController().isWritable(stream);
    this.conn.streams.put(stream.id(), this);
  }

  void onClose() {
    conn.reportBytesWritten(bytesWritten);
    context.dispatch(null, v -> this.handleClose());
  }

  void onError(Throwable cause) {
    context.dispatch(cause, this::handleException);
  }

  void onReset(long code) {
    context.dispatch(code, this::handleReset);
  }

  void onPriorityChange(StreamPriority priority) {
    if (this.priority != null && !this.priority.equals(priority)) {
      this.priority = priority;
      context.dispatch(priority, this::handlePriorityChange);
    }
  }

  void onCustomFrame(HttpFrame frame) {
    context.dispatch(frame, this::handleCustomFrame);
  }

  void onData(Buffer data) {
    context.dispatch(data, pending::write);
  }

  void onWritabilityChanged() {
    synchronized (conn) {
      writable = !writable;
    }
    context.dispatch(null, v -> handleInterestedOpsChanged());
  }

  void onEnd() {
    onEnd(EMPTY);
  }

  void onEnd(MultiMap map) {
    synchronized (conn) {
      trailers = map;
    }
    context.dispatch(InboundBuffer.END_SENTINEL, pending::write);
  }

  public int id() {
    return stream.id();
  }

  long bytesWritten() {
    return bytesWritten;
  }

  long bytesRead() {
    return bytesRead;
  }

  public void doPause() {
    pending.pause();
  }

  public void doFetch(long amount) {
    pending.fetch(amount);
  }

  public boolean isNotWritable() {
    synchronized (conn) {
      return !writable;
    }
  }

  public void writeFrame(int type, int flags, ByteBuf payload) {
    conn.writeFrame(stream, type, flags, payload);
  }

  void writeHeaders(Http2Headers headers, boolean end, Handler<AsyncResult<Void>> handler) {
    FutureListener<Void> promise = handler == null ? null : context.promise(handler);
    conn.writeHeaders(stream, headers, end, priority, promise);
  }

  void flush() {
    conn.flush(stream);
  }

  private void writePriorityFrame(StreamPriority priority) {
    conn.writePriority(stream, priority);
  }

  void writeData(ByteBuf chunk, boolean end) {
    writeData(chunk, end, null);
  }

  void writeData(ByteBuf chunk, boolean end, Handler<AsyncResult<Void>> handler) {
    bytesWritten += chunk.readableBytes();
    FutureListener<Void> promise = handler == null ? null : context.promise(handler);
    conn.writeData(stream, chunk, end, promise);
  }

  void writeReset(long code) {
    conn.writeReset(stream.id(), code);
  }

  void handleInterestedOpsChanged() {
  }

  void handleData(Buffer buf) {
  }

  void handleCustomFrame(HttpFrame frame) {
  }

  void handleEnd(MultiMap trailers) {
  }

  void handleReset(long errorCode) {
  }

  void handleException(Throwable cause) {
  }

  void handleClose() {
  }

  synchronized void priority(StreamPriority streamPriority) {
    this.priority = streamPriority;
  }

  synchronized StreamPriority priority() {
    return priority;
  }

  synchronized void updatePriority(StreamPriority priority) {
    if (!this.priority.equals(priority)) {
      this.priority = priority;
      if (stream != null) {
        writePriorityFrame(priority);
      }
    }
  }

  abstract void handlePriorityChange(StreamPriority streamPriority);

  void resolveFile(String filename, long offset, long length, Handler<AsyncResult<AsyncFile>> resultHandler) {
    File file_ = vertx.resolveFile(filename);
    if (!file_.exists()) {
      resultHandler.handle(Future.failedFuture(new FileNotFoundException()));
      return;
    }
    try(RandomAccessFile raf = new RandomAccessFile(file_, "r")) {
    } catch (IOException e) {
      resultHandler.handle(Future.failedFuture(e));
      return;
    }
    FileSystem fs = conn.vertx().fileSystem();
    fs.open(filename, new OpenOptions().setCreate(false).setWrite(false), ar -> {
      if (ar.succeeded()) {
        AsyncFile file = ar.result();
        long contentLength = Math.min(length, file_.length() - offset);
        file.setReadPos(offset);
        file.setReadLength(contentLength);
      }
      resultHandler.handle(ar);
    });
  }
}

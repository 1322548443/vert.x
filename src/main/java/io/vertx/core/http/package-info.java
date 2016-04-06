/*
 * Copyright 2014 Red Hat, Inc.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */

/**
 * == Writing HTTP servers and clients
 *
 * Vert.x allows you to easily write non blocking HTTP clients and servers.
 *
 * Vert.x supports the HTTP/1.0, HTTP/1.1 and HTTP/2 protocols.
 *
 * The base API for HTTP is the same for HTTP/1.x and HTTP/2, specific API features are available for dealing with the
 * HTTP/2 protocol.
 *
 * === Creating an HTTP Server
 *
 * The simplest way to create an HTTP server, using all default options is as follows:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example1}
 * ----
 *
 * === Configuring an HTTP server
 *
 * If you don't want the default, a server can be configured by passing in a {@link io.vertx.core.http.HttpServerOptions}
 * instance when creating it:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example2}
 * ----
 *
 * === Configuring an HTTP/2 server
 *
 * Vert.x supports HTTP/2 over TLS `h2` and over TCP `h2c`.
 *
 * - `h2` identifies the HTTP/2 protocol when used over TLS negotiated by https://en.wikipedia.org/wiki/Application-Layer_Protocol_Negotiation[Application-Layer Protocol Negotiation] (ALPN)
 * - `h2c` identifies the HTTP/2 protocol when using in clear text over TCP, such connections are established either with
 * an HTTP/1.1 upgraded request or directly
 *
 * To handle `h2` requests, TLS must be enabled along with {@link io.vertx.core.http.HttpServerOptions#setUseAlpn(boolean)}:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example0}
 * ----
 *
 * ALPN is a TLS extension that negotiates the protocol before the client and the server start to exchange data.
 *
 * Clients that don't support ALPN will still be able to do a _classic_ SSL handshake.
 *
 * ALPN will usually agree on the `h2` protocol, although `http/1.1` can be used if the server or the client decides
 * so.
 *
 * To handle `h2c` requests, TLS must be disabled, the server will upgrade to HTTP/2 any request HTTP/1.1 that wants to
 * upgrade to HTTP/2. It will also accept a direct `h2c` connection beginning with the `PRI * HTTP/2.0\r\nSM\r\n` preface.
 *
 * WARNING: most browsers won't support `h2c`, so for serving web sites you should use `h2` and not `h2c`.
 *
 * === Start the Server Listening
 *
 * To tell the server to listen for incoming requests you use one of the {@link io.vertx.core.http.HttpServer#listen}
 * alternatives.
 *
 * To tell the server to listen at the host and port as specified in the options:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example3}
 * ----
 *
 * Or to specify the host and port in the call to listen, ignoring what is configured in the options:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example4}
 * ----
 *
 * The default host is `0.0.0.0` which means 'listen on all available addresses' and the default port is `80`.
 *
 * The actual bind is asynchronous so the server might not actually be listening until some time *after* the call to
 * listen has returned.
 *
 * If you want to be notified when the server is actually listening you can provide a handler to the `listen` call.
 * For example:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example5}
 * ----
 *
 * === Getting notified of incoming requests
 *
 * To be notified when a request arrives you need to set a {@link io.vertx.core.http.HttpServer#requestHandler}:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example6}
 * ----
 *
 * === Handling requests
 *
 * When a request arrives, the request handler is called passing in an instance of {@link io.vertx.core.http.HttpServerRequest}.
 * This object represents the server side HTTP request.
 *
 * The handler is called when the headers of the request have been fully read.
 *
 * If the request contains a body, that body will arrive at the server some time after the request handler has been called.
 *
 * The server request object allows you to retrieve the {@link io.vertx.core.http.HttpServerRequest#uri},
 * {@link io.vertx.core.http.HttpServerRequest#path}, {@link io.vertx.core.http.HttpServerRequest#params} and
 * {@link io.vertx.core.http.HttpServerRequest#headers}, amongst other things.
 *
 * Each server request object is associated with one server response object. You use
 * {@link io.vertx.core.http.HttpServerRequest#response} to get a reference to the {@link io.vertx.core.http.HttpServerResponse}
 * object.
 *
 * Here's a simple example of a server handling a request and replying with "hello world" to it.
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example7_1}
 * ----
 *
 * ==== Request version
 *
 * The version of HTTP specified in the request can be retrieved with {@link io.vertx.core.http.HttpServerRequest#version}
 *
 * ==== Request method
 *
 * Use {@link io.vertx.core.http.HttpServerRequest#method} to retrieve the HTTP method of the request.
 * (i.e. whether it's GET, POST, PUT, DELETE, HEAD, OPTIONS, etc).
 *
 * ==== Request URI
 *
 * Use {@link io.vertx.core.http.HttpServerRequest#uri} to retrieve the URI of the request.
 *
 * Note that this is the actual URI as passed in the HTTP request, and it's almost always a relative URI.
 *
 * The URI is as defined in http://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html[Section 5.1.2 of the HTTP specification - Request-URI]
 *
 * ==== Request path
 *
 * Use {@link io.vertx.core.http.HttpServerRequest#path} to return the path part of the URI
 *
 * For example, if the request URI was:
 *
 *  a/b/c/page.html?param1=abc&param2=xyz
 *
 * Then the path would be
 *
 *  /a/b/c/page.html
 *
 * ==== Request query
 *
 * Use {@link io.vertx.core.http.HttpServerRequest#query} to return the query part of the URI
 *
 * For example, if the request URI was:
 *
 *  a/b/c/page.html?param1=abc&param2=xyz
 *
 * Then the query would be
 *
 *  param1=abc&param2=xyz
 *
 * ==== Request headers
 *
 * Use {@link io.vertx.core.http.HttpServerRequest#headers} to return the headers of the HTTP request.
 *
 * This returns an instance of {@link io.vertx.core.MultiMap} - which is like a normal Map or Hash but allows multiple
 * values for the same key - this is because HTTP allows multiple header values with the same key.
 *
 * It also has case-insensitive keys, that means you can do the following:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example8}
 * ----
 *
 * ==== Request host
 *
 * Use {@link io.vertx.core.http.HttpServerRequest#host} to return the host of the HTTP request.
 *
 * For HTTP/1.x requests the `host` header is returned, for HTTP/1 requests the `:authority` pseudo header is returned.
 *
 * ==== Request parameters
 *
 * Use {@link io.vertx.core.http.HttpServerRequest#params} to return the parameters of the HTTP request.
 *
 * Just like {@link io.vertx.core.http.HttpServerRequest#headers} this returns an instance of {@link io.vertx.core.MultiMap}
 * as there can be more than one parameter with the same name.
 *
 * Request parameters are sent on the request URI, after the path. For example if the URI was:
 *
 *  /page.html?param1=abc&param2=xyz
 *
 * Then the parameters would contain the following:
 *
 * ----
 * param1: 'abc'
 * param2: 'xyz
 * ----
 *
 * Note that these request parameters are retrieved from the URL of the request. If you have form attributes that
 * have been sent as part of the submission of an HTML form submitted in the body of a `multi-part/form-data` request
 * then they will not appear in the params here.
 *
 * ==== Remote address
 *
 * The address of the sender of the request can be retrieved with {@link io.vertx.core.http.HttpServerRequest#remoteAddress}.
 *
 * ==== Absolute URI
 *
 * The URI passed in an HTTP request is usually relative. If you wish to retrieve the absolute URI corresponding
 * to the request, you can get it with {@link io.vertx.core.http.HttpServerRequest#absoluteURI}
 *
 * ==== End handler
 *
 * The {@link io.vertx.core.http.HttpServerRequest#endHandler} of the request is invoked when the entire request,
 * including any body has been fully read.
 *
 * ==== Reading Data from the Request Body
 *
 * Often an HTTP request contains a body that we want to read. As previously mentioned the request handler is called
 * when just the headers of the request have arrived so the request object does not have a body at that point.
 *
 * This is because the body may be very large (e.g. a file upload) and we don't generally want to buffer the entire
 * body in memory before handing it to you, as that could cause the server to exhaust available memory.
 *
 * To receive the body, you can use the {@link io.vertx.core.http.HttpServerRequest#handler}  on the request,
 * this will get called every time a chunk of the request body arrives. Here's an example:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example9}
 * ----
 *
 * The object passed into the handler is a {@link io.vertx.core.buffer.Buffer}, and the handler can be called
 * multiple times as data arrives from the network, depending on the size of the body.
 *
 * In some cases (e.g. if the body is small) you will want to aggregate the entire body in memory, so you could do
 * the aggregation yourself as follows:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example10}
 * ----
 *
 * This is such a common case, that Vert.x provides a {@link io.vertx.core.http.HttpServerRequest#bodyHandler} to do this
 * for you. The body handler is called once when all the body has been received:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example11}
 * ----
 *
 * ==== Pumping requests
 *
 * The request object is a {@link io.vertx.core.streams.ReadStream} so you can pump the request body to any
 * {@link io.vertx.core.streams.WriteStream} instance.
 *
 * See the chapter on <<streams, streams and pumps>> for a detailed explanation.
 *
 * ==== Handling HTML forms
 *
 * HTML forms can be submitted with either a content type of `application/x-www-form-urlencoded` or `multipart/form-data`.
 *
 * For url encoded forms, the form attributes are encoded in the url, just like normal query parameters.
 *
 * For multi-part forms they are encoded in the request body, and as such are not available until the entire body
 * has been read from the wire.
 *
 * Multi-part forms can also contain file uploads.
 *
 * If you want to retrieve the attributes of a multi-part form you should tell Vert.x that you expect to receive
 * such a form *before* any of the body is read by calling {@link io.vertx.core.http.HttpServerRequest#setExpectMultipart}
 * with true, and then you should retrieve the actual attributes using {@link io.vertx.core.http.HttpServerRequest#formAttributes}
 * once the entire body has been read:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example12}
 * ----
 *
 * ==== Handling form file uploads
 *
 * Vert.x can also handle file uploads which are encoded in a multi-part request body.
 *
 * To receive file uploads you tell Vert.x to expect a multi-part form and set an
 * {@link io.vertx.core.http.HttpServerRequest#uploadHandler} on the request.
 *
 * This handler will be called once for every
 * upload that arrives on the server.
 *
 * The object passed into the handler is a {@link io.vertx.core.http.HttpServerFileUpload} instance.
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example13}
 * ----
 *
 * File uploads can be large we don't provide the entire upload in a single buffer as that might result in memory
 * exhaustion, instead, the upload data is received in chunks:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example14}
 * ----
 *
 * The upload object is a {@link io.vertx.core.streams.ReadStream} so you can pump the request body to any
 * {@link io.vertx.core.streams.WriteStream} instance. See the chapter on <<streams, streams and pumps>> for a
 * detailed explanation.
 *
 * If you just want to upload the file to disk somewhere you can use {@link io.vertx.core.http.HttpServerFileUpload#streamToFileSystem}:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example15}
 * ----
 *
 * WARNING: Make sure you check the filename in a production system to avoid malicious clients uploading files
 * to arbitrary places on your filesystem. See <<Security notes, security notes>> for more information.
 *
 * ==== Receiving unknown HTTP/2 frames
 *
 * HTTP/2 is a framed protocol with various frames for the HTTP request/response model. The protocol allows other kind
 * of frames to be sent and received.
 *
 * To receive unknown frames, you can use the {@link io.vertx.core.http.HttpServerRequest#unknownFrameHandler} on the request,
 * this will get called every time an unknown frame arrives. Here's an example:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example1}
 * ----
 *
 * HTTP/2 frames are not subject to flow control - the frame handler will be called immediatly when an
 * unkown frame is received whether the request is paused or is not
 *
 * === Sending back responses
 *
 * The server response object is an instance of {@link io.vertx.core.http.HttpServerResponse} and is obtained from the
 * request with {@link io.vertx.core.http.HttpServerRequest#response}.
 *
 * You use the response object to write a response back to the HTTP client.
 *
 * ==== Setting status code and message
 *
 * The default HTTP status code for a response is `200`, representing `OK`.
 *
 * Use {@link io.vertx.core.http.HttpServerResponse#setStatusCode} to set a different code.
 *
 * You can also specify a custom status message with {@link io.vertx.core.http.HttpServerResponse#setStatusMessage}.
 *
 * If you don't specify a status message, the default one corresponding to the status code will be used.
 *
 * NOTE: for HTTP/2 the status won't be present in the response since the protocol won't transmit the message
 * to the client
 *
 * ==== Writing HTTP responses
 *
 * To write data to an HTTP response, you use one the {@link io.vertx.core.http.HttpServerResponse#write} operations.
 *
 * These can be invoked multiple times before the response is ended. They can be invoked in a few ways:
 *
 * With a single buffer:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example16}
 * ----
 *
 * With a string. In this case the string will encoded using UTF-8 and the result written to the wire.
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example17}
 * ----
 *
 * With a string and an encoding. In this case the string will encoded using the specified encoding and the
 * result written to the wire.
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example18}
 * ----
 *
 * Writing to a response is asynchronous and always returns immediately after the write has been queued.
 *
 * If you are just writing a single string or buffer to the HTTP response you can write it and end the response in a
 * single call to the {@link io.vertx.core.http.HttpServerResponse#end(String)}
 *
 * The first call to write results in the response header being being written to the response. Consequently, if you are
 * not using HTTP chunking then you must set the `Content-Length` header before writing to the response, since it will
 * be too late otherwise. If you are using HTTP chunking you do not have to worry.
 *
 * ==== Ending HTTP responses
 *
 * Once you have finished with the HTTP response you should {@link io.vertx.core.http.HttpServerResponse#end} it.
 *
 * This can be done in several ways:
 *
 * With no arguments, the response is simply ended.
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example19}
 * ----
 *
 * It can also be called with a string or buffer in the same way `write` is called. In this case it's just the same as
 * calling write with a string or buffer followed by calling end with no arguments. For example:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example20}
 * ----
 *
 * ==== Closing the underlying connection
 *
 * You can close the underlying TCP connection with {@link io.vertx.core.http.HttpServerResponse#close}.
 *
 * Non keep-alive connections will be automatically closed by Vert.x when the response is ended.
 *
 * Keep-alive connections are not automatically closed by Vert.x by default. If you want keep-alive connections to be
 * closed after an idle time, then you configure {@link io.vertx.core.http.HttpServerOptions#setIdleTimeout}.
 *
 * HTTP/2 connections send a {@literal GOAWAY} frame before closing the response.
 *
 * ==== Setting response headers
 *
 * HTTP response headers can be added to the response by adding them directly to the
 * {@link io.vertx.core.http.HttpServerResponse#headers}:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example21}
 * ----
 *
 * Or you can use {@link io.vertx.core.http.HttpServerResponse#putHeader}
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example22}
 * ----
 *
 * Headers must all be added before any parts of the response body are written.
 *
 * ==== Chunked HTTP responses and trailers
 *
 * Vert.x supports http://en.wikipedia.org/wiki/Chunked_transfer_encoding[HTTP Chunked Transfer Encoding].
 *
 * This allows the HTTP response body to be written in chunks, and is normally used when a large response body is
 * being streamed to a client and the total size is not known in advance.
 *
 * You put the HTTP response into chunked mode as follows:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example23}
 * ----
 *
 * Default is non-chunked. When in chunked mode, each call to one of the {@link io.vertx.core.http.HttpServerResponse#write}
 * methods will result in a new HTTP chunk being written out.
 *
 * When in chunked mode you can also write HTTP response trailers to the response. These are actually written in
 * the final chunk of the response.
 *
 * NOTE: chunked response has no effect for an HTTP/2 stream
 *
 * To add trailers to the response, add them directly to the {@link io.vertx.core.http.HttpServerResponse#trailers}.
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example24}
 * ----
 *
 * Or use {@link io.vertx.core.http.HttpServerResponse#putTrailer}.
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example25}
 * ----
 *
 * ==== Serving files directly from disk or the classpath
 *
 * If you were writing a web server, one way to serve a file from disk would be to open it as an {@link io.vertx.core.file.AsyncFile}
 * and pump it to the HTTP response.
 *
 * Or you could load it it one go using {@link io.vertx.core.file.FileSystem#readFile} and write it straight to the response.
 *
 * Alternatively, Vert.x provides a method which allows you to serve a file from disk or the filesystem to an HTTP response 
 * in one operation.
 * Where supported by the underlying operating system this may result in the OS directly transferring bytes from the
 * file to the socket without being copied through user-space at all.
 *
 * This is done by using {@link io.vertx.core.http.HttpServerResponse#sendFile}, and is usually more efficient for large
 * files, but may be slower for small files.
 *
 * Here's a very simple web server that serves files from the file system using sendFile:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example26}
 * ----
 *
 * Sending a file is asynchronous and may not complete until some time after the call has returned. If you want to
 * be notified when the file has been writen you can use {@link io.vertx.core.http.HttpServerResponse#sendFile(String, io.vertx.core.Handler)}
 *
 * Please see the chapter about <<classpath, serving files from the classpath>> for restrictions about the classpath resolution or disabling it.
 *
 * NOTE: If you use `sendFile` while using HTTPS it will copy through user-space, since if the kernel is copying data
 * directly from disk to socket it doesn't give us an opportunity to apply any encryption.
 *
 * WARNING: If you're going to write web servers directly using Vert.x be careful that users cannot exploit the
 * path to access files outside the directory from which you want to serve them or the classpath It may be safer instead to use
 * Vert.x Web. 
 *
 * When there is a need to serve just a segment of a file, say starting from a given byte, you can achieve this by doing:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example26b}
 * ----
 *
 * You are not required to supply the length if you want to send a file starting from an offset until the end, in this
 * case you can just do:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example26c}
 * ----
 *
 * ==== Pumping responses
 *
 * The server response is a {@link io.vertx.core.streams.WriteStream} instance so you can pump to it from any
 * {@link io.vertx.core.streams.ReadStream}, e.g. {@link io.vertx.core.file.AsyncFile}, {@link io.vertx.core.net.NetSocket},
 * {@link io.vertx.core.http.WebSocket} or {@link io.vertx.core.http.HttpServerRequest}.
 *
 * Here's an example which echoes the request body back in the response for any PUT methods.
 * It uses a pump for the body, so it will work even if the HTTP request body is much larger than can fit in memory
 * at any one time:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example27}
 * ----
 *
 * ==== Writing HTTP/2 frames
 *
 * HTTP/2 is a framed protocol with various frames for the HTTP request/response model. The protocol allows other kind
 * of frames to be sent and received.
 *
 * To send such frames, you can use the {@link io.vertx.core.http.HttpServerResponse#writeFrame} on the response.
 * Here’s an example:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example2}
 * ----
 *
 * These frames are sent immediately and are not subject to flow control - when such frame is sent there it may be done
 * before other {@literal DATA} frames.
 *
 * ==== Stream reset
 *
 * HTTP/1.x does not allow a clean reset of a request or a response stream, for example when a client uploads
 * a resource already present on the server, the server needs to accept the entire response.
 *
 * HTTP/2 supports stream reset at any time during the request/response:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example3}
 * ----
 *
 * By default the `NO_ERROR` (0) error code is sent, another code can sent instead:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example4}
 * ----
 *
 * The HTTP/2 specification defines the list of http://httpwg.org/specs/rfc7540.html#ErrorCodes[error codes] one can use.
 *
 * The request handler are notified of stream reset events with the {@link io.vertx.core.http.HttpServerRequest#exceptionHandler request handler} and
 * {@link io.vertx.core.http.HttpServerResponse#exceptionHandler response handler}:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example5}
 * ----
 *
 * ==== Server push
 *
 * Server push is a new feature of HTTP/2 that enables sending multiple responses in parallel for a single client request.
 *
 * When a server process a request, it can push a request/response to the client:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example6}
 * ----
 *
 * When the server is ready to push the response, the push response handler is called and the handler can send the response.
 *
 * The push response handler may receive a failure, for instance the client may cancel the push because it already has `main.js` in its
 * cache and does not want it anymore.
 *
 * The {@link io.vertx.core.http.HttpServerResponse#push} method must be called before the initiating response ends, however
 * the pushed response can be written after.
 *
 * === HTTP Compression
 *
 * Vert.x comes with support for HTTP Compression out of the box.
 *
 * This means you are able to automatically compress the body of the responses before they are sent back to the client.
 *
 * If the client does not support HTTP compression the responses are sent back without compressing the body.
 *
 * This allows to handle Client that support HTTP Compression and those that not support it at the same time.
 *
 * To enable compression use can configure it with {@link io.vertx.core.http.HttpServerOptions#setCompressionSupported}.
 *
 * By default compression is not enabled.
 *
 * When HTTP compression is enabled the server will check if the client includes an `Accept-Encoding` header which
 * includes the supported compressions. Commonly used are deflate and gzip. Both are supported by Vert.x.
 *
 * If such a header is found the server will automatically compress the body of the response with one of the supported
 * compressions and send it back to the client.
 *
 * Be aware that compression may be able to reduce network traffic but is more CPU-intensive.
 *
 * === Creating an HTTP client
 *
 * You create an {@link io.vertx.core.http.HttpClient} instance with default options as follows:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example28}
 * ----
 *
 * If you want to configure options for the client, you create it as follows:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example29}
 * ----
 *
 * Vert.x supports HTTP/2 over TLS `h2` and over TCP `h2c`.
 *
 * By default the http client performs HTTP/1.1 requests, to perform HTTP/2 requests the {@link io.vertx.core.http.HttpClientOptions#setProtocolVersion}
 * must be set to {@link io.vertx.core.http.HttpVersion#HTTP_2}.
 *
 * For `h2` requests, TLS must be enabled with _Application-Layer Protocol Negotiation_:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example7}
 * ----
 *
 * For `h2c` requests, TLS must be disabled, the client will do an HTTP/1.1 requests and try an upgrade to HTTP/2:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example8}
 * ----
 *
 * `h2c` connections can also be established directly, i.e connection started with a prior knowledge, when
 * {@link io.vertx.core.http.HttpClientOptions#setH2cUpgrade(boolean)} options is set to false: after the
 * connection is established, the client will send the HTTP/2 connection preface and expect to receive
 * the same preface from the server.
 *
 * The http server may not support HTTP/2, the actual version can be checked
 * with {@link io.vertx.core.http.HttpClientResponse#version()} when the response arrives.
 *
 * === Making requests
 *
 * The http client is very flexible and there are various ways you can make requests with it.
 *
 *
 * Often you want to make many requests to the same host/port with an http client. To avoid you repeating the host/port
 * every time you make a request you can configure the client with a default host/port:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example30}
 * ----
 *
 * Alternatively if you find yourself making lots of requests to different host/ports with the same client you can
 * simply specify the host/port when doing the request.
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example31}
 * ----
 *
 * Both methods of specifying host/port are supported for all the different ways of making requests with the client.
 *
 * ==== Simple requests with no request body
 *
 * Often, you'll want to make HTTP requests with no request body. This is usually the case with HTTP GET, OPTIONS and
 * HEAD requests.
 *
 * The simplest way to do this with the Vert.x http client is using the methods prefixed with `Now`. For example
 * {@link io.vertx.core.http.HttpClient#getNow}.
 *
 * These methods create the http request and send it in a single method call and allow you to provide a handler that will be
 * called with the http response when it comes back.
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example32}
 * ----
 *
 * ==== Writing general requests
 *
 * At other times you don't know the request method you want to send until run-time. For that use case we provide
 * general purpose request methods such as {@link io.vertx.core.http.HttpClient#request} which allow you to specify
 * the HTTP method at run-time:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example33}
 * ----
 *
 * ==== Writing request bodies
 *
 * Sometimes you'll want to write requests which have a body, or perhaps you want to write headers to a request
 * before sending it.
 *
 * To do this you can call one of the specific request methods such as {@link io.vertx.core.http.HttpClient#post} or
 * one of the general purpose request methods such as {@link io.vertx.core.http.HttpClient#request}.
 *
 * These methods don't send the request immediately, but instead return an instance of {@link io.vertx.core.http.HttpClientRequest}
 * which can be used to write to the request body or write headers.
 *
 * Here are some examples of writing a POST request with a body:
 *m
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example34}
 * ----
 *
 * Methods exist to write strings in UTF-8 encoding and in any specific encoding and to write buffers:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example35}
 * ----
 *
 * If you are just writing a single string or buffer to the HTTP request you can write it and end the request in a
 * single call to the `end` function.
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example36}
 * ----
 *
 * When you're writing to a request, the first call to `write` will result in the request headers being written
 * out to the wire.
 *
 * The actual write is asynchronous and might not occur until some time after the call has returned.
 *
 * Non-chunked HTTP requests with a request body require a `Content-Length` header to be provided.
 *
 * Consequently, if you are not using chunked HTTP then you must set the `Content-Length` header before writing
 * to the request, as it will be too late otherwise.
 *
 * If you are calling one of the `end` methods that take a string or buffer then Vert.x will automatically calculate
 * and set the `Content-Length` header before writing the request body.
 *
 * If you are using HTTP chunking a a `Content-Length` header is not required, so you do not have to calculate the size
 * up-front.
 *
 * ==== Writing request headers
 *
 * You can write headers to a request using the {@link io.vertx.core.http.HttpClientRequest#headers()} multi-map as follows:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example37}
 * ----
 *
 * The headers are an instance of {@link io.vertx.core.MultiMap} which provides operations for adding, setting and removing
 * entries. Http headers allow more than one value for a specific key.
 *
 * You can also write headers using {@link io.vertx.core.http.HttpClientRequest#putHeader}
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example38}
 * ----
 *
 * If you wish to write headers to the request you must do so before any part of the request body is written.
 *
 * ==== Ending HTTP requests
 *
 * Once you have finished with the HTTP request you must end it with one of the {@link io.vertx.core.http.HttpClientRequest#end}
 * operations.
 *
 * Ending a request causes any headers to be written, if they have not already been written and the request to be marked
 * as complete.
 *
 * Requests can be ended in several ways. With no arguments the request is simply ended:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example39}
 * ----
 *
 * Or a string or buffer can be provided in the call to `end`. This is like calling `write` with the string or buffer
 * before calling `end` with no arguments
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example40}
 * ----
 *
 * ==== Chunked HTTP requests
 *
 * Vert.x supports http://en.wikipedia.org/wiki/Chunked_transfer_encoding[HTTP Chunked Transfer Encoding] for requests.
 *
 * This allows the HTTP request body to be written in chunks, and is normally used when a large request body is being streamed
 * to the server, whose size is not known in advance.
 *
 * You put the HTTP request into chunked mode using {@link io.vertx.core.http.HttpClientRequest#setChunked(boolean)}.
 *
 * In chunked mode each call to write will cause a new chunk to be written to the wire. In chunked mode there is
 * no need to set the `Content-Length` of the request up-front.
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example41}
 * ----
 *
 * ==== Request timeouts
 *
 * You can set a timeout for a specific http request using {@link io.vertx.core.http.HttpClientRequest#setTimeout(long)}.
 *
 * If the request does not return any data within the timeout period an exception will be passed to the exception handler
 * (if provided) and the request will be closed.
 *
 * ==== Handling exceptions
 *
 * You can handle exceptions corresponding to a request by setting an exception handler on the
 * {@link io.vertx.core.http.HttpClientRequest} instance:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example42}
 * ----
 *
 * This does not handle non _2xx_ response that need to be handled in the
 * {@link io.vertx.core.http.HttpClientResponse} code:
 *
 * [source, $lang]
 * ----
 * {@link examples.HTTPExamples#statusCodeHandling}
 * ----
 *
 * IMPORTANT: `XXXNow` methods cannot receive an exception handler.
 *
 * ==== Specifying a handler on the client request
 *
 * Instead of providing a response handler in the call to create the client request object, alternatively, you can
 * not provide a handler when the request is created and set it later on the request object itself, using
 * {@link io.vertx.core.http.HttpClientRequest#handler(io.vertx.core.Handler)}, for example:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example43}
 * ----
 *
 * ==== Using the request as a stream
 *
 * The {@link io.vertx.core.http.HttpClientRequest} instance is also a {@link io.vertx.core.streams.WriteStream} which means
 * you can pump to it from any {@link io.vertx.core.streams.ReadStream} instance.
 *
 * For, example, you could pump a file on disk to a http request body as follows:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example44}
 * ----
 *
 * ==== Writing HTTP/2 frames
 *
 * HTTP/2 is a framed protocol with various frames for the HTTP request/response model. The protocol allows other kind
 * of frames to be sent and received.
 *
 * To send such frames, you can use the {@link io.vertx.core.http.HttpClientRequest#write} on the request. Here’s an example:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example9}
 * ----
 *
 * ==== Stream reset
 *
 * HTTP/1.x does not allow a clean reset of a request or a response stream, for example when a client uploads a resource already
 * present on the server, the server needs to accept the entire response.
 *
 * HTTP/2 supports stream reset at any time during the request/response:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example10}
 * ----
 *
 * By default the NO_ERROR (0) error code is sent, another code can sent instead:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example11}
 * ----
 *
 * The HTTP/2 specification defines the list of http://httpwg.org/specs/rfc7540.html#ErrorCodes[error codes] one can use.
 *
 * The request handler are notified of stream reset events with the {@link io.vertx.core.http.HttpClientRequest#exceptionHandler request handler} and
 * {@link io.vertx.core.http.HttpClientResponse#exceptionHandler response handler}:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example12}
 * ----
 *
 * === Handling http responses
 *
 * You receive an instance of {@link io.vertx.core.http.HttpClientResponse} into the handler that you specify in of
 * the request methods or by setting a handler directly on the {@link io.vertx.core.http.HttpClientRequest} object.
 *
 * You can query the status code and the status message of the response with {@link io.vertx.core.http.HttpClientResponse#statusCode}
 * and {@link io.vertx.core.http.HttpClientResponse#statusMessage}.
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example45}
 * ----
 *
 * ==== Using the response as a stream
 *
 * The {@link io.vertx.core.http.HttpClientResponse} instance is also a {@link io.vertx.core.streams.ReadStream} which means
 * you can pump it to any {@link io.vertx.core.streams.WriteStream} instance.
 *
 * ==== Response headers and trailers
 *
 * Http responses can contain headers. Use {@link io.vertx.core.http.HttpClientResponse#headers} to get the headers.
 *
 * The object returned is a {@link io.vertx.core.MultiMap} as HTTP headers can contain multiple values for single keys.
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example46}
 * ----
 *
 * Chunked HTTP responses can also contain trailers - these are sent in the last chunk of the response body.
 *
 * You use {@link io.vertx.core.http.HttpClientResponse#trailers} to get the trailers. Trailers are also a {@link io.vertx.core.MultiMap}.
 *
 * ==== Reading the request body
 *
 * The response handler is called when the headers of the response have been read from the wire.
 *
 * If the response has a body this might arrive in several pieces some time after the headers have been read. We
 * don't wait for all the body to arrive before calling the response handler as the response could be very large and we
 * might be waiting a long time, or run out of memory for large responses.
 *
 * As parts of the response body arrive, the {@link io.vertx.core.http.HttpClientResponse#handler} is called with
 * a {@link io.vertx.core.buffer.Buffer} representing the piece of the body:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example47}
 * ----
 *
 * If you know the response body is not very large and want to aggregate it all in memory before handling it, you can
 * either aggregate it yourself:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example48}
 * ----
 *
 * Or you can use the convenience {@link io.vertx.core.http.HttpClientResponse#bodyHandler(io.vertx.core.Handler)} which
 * is called with the entire body when the response has been fully read:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example49}
 * ----
 *
 * ==== Response end handler
 *
 * The response {@link io.vertx.core.http.HttpClientResponse#endHandler} is called when the entire response body has been read
 * or immediately after the headers have been read and the response handler has been called if there is no body.
 *
 * ==== Reading cookies from the response
 *
 * You can retrieve the list of cookies from a response using {@link io.vertx.core.http.HttpClientResponse#cookies()}.
 *
 * Alternatively you can just parse the `Set-Cookie` headers yourself in the response.
 *
 *
 * ==== 100-Continue handling
 *
 * According to the http://www.w3.org/Protocols/rfc2616/rfc2616-sec8.html[HTTP 1.1 specification] a client can set a
 * header `Expect: 100-Continue` and send the request header before sending the rest of the request body.
 *
 * The server can then respond with an interim response status `Status: 100 (Continue)` to signify to the client that
 * it is ok to send the rest of the body.
 *
 * The idea here is it allows the server to authorise and accept/reject the request before large amounts of data are sent.
 * Sending large amounts of data if the request might not be accepted is a waste of bandwidth and ties up the server
 * in reading data that it will just discard.
 *
 * Vert.x allows you to set a {@link io.vertx.core.http.HttpClientRequest#continueHandler(io.vertx.core.Handler)} on the
 * client request object
 *
 * This will be called if the server sends back a `Status: 100 (Continue)` response to signify that it is ok to send
 * the rest of the request.
 *
 * This is used in conjunction with {@link io.vertx.core.http.HttpClientRequest#sendHead()}to send the head of the request.
 *
 * Here's an example:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example50}
 * ----
 *
 * On the server side a Vert.x http server can be configured to automatically send back 100 Continue interim responses
 * when it receives an `Expect: 100-Continue` header.
 *
 * This is done by setting the option {@link io.vertx.core.http.HttpServerOptions#setHandle100ContinueAutomatically(boolean)}.
 *
 * If you'd prefer to decide whether to send back continue responses manually, then this property should be set to
 * `false` (the default), then you can inspect the headers and call {@link io.vertx.core.http.HttpServerResponse#writeContinue()}
 * to have the client continue sending the body:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example50_1}
 * ----
 *
 * You can also reject the request by sending back a failure status code directly: in this case the body
 * should either be ignored or the connection should be closed (100-Continue is a performance hint and
 * cannot be a logical protocol constraint):
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example50_2}
 * ----
 *
 * ==== Client push
 *
 * Server push is a new feature of HTTP/2 that enables sending multiple responses in parallel for a single client request.
 *
 * A push handler can be set on a request to receive the request/response pushed by the server:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example13}
 * ----
 *
 * If the client does not want to receive a pushed request, it can reset the stream:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example13}
 * ----
 *
 * When no handler is set, any stream pushed will be automatically cancelled by the client with
 * a stream reset (`8` error code).
 *
 * ==== Receiving unknown HTTP/2 frames
 *
 * HTTP/2 is a framed protocol with various frames for the HTTP request/response model. The protocol allows other kind of
 * frames to be sent and received.
 *
 * To receive unknown frames, you can use the unknownFrameHandler on the request, this will get called every time an unknown
 * frame arrives. Here’s an example:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example15}
 * ----
 *
 * === Enabling compression on the client
 *
 * The http client comes with support for HTTP Compression out of the box.
 *
 * This means the client can let the remote http server know that it supports compression, and will be able to handle
 * compressed response bodies.
 *
 * An http server is free to either compress with one of the supported compression algorithms or to send the body back
 * without compressing it at all. So this is only a hint for the Http server which it may ignore at will.
 *
 * To tell the http server which compression is supported by the client it will include an `Accept-Encoding` header with
 * the supported compression algorithm as value. Multiple compression algorithms are supported. In case of Vert.x this
 * will result in the following header added:
 *
 *  Accept-Encoding: gzip, deflate
 *
 * The server will choose then from one of these. You can detect if a server ompressed the body by checking for the
 * `Content-Encoding` header in the response sent back from it.
 *
 * If the body of the response was compressed via gzip it will include for example the following header:
 *
 *  Content-Encoding: gzip
 *
 * To enable compression set {@link io.vertx.core.http.HttpClientOptions#setTryUseCompression(boolean)} on the options
 * used when creating the client.
 *
 * By default compression is disabled.
 *
 * === HTTP/1.x pooling and keep alive
 *
 * Http keep alive allows http connections to be used for more than one request. This can be a more efficient use of
 * connections when you're making multiple requests to the same server.
 *
 * For HTTP/1.x versions, the http client supports pooling of connections, allowing you to reuse connections between requests.
 *
 * For pooling to work, keep alive must be true using {@link io.vertx.core.http.HttpClientOptions#setKeepAlive(boolean)}
 * on the options used when configuring the client. The default value is true.
 *
 * When keep alive is enabled. Vert.x will add a `Connection: Keep-Alive` header to each HTTP/1.0 request sent.
 * When keep alive is disabled. Vert.x will add a `Connection: Close` header to each HTTP/1.1 request sent to signal
 * that the connection will be closed after completion of the response.
 *
 * The maximum number of connections to pool *for each server* is configured using {@link io.vertx.core.http.HttpClientOptions#setMaxPoolSize(int)}
 *
 * When making a request with pooling enabled, Vert.x will create a new connection if there are less than the maximum number of
 * connections already created for that server, otherwise it will add the request to a queue.
 *
 * Keep alive connections will not be closed by the client automatically. To close them you can close the client instance.
 *
 * Alternatively you can set idle timeout using {@link io.vertx.core.http.HttpClientOptions#setIdleTimeout(int)} - any
 * connections not used within this timeout will be closed. Please note the idle timeout value is in seconds not milliseconds.
 *
 * === HTTP/1.1 pipe-lining
 *
 * The client also supports pipe-lining of requests on a connection.
 *
 * Pipe-lining means another request is sent on the same connection before the response from the preceding one has
 * returned. Pipe-lining is not appropriate for all requests.
 *
 * To enable pipe-lining, it must be enabled using {@link io.vertx.core.http.HttpClientOptions#setPipelining(boolean)}.
 * By default pipe-lining is disabled.
 *
 * When pipe-lining is enabled requests will be written to connections without waiting for previous responses to return.
 *
 * === HTTP/2 multiplexing
 *
 * For HTTP/2, the http client uses a single connection for each server, all the requests to the same server are
 * multiplexed on the same connection.
 *
 * HTTP/2 connections will not be closed by the client automatically. To close them you can call {@link io.vertx.core.http.HttpConnection#close()}
 * or close the client instance.
 *
 * Alternatively you can set idle timeout using {@link io.vertx.core.http.HttpClientOptions#setIdleTimeout(int)} - any
 * connections not used within this timeout will be closed. Please note the idle timeout value is in seconds not milliseconds.
 *
 * === HTTP/2 connections
 *
 * HTTP/2 does not change HTTP programming and the design of HTTP server and clients remains the same. However HTTP/2
 * defines a mapping of HTTP's semantics to a connection.
 *
 * The {@link io.vertx.core.http.HttpConnection} offers the API for dealing with HTTP/2 connection events, lifecycle
 * and settings.
 *
 * ==== Server connections
 *
 * The {@link io.vertx.core.http.HttpServerRequest#connection()} method returns the request connection on the server:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example16}
 * ----
 *
 * A connection handler can be set on the server to be notified of any incoming connection:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example17}
 * ----
 *
 * NOTE: this only applies to the HTTP/2 protocol
 *
 * ==== Client connections
 *
 * The {@link io.vertx.core.http.HttpClientRequest#connection()} method returns the request connection on the client:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example18}
 * ----
 *
 * A connection handler can be set on the request to be notified when the connection happens:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example19}
 * ----
 *
 * NOTE: this only applies to the HTTP/2 protocol
 *
 * ==== Connection settings
 *
 * The configuration of an HTTP/2 is configured by the {@link io.vertx.core.http.Http2Settings} data object.
 *
 * Each endpoint must respect the settings sent by the other side of the connection.
 *
 * When a connection is established, the client and the server exchange initial settings. Initial settings
 * are configured by {@link io.vertx.core.http.HttpClientOptions#setInitialSettings} on the client and
 * {@link io.vertx.core.http.HttpServerOptions#setInitialSettings} on the server.
 *
 * The settings can be changed at any time after the connection is established:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example20}
 * ----
 *
 * As the remote side should acknowledge on reception of the settings update, it's possible to give a callback
 * to be notified of the acknowledgent:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example21}
 * ----
 *
 * Conversely the {@link io.vertx.core.http.HttpConnection#remoteSettingsHandler(io.vertx.core.Handler)} is notified
 * when the new remote settings are received:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example22}
 * ----
 *
 * ==== Connection ping
 *
 * HTTP/2 connection ping is useful for determining the connection round-trip time or check the connection
 * validity: {@link io.vertx.core.http.HttpConnection#ping} sends a {@literal PING} frame to the remote
 * endpoint:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example23}
 * ----
 *
 * Vert.x will send automatically an acknowledgement when a {@literal PING} frame is received,
 * an handler can be set to be called when the connection is pinged:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example24}
 * ----
 *
 * ==== Connection shutdown
 *
 * Calling {@link io.vertx.core.http.HttpConnection#shutdown()} will send a {@literal GOAWAY} frame to the
 * remote side of the connection, asking it to stop creating streams: a client will stop doing new requests
 * and a server will stop pushing responses. After the {@literal GOAWAY} frame is sent, the connection
 * waits some time (30 seconds by default) until all current streams closed and close the connection:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example25}
 * ----
 *
 * Connection {@link io.vertx.core.http.HttpConnection#close} close is a shutdown with no delay, the {@literal GOAWAY}
 * frame will still be sent before the connection is closed.
 *
 * The {@link io.vertx.core.http.HttpConnection#closeHandler} notifies when connection is closed,
 * {@link io.vertx.core.http.HttpConnection#shutdownHandler} notifies when all streams have been closed but the
 * connection is not yet closed.
 *
 * Finally it's possible to just send a {@literal GOAWAY} frame, the main difference with a shutdown is that
 * it will just tell the remote side of the connection to stop creating new streams without scheduling a connection
 * close:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example26}
 * ----
 *
 * Conversely, it is also possible to be notified when {@literal GOAWAY} are received:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example27}
 * ----
 *
 * The {@link io.vertx.core.http.HttpConnection#shutdownHandler} will be called when all current streams
 * have been closed and the connection can be closed:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTP2Examples#example28}
 * ----
 *
 * This applies also when a {@literal GOAWAY} is received.
 *
 * === HttpClient usage
 *
 * The HttpClient can be used in a Verticle or embedded.
 *
 * When used in a Verticle, the Verticle *should use its own client instance*.
 *
 * More generally a client should not be shared between different Vert.x contexts as it can lead to unexpected behavior.
 *
 * For example a keep-alive connection will call the client handlers on the context of the request that opened the connection, subsequent requests will use
 * the same context.
 *
 * When this happen Vert.x detects it and log a warn:
 *
 * ----
 * Reusing a connection with a different context: an HttpClient is probably shared between different Verticles
 * ----
 *
 * The HttpClient can be embedded in a non Vert.x thread like a unit test or a plain java `main`: the client handlers
 * will be called by different Vert.x threads and contexts, such contexts are created as needed. For production this
 * usage is not recommended.
 *
 * === Server sharing
 *
 * When several HTTP servers listen on the same port, vert.x orchestrates the request handling using a
 * round-robin strategy.
 *
 * Let's take a verticle creating a HTTP server such as:
 *
 * .io.vertx.examples.http.sharing.HttpServerVerticle
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#serversharing(io.vertx.core.Vertx)}
 * ----
 *
 * This service is listening on the port 8080. So, when this verticle is instantiated multiple times as with:
 * `vertx run io.vertx.examples.http.sharing.HttpServerVerticle -instances 2`, what's happening ? If both
 * verticles would bind to the same port, you would receive a socket exception. Fortunately, vert.x is handling
 * this case for you. When you deploy another server on the same host and port as an existing server it doesn't
 * actually try and create a new server listening on the same host/port. It binds only once to the socket. When
 * receiving a request it calls the server handlers following a round robin strategy.
 *
 * Let's now imagine a client such as:
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#serversharingclient(io.vertx.core.Vertx)}
 * ----
 *
 * Vert.x delegates the requests to one of the server sequentially:
 *
 * [source]
 * ----
 * Hello from i.v.e.h.s.HttpServerVerticle@1
 * Hello from i.v.e.h.s.HttpServerVerticle@2
 * Hello from i.v.e.h.s.HttpServerVerticle@1
 * Hello from i.v.e.h.s.HttpServerVerticle@2
 * ...
 * ----
 *
 * Consequently the servers can scale over available cores while each Vert.x verticle instance remains strictly
 * single threaded, and you don't have to do any special tricks like writing load-balancers in order to scale your
 * server on your multi-core machine.
 *
 * === Using HTTPS with Vert.x
 *
 * Vert.x http servers and clients can be configured to use HTTPS in exactly the same way as net servers.
 *
 * Please see <<ssl, configuring net servers to use SSL>> for more information.
 *
 * === WebSockets
 *
 * http://en.wikipedia.org/wiki/WebSocket[WebSockets] are a web technology that allows a full duplex socket-like
 * connection between HTTP servers and HTTP clients (typically browsers).
 *
 * Vert.x supports WebSockets on both the client and server-side.
 *
 * ==== WebSockets on the server
 *
 * There are two ways of handling WebSockets on the server side.
 *
 * ===== WebSocket handler
 *
 * The first way involves providing a {@link io.vertx.core.http.HttpServer#websocketHandler(io.vertx.core.Handler)}
 * on the server instance.
 *
 * When a WebSocket connection is made to the server, the handler will be called, passing in an instance of
 * {@link io.vertx.core.http.ServerWebSocket}.
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example51}
 * ----
 *
 * You can choose to reject the WebSocket by calling {@link io.vertx.core.http.ServerWebSocket#reject()}.
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example52}
 * ----
 *
 * ===== Upgrading to WebSocket
 *
 * The second way of handling WebSockets is to handle the HTTP Upgrade request that was sent from the client, and
 * call {@link io.vertx.core.http.HttpServerRequest#upgrade()} on the server request.
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example53}
 * ----
 *
 * ===== The server WebSocket
 *
 * The {@link io.vertx.core.http.ServerWebSocket} instance enables you to retrieve the {@link io.vertx.core.http.ServerWebSocket#headers() headers},
 * {@link io.vertx.core.http.ServerWebSocket#path() path}, {@link io.vertx.core.http.ServerWebSocket#query() query} and
 * {@link io.vertx.core.http.ServerWebSocket#uri() URI} of the HTTP request of the WebSocket handshake.
 *
 * ==== WebSockets on the client
 *
 * The Vert.x {@link io.vertx.core.http.HttpClient} supports WebSockets.
 *
 * You can connect a WebSocket to a server using one of the {@link io.vertx.core.http.HttpClient#websocket} operations and
 * providing a handler.
 *
 * The handler will be called with an instance of {@link io.vertx.core.http.WebSocket} when the connection has been made:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example54}
 * ----
 *
 * ==== Writing messages to WebSockets
 *
 * If you wish to write a single binary WebSocket message to the WebSocket you can do this with
 * {@link io.vertx.core.http.WebSocket#writeBinaryMessage(io.vertx.core.buffer.Buffer)}:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example55}
 * ----
 *
 * If the WebSocket message is larger than the maximum websocket frame size as configured with
 * {@link io.vertx.core.http.HttpClientOptions#setMaxWebsocketFrameSize(int)}
 * then Vert.x will split it into multiple WebSocket frames before sending it on the wire.
 *
 * ==== Writing frames to WebSockets
 *
 * A WebSocket message can be composed of multiple frames. In this case the first frame is either a _binary_ or _text_ frame
 * followed by zero or more _continuation_ frames.
 *
 * The last frame in the message is marked as _final_.
 *
 * To send a message consisting of multiple frames you create frames using
 * {@link io.vertx.core.http.WebSocketFrame#binaryFrame(io.vertx.core.buffer.Buffer, boolean)}
 * , {@link io.vertx.core.http.WebSocketFrame#textFrame(java.lang.String, boolean)} or
 * {@link io.vertx.core.http.WebSocketFrame#continuationFrame(io.vertx.core.buffer.Buffer, boolean)} and write them
 * to the WebSocket using {@link io.vertx.core.http.WebSocket#writeFrame(io.vertx.core.http.WebSocketFrame)}.
 *
 * Here's an example for binary frames:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example56}
 * ----
 *
 * In many cases you just want to send a websocket message that consists of a single final frame, so we provide a couple
 * of shortcut methods to do that with {@link io.vertx.core.http.WebSocket#writeFinalBinaryFrame(io.vertx.core.buffer.Buffer)}
 * and {@link io.vertx.core.http.WebSocket#writeFinalTextFrame(String)}.
 *
 * Here's an example:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example56_1}
 * ----
 *
 * ==== Reading frames from WebSockets
 *
 * To read frames from a WebSocket you use the {@link io.vertx.core.http.WebSocket#frameHandler(io.vertx.core.Handler)}.
 *
 * The frame handler will be called with instances of {@link io.vertx.core.http.WebSocketFrame} when a frame arrives,
 * for example:
 *
 * [source,$lang]
 * ----
 * {@link examples.HTTPExamples#example57}
 * ----
 *
 * ==== Closing WebSockets
 *
 * Use {@link io.vertx.core.http.WebSocket#close()} to close the WebSocket connection when you have finished with it.
 *
 * ==== Streaming WebSockets
 *
 * The {@link io.vertx.core.http.WebSocket} instance is also a {@link io.vertx.core.streams.ReadStream} and a
 * {@link io.vertx.core.streams.WriteStream} so it can be used with pumps.
 *
 * When using a WebSocket as a write stream or a read stream it can only be used with WebSockets connections that are
 * used with binary frames that are no split over multiple frames.
 *
 * === Automatic clean-up in verticles
 *
 * If you're creating http servers and clients from inside verticles, those servers and clients will be automatically closed
 * when the verticle is undeployed.
 *
 */
@Document(fileName = "http.adoc")
package io.vertx.core.http;

import io.vertx.docgen.Document;


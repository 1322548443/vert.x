/*
 * Copyright (c) 2011-2017 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.core.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.netty.buffer.ByteBufInputStream;
import io.vertx.core.buffer.Buffer;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class Json {

  public static ObjectMapper mapper = new ObjectMapper();
  public static ObjectMapper prettyMapper = new ObjectMapper();

  static {
    initialize();
  }

  private static void initialize() {
    // Non-standard JSON but we allow C style comments in our JSON
    mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);

    prettyMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
    prettyMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

    SimpleModule module = new SimpleModule();
    // custom types
    module.addSerializer(JsonObject.class, new JsonObjectSerializer());
    module.addSerializer(JsonArray.class, new JsonArraySerializer());
    // he have 2 extensions: RFC-7493
    module.addSerializer(Instant.class, new InstantSerializer());
    module.addDeserializer(Instant.class, new InstantDeserializer());
    module.addSerializer(byte[].class, new ByteArraySerializer());
    module.addDeserializer(byte[].class, new ByteArrayDeserializer());

    mapper.registerModule(module);
    prettyMapper.registerModule(module);
  }

  /**
   * Encode a POJO to JSON using the underlying Jackson mapper.
   *
   * @param obj a POJO
   * @return a String containing the JSON representation of the given POJO.
   * @throws EncodeException if a property cannot be encoded.
   */
  public static String encode(Object obj) throws EncodeException {
    try {
      return mapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
    }
  }

  /**
   * Encode a POJO to JSON using the underlying Jackson mapper.
   *
   * @param obj a POJO
   * @return a Buffer containing the JSON representation of the given POJO.
   * @throws EncodeException if a property cannot be encoded.
   */
  public static Buffer encodeToBuffer(Object obj) throws EncodeException {
    try {
      return Buffer.buffer(mapper.writeValueAsBytes(obj));
    } catch (Exception e) {
      throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
    }
  }

  /**
   * Encode a POJO to JSON with pretty indentation, using the underlying Jackson mapper.
   *
   * @param obj a POJO
   * @return a String containing the JSON representation of the given POJO.
   * @throws EncodeException if a property cannot be encoded.
   */
  public static String encodePrettily(Object obj) throws EncodeException {
    try {
      return prettyMapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new EncodeException("Failed to encode as JSON: " + e.getMessage());
    }
  }

  /**
   * Decode a given JSON string to a POJO of the given class type.
   * @param str the JSON string.
   * @param clazz the class to map to.
   * @param <T> the generic type.
   * @return an instance of T
   * @throws DecodeException when there is a parsing or invalid mapping.
   */
  public static <T> T decodeValue(String str, Class<T> clazz) throws DecodeException {
    try {
      return mapper.readValue(str, clazz);
    } catch (Exception e) {
      throw new DecodeException("Failed to decode: " + e.getMessage());
    }
  }

  /**
   * Decode a given JSON string.
   *
   * @param str the JSON string.
   *
   * @return a JSON element which can be a {@link JsonArray}, {@link JsonObject}, {@link String}, ...etc if the content is an array, object, string, ...etc
   * @throws DecodeException when there is a parsing or invalid mapping.
   */
  public static Object decodeValue(String str) throws DecodeException {
    try {
      Object value = mapper.readValue(str, Object.class);
      if (value instanceof List) {
        List list = (List) value;
        return new JsonArray(list);
      } else if (value instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) value;
        return new JsonObject(map);
      }
      return value;
    } catch (Exception e) {
      throw new DecodeException("Failed to decode: " + e.getMessage());
    }
  }

  /**
   * Decode a given JSON string to a POJO of the given type.
   * @param str the JSON string.
   * @param type the type to map to.
   * @param <T> the generic type.
   * @return an instance of T
   * @throws DecodeException when there is a parsing or invalid mapping.
   */
  public static <T> T decodeValue(String str, TypeReference<T> type) throws DecodeException {
    try {
      return mapper.readValue(str, type);
    } catch (Exception e) {
      throw new DecodeException("Failed to decode: " + e.getMessage(), e);
    }
  }

  /**
   * Decode a given JSON buffer.
   *
   * @param buf the JSON buffer.
   *
   * @return a JSON element which can be a {@link JsonArray}, {@link JsonObject}, {@link String}, ...etc if the buffer contains an array, object, string, ...etc
   * @throws DecodeException when there is a parsing or invalid mapping.
   */
  public static Object decodeValue(Buffer buf) throws DecodeException {
    try {
      Object value = decodeValue(buf, Object.class);
      if (value instanceof List) {
        List list = (List) value;
        return new JsonArray(list);
      } else if (value instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) value;
        return new JsonObject(map);
      }
      return value;
    } catch (Exception e) {
      throw new DecodeException("Failed to decode: " + e.getMessage());
    }
  }

  /**
   * Decode a given JSON buffer to a POJO of the given class type.
   * @param buf the JSON buffer.
   * @param type the type to map to.
   * @param <T> the generic type.
   * @return an instance of T
   * @throws DecodeException when there is a parsing or invalid mapping.
   */
  public static <T> T decodeValue(Buffer buf, TypeReference<T> type) throws DecodeException {
    try {
      return mapper.readValue(new ByteBufInputStream(buf.getByteBuf()), type);
    } catch (Exception e) {
      throw new DecodeException("Failed to decode:" + e.getMessage(), e);
    }
  }

  /**
   * Decode a given JSON buffer to a POJO of the given class type.
   * @param buf the JSON buffer.
   * @param clazz the class to map to.
   * @param <T> the generic type.
   * @return an instance of T
   * @throws DecodeException when there is a parsing or invalid mapping.
   */
  public static <T> T decodeValue(Buffer buf, Class<T> clazz) throws DecodeException {
    try {
      return mapper.readValue((InputStream) new ByteBufInputStream(buf.getByteBuf()), clazz);
    } catch (Exception e) {
      throw new DecodeException("Failed to decode:" + e.getMessage(), e);
    }
  }
}

// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: response.proto

package org.zgc.nio.protocol;

public interface MethodInvokeResponseOrBuilder extends
    // @@protoc_insertion_point(interface_extends:org.nio.test.MethodInvokeResponse)
    com.google.protobuf.MessageOrBuilder {

  /**
   * <code>int32 requestId = 1;</code>
   * @return The requestId.
   */
  int getRequestId();

  /**
   * <code>string clazz = 2;</code>
   * @return The clazz.
   */
  java.lang.String getClazz();
  /**
   * <code>string clazz = 2;</code>
   * @return The bytes for clazz.
   */
  com.google.protobuf.ByteString
      getClazzBytes();

  /**
   * <code>string method = 3;</code>
   * @return The method.
   */
  java.lang.String getMethod();
  /**
   * <code>string method = 3;</code>
   * @return The bytes for method.
   */
  com.google.protobuf.ByteString
      getMethodBytes();

  /**
   * <code>repeated string args = 4;</code>
   * @return A list containing the args.
   */
  java.util.List<java.lang.String>
      getArgsList();
  /**
   * <code>repeated string args = 4;</code>
   * @return The count of args.
   */
  int getArgsCount();
  /**
   * <code>repeated string args = 4;</code>
   * @param index The index of the element to return.
   * @return The args at the given index.
   */
  java.lang.String getArgs(int index);
  /**
   * <code>repeated string args = 4;</code>
   * @param index The index of the value to return.
   * @return The bytes of the args at the given index.
   */
  com.google.protobuf.ByteString
      getArgsBytes(int index);

  /**
   * <code>bytes result = 5;</code>
   * @return The result.
   */
  com.google.protobuf.ByteString getResult();

  /**
   * <code>.google.protobuf.Timestamp time = 6;</code>
   * @return Whether the time field is set.
   */
  boolean hasTime();
  /**
   * <code>.google.protobuf.Timestamp time = 6;</code>
   * @return The time.
   */
  com.google.protobuf.Timestamp getTime();
  /**
   * <code>.google.protobuf.Timestamp time = 6;</code>
   */
  com.google.protobuf.TimestampOrBuilder getTimeOrBuilder();
}
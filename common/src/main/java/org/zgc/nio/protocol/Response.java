// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: response.proto

package org.zgc.nio.protocol;

public final class Response {
  private Response() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_org_nio_test_MethodInvokeResponse_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_org_nio_test_MethodInvokeResponse_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\016response.proto\022\014org.nio.test\032\037google/p" +
      "rotobuf/timestamp.proto\"\220\001\n\024MethodInvoke" +
      "Response\022\021\n\trequestId\030\001 \001(\005\022\r\n\005clazz\030\002 \001" +
      "(\t\022\016\n\006method\030\003 \001(\t\022\014\n\004args\030\004 \003(\t\022\016\n\006resu" +
      "lt\030\005 \001(\014\022(\n\004time\030\006 \001(\0132\032.google.protobuf" +
      ".TimestampB\030\n\024org.zgc.nio.protocolP\001b\006pr" +
      "oto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          com.google.protobuf.TimestampProto.getDescriptor(),
        });
    internal_static_org_nio_test_MethodInvokeResponse_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_org_nio_test_MethodInvokeResponse_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_org_nio_test_MethodInvokeResponse_descriptor,
        new java.lang.String[] { "RequestId", "Clazz", "Method", "Args", "Result", "Time", });
    com.google.protobuf.TimestampProto.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}
package org.zgc.nio.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.ProtocolStringList;
import com.google.protobuf.Timestamp;
import org.zgc.nio.protocol.MethodInvokeRequest;
import org.zgc.nio.protocol.MethodInvokeResponse;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;

/**
 * @author lucheng
 * @since 2022/8/24
 */
public class Service {

    public static MethodInvokeResponse handlerRequest(MethodInvokeRequest request) {
        ProtocolStringList argsList = request.getArgsList();
        String result = null;
        try {
            Class<?> clazz = Class.forName(request.getClazz());
            MethodType methodType = MethodType.methodType(String.class, String[].class);
            MethodHandle methodHandle = MethodHandles.lookup().findStatic(clazz, request.getMethod(), methodType);
            result = (String) methodHandle.invoke(argsList.toArray(new String[0]));
        } catch (Throwable e) {
            result = e.toString();
        }
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(System.currentTimeMillis() / 1000).build();
        return MethodInvokeResponse.newBuilder()
                .addAllArgs(argsList)
                .setClazz(request.getClazz())
                .setMethod(request.getMethod())
                .setRequestId(request.getRequestId())
                .setResult(ByteString.copyFrom(result, StandardCharsets.UTF_8))
                .setTime(timestamp)
                .build();
    }

    public static String join(String[] args) {
        return String.join(",", args);
    }
}

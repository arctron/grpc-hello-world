package com.example.grpc.client;

import java.util.concurrent.TimeUnit;

import com.example.grpc.HelloWorldRequest;
import com.example.grpc.HelloWorldResponse;
import com.example.grpc.HelloWorldServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloWorldClient {
    public static void main(String[] args) throws Exception {
        // Access a service running on the local machine on port 50051
        String target = "localhost:50051";

        // Create a communication channel to the server, known as a Channel. Channels
        // are thread-safe
        // and reusable. It is common to create channels at the beginning of your
        // application and reuse
        // them until the application shuts down.
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS
                // to avoid
                // needing certificates.
                .usePlaintext()
                .build();

        HelloWorldServiceGrpc.HelloWorldServiceBlockingStub blockingStub = HelloWorldServiceGrpc
                .newBlockingStub(channel);
        HelloWorldRequest request = HelloWorldRequest.newBuilder().build();

        HelloWorldResponse response;
        try {
            response = blockingStub.helloWorld(request);
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {0}", e.getStatus());
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
            return;
        }
        log.info("Response received form the server: " + response.getResponse());

        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
}

package com.example.grpc.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.example.grpc.HelloWorldRequest;
import com.example.grpc.HelloWorldResponse;
import com.example.grpc.HelloWorldServiceGrpc;
import com.example.grpc.HelloWorldServiceGrpc.HelloWorldServiceBlockingStub;
import com.example.grpc.HelloWorldServiceGrpc.HelloWorldServiceFutureStub;
import com.example.grpc.HelloWorldServiceGrpc.HelloWorldServiceStub;
import com.google.common.util.concurrent.ListenableFuture;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
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

        blockingRequest(channel);
        asyncRequest(channel);
        futureRequest(channel);
        channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }

    public static void blockingRequest(ManagedChannel channel) {
        HelloWorldServiceBlockingStub blockingStub = HelloWorldServiceGrpc
                .newBlockingStub(channel);
        HelloWorldRequest request = HelloWorldRequest.newBuilder().build();

        HelloWorldResponse response;

        try {
            response = blockingStub.helloWorld(request);
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {0}", e.getStatus());
            return;
        }
        log.info("(BS) Response received from server: {}", response.getResponse());
    }

    public static void asyncRequest(ManagedChannel channel) {
        HelloWorldServiceStub asyncStub = HelloWorldServiceGrpc.newStub(channel);
        HelloWorldRequest request = HelloWorldRequest.newBuilder().build();

        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<HelloWorldResponse> responseObserver = new StreamObserver<HelloWorldResponse>() {
            @Override
            public void onNext(HelloWorldResponse response) {
                log.info("(AS) Response received from server: {}", response.getResponse());
            }

            @Override
            public void onError(Throwable t) {
                Status status = Status.fromThrowable(t);
                log.error("RPC Failed: {0}", status);
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                log.info("Finished RPC");
                finishLatch.countDown();
            }
        };

        asyncStub.helloWorld(request, responseObserver);

        try {
            finishLatch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.error("Thread interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    public static void futureRequest(ManagedChannel channel) {
        HelloWorldServiceFutureStub futureStub = HelloWorldServiceGrpc.newFutureStub(channel);
        HelloWorldRequest request = HelloWorldRequest.newBuilder().build();

        ListenableFuture<HelloWorldResponse> futureResponse = futureStub.helloWorld(request);

        try {
            HelloWorldResponse response = futureResponse.get(1, TimeUnit.MINUTES);
            log.info("(LF) Response received from server: {}", response.getResponse());
        } catch (InterruptedException e) {
            log.error("Exception occured while making RPC call", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException | TimeoutException e) {
            log.error("Exception occured while making RPC call", e);
        }
    }
}

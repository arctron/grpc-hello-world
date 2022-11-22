package com.example.grpc.client;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

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
    private ManagedChannel channel;
    private HelloWorldServiceBlockingStub blockingStub;
    private HelloWorldServiceStub asyncStub;
    private HelloWorldServiceFutureStub futureStub;

    private HelloWorldRequest request;

    public HelloWorldClient(ManagedChannel channel) {
        blockingStub = HelloWorldServiceGrpc.newBlockingStub(channel);

        asyncStub = HelloWorldServiceGrpc.newStub(channel);

        futureStub = HelloWorldServiceGrpc.newFutureStub(channel);

        request = HelloWorldRequest.newBuilder().build();
    }

    public HelloWorldClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext() // remove this if SSL/TLS is enabled
                .build());
    }

    public void shutdown() {
        log.info("Shutting down gRPC client");
        try {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error("Thread interrupted", e);
            Thread.currentThread().interrupt();
        }
    }

    public Optional<HelloWorldResponse> blockingRequest() {

        try {
            HelloWorldResponse response = blockingStub.helloWorld(request);
            log.info("(BS) Response received from server: {}", response.getResponse());
            return Optional.of(response);
        } catch (StatusRuntimeException e) {
            log.error("RPC failed: {0}", e.getStatus());
            return Optional.empty();
        }
    }

    public Optional<HelloWorldResponse> asyncRequest() {
        AtomicReference<HelloWorldResponse> responseAtomicReference = new AtomicReference<>();
        final CountDownLatch finishLatch = new CountDownLatch(1);
        StreamObserver<HelloWorldResponse> responseObserver = new StreamObserver<HelloWorldResponse>() {
            @Override
            public void onNext(HelloWorldResponse response) {
                responseAtomicReference.set(response);
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

        boolean countZero = false;

        try {
            countZero = finishLatch.await(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.error("Thread interrupted", e);
            Thread.currentThread().interrupt();
        }

        if (!countZero) {
            return Optional.empty();
        }

        return Optional.ofNullable(responseAtomicReference.get());
    }

    public Optional<HelloWorldResponse> futureRequest() {
        ListenableFuture<HelloWorldResponse> futureResponse = futureStub.helloWorld(request);

        try {
            HelloWorldResponse response = futureResponse.get(1, TimeUnit.MINUTES);
            log.info("(LF) Response received from server: {}", response.getResponse());
            return Optional.of(response);
        } catch (InterruptedException e) {
            log.error("Exception occured while making RPC call", e);
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (ExecutionException | TimeoutException e) {
            log.error("Exception occured while making RPC call", e);
            return Optional.empty();
        }
    }
}

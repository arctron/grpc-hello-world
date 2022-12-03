package com.example.grpc.server;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.example.grpc.HelloWorldRequest;
import com.example.grpc.HelloWorldResponse;
import com.example.grpc.HelloWorldServiceGrpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HelloWorldServer implements Runnable {
    private Server server;

    public HelloWorldServer(int port) {
        log.info("Building server on port {}", port);
        server = ServerBuilder.forPort(port)
                .addService(new HelloWorldService())
                .build();
    }

    public void shutdown() {
        log.info("Shutting down gRPC server");
        if (server != null) {
            try {
                server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("InterruptedException ", e);
                Thread.currentThread().interrupt();
            }
        }
    }

    static class HelloWorldService extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {

        @Override
        public void helloWorld(HelloWorldRequest request, StreamObserver<HelloWorldResponse> responseObserver) {
            HelloWorldResponse response = HelloWorldResponse.newBuilder().setResponse("Hello World!").build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void run() {
        try {
            server.start();
            log.info("Server started, listening on " + server.getPort());
        } catch (IOException e) {
            log.error("IOException ", e);
        }
        try {
            server.awaitTermination();
        } catch (InterruptedException e) {
            log.error("InterruptedException ", e);
            Thread.currentThread().interrupt();
        }
    }
}

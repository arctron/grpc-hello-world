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
public class HelloWorldServer {
    private Server server;

    private void start() throws IOException {
        int port = 50051;

        server = ServerBuilder.forPort(port)
                .addService(new HelloWorldService())
                .build()
                .start();

        log.info("Server started, listening on " + port);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                log.info("Shutting down gRPC server since JVM is shutting down");
                try {
                    HelloWorldServer.this.stop();
                } catch (InterruptedException e) {
                    log.error("InterruptedException occcured", e);
                    Thread.currentThread().interrupt();
                }
                log.info("Server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final HelloWorldServer server = new HelloWorldServer();
        server.start();
        server.blockUntilShutdown();
    }

    private static class HelloWorldService extends HelloWorldServiceGrpc.HelloWorldServiceImplBase {

        @Override
        public void helloWorld(HelloWorldRequest request, StreamObserver<HelloWorldResponse> responseObserver) {
            HelloWorldResponse response = HelloWorldResponse.newBuilder().setResponse("Hello World!").build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}

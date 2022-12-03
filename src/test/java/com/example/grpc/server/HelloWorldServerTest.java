package com.example.grpc.server;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.grpc.HelloWorldRequest;
import com.example.grpc.HelloWorldResponse;
import com.example.grpc.HelloWorldServiceGrpc;
import com.example.grpc.server.HelloWorldServer.HelloWorldService;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;

@RunWith(SpringRunner.class)
public class HelloWorldServerTest {
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    @Test
    public void testServer() throws IOException {
        String serverName = InProcessServerBuilder.generateName();

        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(new HelloWorldService()).build().start());

        HelloWorldServiceGrpc.HelloWorldServiceBlockingStub blockingStub = HelloWorldServiceGrpc.newBlockingStub(
                grpcCleanup.register(InProcessChannelBuilder.forName(serverName).directExecutor().build()));

        HelloWorldResponse response = blockingStub.helloWorld(HelloWorldRequest.newBuilder().build());

        assertEquals("Hello World!", response.getResponse());
    }
}

package com.example.grpc.client;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.grpc.HelloWorldRequest;
import com.example.grpc.HelloWorldResponse;
import com.example.grpc.HelloWorldServiceGrpc.HelloWorldServiceImplBase;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;

@RunWith(SpringRunner.class)
public class HelloWorldClientTest {
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    // implement the fake service
    private final HelloWorldServiceImplBase serviceImpl = new HelloWorldServiceImplBase() {
        @Override
        public void helloWorld(HelloWorldRequest request, StreamObserver<HelloWorldResponse> responseObserver) {
            HelloWorldResponse response = HelloWorldResponse.newBuilder().setResponse("Hello World!").build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    };

    private HelloWorldClient client;

    @Before
    public void setUp() throws Exception {

        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful
        // shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(serviceImpl).build().start());

        // Create a client channel and register for automatic graceful shutdown.
        ManagedChannel channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());

        // Create a HelloWorldClient using the in-process channel;
        client = new HelloWorldClient(channel);
    }

    /**
     * To test the client, call from the client against the fake server, and verify
     * behaviors or state
     * changes from the server side.
     */
    @Test
    public void testClient() {
        assertTrue(client.blockingRequest().isPresent());
        assertEquals("Hello World!", client.blockingRequest().get().getResponse());

        assertTrue(client.blockingRequest().isPresent());
        assertEquals("Hello World!", client.asyncRequest().get().getResponse());

        assertTrue(client.blockingRequest().isPresent());
        assertEquals("Hello World!", client.futureRequest().get().getResponse());
    }
}

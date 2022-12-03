package com.example.grpc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import com.example.grpc.client.HelloWorldClient;
import com.example.grpc.server.HelloWorldServer;

@Configuration
public class GrpcApplicationConfig {
    @Value("${grpc.host}")
    private String grpcHost;

    @Value("${grpc.port}")
    private int grpcPort;

    @Bean
    public TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }

    @Bean
    public HelloWorldServer helloWorldServer() {
        return new HelloWorldServer(grpcPort);
    }

    @Bean
    public HelloWorldClient helloWorldClient() {
        return new HelloWorldClient(grpcHost, grpcPort);
    }
}

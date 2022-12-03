package com.example.grpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.task.TaskExecutor;

import com.example.grpc.client.HelloWorldClient;
import com.example.grpc.server.HelloWorldServer;

@SpringBootApplication
public class GrpcApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext applicationContext = SpringApplication.run(GrpcApplication.class, args);
		HelloWorldClient client = applicationContext.getBean(HelloWorldClient.class);
		HelloWorldServer server = applicationContext.getBean(HelloWorldServer.class);

		TaskExecutor taskExecutor = applicationContext.getBean(TaskExecutor.class);
		taskExecutor.execute(server);

		client.blockingRequest();
		client.asyncRequest();
		client.futureRequest();
	}

}

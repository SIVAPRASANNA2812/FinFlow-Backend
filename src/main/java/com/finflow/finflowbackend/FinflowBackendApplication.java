package com.finflow.finflowbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FinflowBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinflowBackendApplication.class, args);
    }

}

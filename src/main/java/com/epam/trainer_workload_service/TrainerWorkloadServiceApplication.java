package com.epam.trainer_workload_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.jms.annotation.EnableJms;

@EnableJms
@SpringBootApplication(scanBasePackages = "com.epam.trainer_workload_service")
@EntityScan(basePackages = "com.epam.trainer_workload_service.entity")
public class TrainerWorkloadServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainerWorkloadServiceApplication.class, args);
    }

}

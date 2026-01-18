package com.epam.trainer_workload_service.cucumber;

import com.epam.trainer_workload_service.dlq.TrainingWorkloadDlqProducer;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.activemq.autoconfigure.ActiveMQAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jms.autoconfigure.JmsAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@CucumberContextConfiguration
@ActiveProfiles("test")
@SpringBootTest
@EnableAutoConfiguration(exclude = {
        JmsAutoConfiguration.class,
        ActiveMQAutoConfiguration.class
})
public class CucumberSpringConfiguration {

    @MockitoBean
    private TrainingWorkloadDlqProducer trainingWorkloadDlqProducer;

    @MockitoBean
    private jakarta.jms.ConnectionFactory connectionFactory;

    @MockitoBean
    private org.springframework.jms.config.JmsListenerContainerFactory<?> jmsListenerContainerFactory;
}




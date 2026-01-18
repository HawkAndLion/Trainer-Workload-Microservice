package com.epam.trainer_workload_service.cucumber;

import com.epam.trainer_workload_service.dlq.TrainingWorkloadDlqProducer;
import io.cucumber.spring.CucumberContextConfiguration;
import jakarta.jms.ConnectionFactory;
import org.springframework.boot.activemq.autoconfigure.ActiveMQAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jms.autoconfigure.JmsAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.config.JmsListenerContainerFactory;
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
    private ConnectionFactory connectionFactory;

    @MockitoBean
    private JmsListenerContainerFactory<?> jmsListenerContainerFactory;
}




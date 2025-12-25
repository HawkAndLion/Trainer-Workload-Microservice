package com.epam.trainer_workload_service.listener;

import com.epam.trainer_workload_service.dto.TrainingEventDto;
import com.epam.trainer_workload_service.security.JwtTokenProvider;
import com.epam.trainer_workload_service.service.WorkloadService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TrainingWorkloadEventListener {
    private static final Logger log =
            LoggerFactory.getLogger(TrainingWorkloadEventListener.class);
    private static final String RECEIVED_TRAINING_EVENT = "RECEIVED training event: {}";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final Integer SEVEN = 7;
    private static final String INVALID_JWT_TOKEN = "Invalid JWT token";
    private static final String UNAUTHORIZED_MESSAGE = "Unauthorized service: ";
    private static final String MISSING_HEADER = "Missing Authorization header";

    private final WorkloadService workloadService;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${app.service.name}")
    private String serviceName;

    @JmsListener(
            destination = "trainer.workload.queue",
            containerFactory = "jmsListenerContainerFactory"
    )
    @Transactional
    public void handleTrainingEvent(TrainingEventDto dto, Message message) throws JMSException {
        log.info(RECEIVED_TRAINING_EVENT, dto);

        String authHeader = message.getStringProperty(AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith(BEARER)) {
            String token = authHeader.substring(SEVEN);

            if (!jwtTokenProvider.validateToken(token)) {
                throw new SecurityException(INVALID_JWT_TOKEN);
            }

            String subject = jwtTokenProvider.getUsernameFromToken(token);

            if (!serviceName.equals(subject)) {
                throw new SecurityException(UNAUTHORIZED_MESSAGE + subject);
            }

            workloadService.processTrainerEvent(dto);
        } else {
            throw new SecurityException(MISSING_HEADER);
        }
    }
}

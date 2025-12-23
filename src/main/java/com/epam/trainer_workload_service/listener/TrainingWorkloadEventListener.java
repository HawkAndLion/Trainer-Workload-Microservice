package com.epam.trainer_workload_service.listener;

import com.epam.trainer_workload_service.dto.TrainingEventDto;
import com.epam.trainer_workload_service.service.WorkloadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class TrainingWorkloadEventListener {
    private static final Logger log =
            LoggerFactory.getLogger(TrainingWorkloadEventListener.class);
    private static final String RECEIVED_TRAINING_EVENT = "RECEIVED training event: {}";

    private final WorkloadService workloadService;

    @JmsListener(
            destination = "trainer.workload.queue",
            containerFactory = "jmsListenerContainerFactory"
    )
    @Transactional
    public void handleTrainingEvent(TrainingEventDto dto) {
        log.info(RECEIVED_TRAINING_EVENT, dto);

        workloadService.processTrainerEvent(dto);
    }
}

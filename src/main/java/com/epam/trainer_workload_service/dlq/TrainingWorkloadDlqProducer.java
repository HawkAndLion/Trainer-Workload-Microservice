package com.epam.trainer_workload_service.dlq;

import com.epam.trainer_workload_service.dto.TrainingEventDto;
import com.epam.trainer_workload_service.dto.TrainingEventErrorDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TrainingWorkloadDlqProducer {

    private final JmsTemplate jmsTemplate;

    public void sendToDlq(String dlq, TrainingEventDto dto, Exception exception) {
        TrainingEventErrorDto errorDto = new TrainingEventErrorDto(
                dto != null ? dto.getTrainingId() : null,
                exception.getClass().getSimpleName(),
                exception.getMessage()
        );

        jmsTemplate.convertAndSend(dlq, errorDto);
    }
}

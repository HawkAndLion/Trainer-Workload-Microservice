package com.epam.trainer_workload_service.dlq;

import com.epam.trainer_workload_service.dto.TrainingEventDto;
import com.epam.trainer_workload_service.dto.TrainingEventErrorDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jms.core.JmsTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class TrainingWorkloadDlqProducerTest {
    private static final String ERROR_MESSAGE = "Something went wrong";
    private static final String DLQ = "dlq";

    private JmsTemplate jmsTemplate;
    private TrainingWorkloadDlqProducer producer;

    @BeforeEach
    void setUp() {
        jmsTemplate = mock(JmsTemplate.class);
        producer = new TrainingWorkloadDlqProducer(jmsTemplate);
    }

    @Test
    void shouldSendCorrectErrorDtoWhenSendToDlq() {
        // Given
        TrainingEventDto dto = new TrainingEventDto();
        dto.setTrainingId(42L);
        Exception exception = new IllegalStateException(ERROR_MESSAGE);

        // When
        producer.sendToDlq(DLQ, dto, exception);

        // Then
        ArgumentCaptor<TrainingEventErrorDto> captor = ArgumentCaptor.forClass(TrainingEventErrorDto.class);
        verify(jmsTemplate, times(1)).convertAndSend(eq(DLQ), captor.capture());

        TrainingEventErrorDto sentDto = captor.getValue();
        assertEquals(42L, sentDto.getTrainingId());
        assertEquals(ERROR_MESSAGE, sentDto.getErrorMessage());
    }

    @Test
    void shouldHandleNullDto() {
        // Given
        JmsTemplate jmsTemplate = mock(JmsTemplate.class);
        TrainingWorkloadDlqProducer producer = new TrainingWorkloadDlqProducer(jmsTemplate);

        Exception ex = new RuntimeException(ERROR_MESSAGE);

        // When
        producer.sendToDlq(DLQ, null, ex);

        // Then
        ArgumentCaptor<TrainingEventErrorDto> captor = ArgumentCaptor.forClass(TrainingEventErrorDto.class);
        verify(jmsTemplate).convertAndSend(eq(DLQ), captor.capture());

        TrainingEventErrorDto sentDto = captor.getValue();
        assertNull(sentDto.getTrainingId());
        assertEquals(ERROR_MESSAGE, sentDto.getErrorMessage());
    }
}
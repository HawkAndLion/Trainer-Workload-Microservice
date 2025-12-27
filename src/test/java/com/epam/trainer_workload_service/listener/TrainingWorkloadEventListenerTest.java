package com.epam.trainer_workload_service.listener;

import com.epam.trainer_workload_service.dlq.TrainingWorkloadDlqProducer;
import com.epam.trainer_workload_service.dto.ActionType;
import com.epam.trainer_workload_service.dto.TrainingEventDto;
import com.epam.trainer_workload_service.security.JwtTokenProvider;
import com.epam.trainer_workload_service.service.WorkloadService;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingWorkloadEventListenerTest {

    @Mock
    private WorkloadService workloadService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private TrainingWorkloadDlqProducer dlqProducer;

    @InjectMocks
    private TrainingWorkloadEventListener listener;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(listener, "serviceName", "trainer-workload-service");
    }

    @Test
    void shouldProcessTrainingEventWhenJwtIsValid() throws JMSException {
        // Given
        TrainingEventDto dto = new TrainingEventDto();
        dto.setTrainingId(1L);
        dto.setUsername("trainer1");
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setActive(true);
        dto.setTrainingDate(java.time.LocalDate.of(2025, 5, 10));
        dto.setDurationMinutes(60);
        dto.setActionType(ActionType.ADD);

        Message message = mock(Message.class);
        when(message.getStringProperty("Authorization")).thenReturn("Bearer valid-token");
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken("valid-token")).thenReturn("trainer-workload-service");

        // When
        listener.handleTrainingEvent(dto, message);

        // Then
        verify(workloadService, times(1))
                .processTrainerEvent(dto);
        verify(message).getStringProperty("Authorization");
        verify(dlqProducer, never())
                .sendToDlq(anyString(), any(), any());
        verify(jwtTokenProvider).validateToken("valid-token");
        verify(jwtTokenProvider).getUsernameFromToken("valid-token");
        assertDoesNotThrow(() ->
                listener.handleTrainingEvent(dto, message)
        );
        assertEquals(
                "trainer-workload-service",
                ReflectionTestUtils.getField(listener, "serviceName")
        );
    }

    @Test
    void shouldSendToDlqWhenJwtIsInvalid() throws JMSException {
        // Given
        TrainingEventDto dto = new TrainingEventDto();

        Message message = mock(Message.class);
        when(message.getStringProperty("Authorization"))
                .thenReturn("Bearer invalid-token");

        when(jwtTokenProvider.validateToken("invalid-token"))
                .thenReturn(false);

        // When
        listener.handleTrainingEvent(dto, message);

        // Then
        verify(workloadService, never())
                .processTrainerEvent(any());

        ArgumentCaptor<Exception> exceptionCaptor =
                ArgumentCaptor.forClass(Exception.class);
        verify(dlqProducer).sendToDlq(
                isNull(),
                eq(dto),
                exceptionCaptor.capture()
        );

        Exception ex = exceptionCaptor.getValue();
        assertTrue(ex instanceof SecurityException);
        assertEquals("Invalid JWT token", ex.getMessage());
    }

    @Test
    void shouldSendToDlqWhenAuthorizationHeaderIsMissing() throws JMSException {
        // Given
        TrainingEventDto dto = new TrainingEventDto();

        Message message = mock(Message.class);
        when(message.getStringProperty("Authorization"))
                .thenReturn(null);

        // When
        listener.handleTrainingEvent(dto, message);

        // Then
        ArgumentCaptor<SecurityException> exceptionCaptor =
                ArgumentCaptor.forClass(SecurityException.class);
        verify(dlqProducer, times(1)).sendToDlq(
                isNull(),
                eq(dto),
                exceptionCaptor.capture()
        );
        verify(workloadService, never()).processTrainerEvent(any());
        Exception ex = exceptionCaptor.getValue();
        assertEquals("Missing Authorization header", ex.getMessage());
    }
}
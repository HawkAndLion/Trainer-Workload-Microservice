package com.epam.trainer_workload_service.cucumber;

import com.epam.trainer_workload_service.dlq.TrainingWorkloadDlqProducer;
import com.epam.trainer_workload_service.dto.TrainingEventDto;
import com.epam.trainer_workload_service.listener.TrainingWorkloadEventListener;
import com.epam.trainer_workload_service.security.JwtTokenProvider;
import com.epam.trainer_workload_service.service.WorkloadService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.jms.Message;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

public class JwtEventListenerSteps {

    private final WorkloadService workloadService = mock(WorkloadService.class);
    private final JwtTokenProvider jwtTokenProvider = mock(JwtTokenProvider.class);
    private final TrainingWorkloadDlqProducer dlqProducer = mock(TrainingWorkloadDlqProducer.class);
    private final Message message = mock(Message.class);

    private TrainingWorkloadEventListener listener;
    private TrainingEventDto dto;

    @Given("a valid JWT token {string} from service {string}")
    public void validJwt(String token, String service) throws Exception {
        listener = new TrainingWorkloadEventListener(workloadService, jwtTokenProvider, dlqProducer);
        ReflectionTestUtils.setField(listener, "serviceName", "trainer-workload-service");
        ReflectionTestUtils.setField(listener, "dlq", "test-dlq");

        when(message.getStringProperty("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(service);
    }

    @Given("an invalid JWT token {string}")
    public void invalidJwt(String token) throws Exception {
        validJwt(token, "trainer-workload-service");
        when(jwtTokenProvider.validateToken(token)).thenReturn(false);
    }

    @Given("a missing Authorization header")
    public void missingAuthorization() throws Exception {
        listener = new TrainingWorkloadEventListener(workloadService, jwtTokenProvider, dlqProducer);
        ReflectionTestUtils.setField(listener, "dlq", "test-dlq");
        when(message.getStringProperty("Authorization")).thenReturn(null);
    }

    @Given("a valid JWT token from service {string}")
    public void validJwtFromService(String service) throws Exception {
        String token = "valid-token";

        listener = new TrainingWorkloadEventListener(workloadService, jwtTokenProvider, dlqProducer);
        ReflectionTestUtils.setField(listener, "serviceName", "trainer-workload-service");
        ReflectionTestUtils.setField(listener, "dlq", "test-dlq");

        when(message.getStringProperty("Authorization")).thenReturn("Bearer " + token);
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);
        when(jwtTokenProvider.getUsernameFromToken(token)).thenReturn(service);
    }

    @When("a training event with ID {long} is received")
    public void trainingEventReceived(Long id) {
        dto = new TrainingEventDto();
        dto.setTrainingId(id);

        try {
            listener.handleTrainingEvent(dto, message);
        } catch (Exception ex) {
        }
    }

    @When("a training event is received")
    public void trainingEventReceivedNoId() {
        trainingEventReceived(null);
    }

    @Then("the event should be processed")
    public void eventProcessed() {
        verify(workloadService).processTrainerEvent(dto);
        verify(dlqProducer, never()).sendToDlq(any(), any(), any());
    }

    @Then("the event should not be processed")
    public void eventNotProcessed() {
        verify(workloadService, never()).processTrainerEvent(any());
    }

    @Then("no message should be sent to DLQ")
    public void noDlq() {
        verify(dlqProducer, never()).sendToDlq(any(), any(), any());
    }

    @Then("a SecurityException should be sent to DLQ with message {string}")
    public void securityExceptionSent(String messageText) {
        verify(dlqProducer).sendToDlq(
                eq("test-dlq"),
                eq(dto),
                argThat(ex ->
                        ex instanceof SecurityException &&
                                ex.getMessage().equals(messageText)
                )
        );
    }
}


package com.epam.trainer_workload_service.cucumber;

import com.epam.trainer_workload_service.dlq.TrainingWorkloadDlqProducer;
import com.epam.trainer_workload_service.dto.TrainingEventDto;
import com.epam.trainer_workload_service.dto.TrainingEventErrorDto;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;
import org.springframework.jms.core.JmsTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DlqProducerSteps {

    private TrainingEventDto event;
    private Exception exception;

    private JmsTemplate jmsTemplate;
    private TrainingWorkloadDlqProducer dlqProducer;

    private TrainingEventErrorDto capturedDto;

    @Given("a training event with ID {long} and error message {string}")
    public void trainingEvent(Long id, String message) {
        event = new TrainingEventDto();
        event.setTrainingId(id);
        exception = new RuntimeException(message);

        jmsTemplate = mock(JmsTemplate.class);
        dlqProducer = new TrainingWorkloadDlqProducer(jmsTemplate);
    }

    @Given("a null training event and error message {string}")
    public void nullTrainingEvent(String message) {
        event = null;
        exception = new RuntimeException(message);

        jmsTemplate = mock(JmsTemplate.class);
        dlqProducer = new TrainingWorkloadDlqProducer(jmsTemplate);
    }

    @When("sending to DLQ")
    public void sendingToDlq() {
        ArgumentCaptor<TrainingEventErrorDto> captor =
                ArgumentCaptor.forClass(TrainingEventErrorDto.class);

        dlqProducer.sendToDlq("test-dlq", event, exception);

        verify(jmsTemplate).convertAndSend(eq("test-dlq"), captor.capture());
        capturedDto = captor.getValue();
    }

    @Then("an error message should be published to DLQ")
    public void verifyErrorDto() {
        assertNotNull(capturedDto);
        assertEquals(exception.getMessage(), capturedDto.getErrorMessage());

        if (event != null) {
            assertEquals(event.getTrainingId(), capturedDto.getTrainingId());
        } else {
            assertNull(capturedDto.getTrainingId());
        }
    }
}

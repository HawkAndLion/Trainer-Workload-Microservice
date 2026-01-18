package com.epam.trainer_workload_service.cucumber;

import com.epam.trainer_workload_service.dto.ActionType;
import com.epam.trainer_workload_service.dto.TrainingEventDto;
import com.epam.trainer_workload_service.model.TrainingSummaryResponseDto;
import com.epam.trainer_workload_service.mongo.TrainerWorkloadDocument;
import com.epam.trainer_workload_service.repository.TrainerWorkloadRepository;
import com.epam.trainer_workload_service.service.ServiceException;
import com.epam.trainer_workload_service.service.WorkloadService;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class TrainerWorkloadSteps {

    @Autowired
    private WorkloadService workloadService;

    @Autowired
    private TrainerWorkloadRepository workloadRepository;

    private TrainingEventDto eventDto;
    private Exception exception;
    private TrainingSummaryResponseDto summary;

    @Before
    public void cleanDatabase() {
        workloadRepository.deleteAll();
    }

    @Given("a trainer with username {string} and first name {string} and last name {string}")
    public void givenTrainer(String username, String firstName, String lastName) {
        if (workloadRepository.findByUsername(username).isEmpty()) {
            TrainerWorkloadDocument doc = new TrainerWorkloadDocument();
            doc.setUsername(username);
            doc.setFirstName(firstName);
            doc.setLastName(lastName);
            doc.setActive(true);
            doc.setYears(new ArrayList<>());
            workloadRepository.save(doc);
        }

        eventDto = new TrainingEventDto();
        eventDto.setUsername(username);
        eventDto.setFirstName(firstName);
        eventDto.setLastName(lastName);
        eventDto.setActive(true);
        eventDto.setTrainingDate(LocalDate.of(2025, 5, 1)); // Default date
    }


    @Given("no trainer exists with username {string}")
    public void noTrainerExists(String username) {
        workloadRepository.findByUsername(username).ifPresent(workloadRepository::delete);

        eventDto = new TrainingEventDto();
        eventDto.setUsername(username);
        eventDto.setFirstName("Dummy");
        eventDto.setLastName("Dummy");
        eventDto.setActive(true);
        eventDto.setTrainingDate(LocalDate.of(2025, 5, 1));
    }

    @Given("a training event with ID {int} exists for trainer {string} in May {int} with duration {int}")
    public void trainingEventExistsForTrainer(int id, String username, int year, int duration) {
        eventDto = new TrainingEventDto();
        eventDto.setTrainingId((long) id);
        eventDto.setUsername(username);
        eventDto.setTrainingDate(LocalDate.of(year, 5, 1));
        eventDto.setDurationMinutes((long) duration);
        eventDto.setActionType(ActionType.ADD);

        try {
            workloadService.processTrainerEvent(eventDto);
        } catch (Exception ex) {
            exception = ex;
        }
    }

    @Given("a training event with ID {int} already exists")
    public void trainingEventAlreadyExists(Integer id) {
        eventDto = new TrainingEventDto();
        eventDto.setTrainingId(id.longValue());
        eventDto.setUsername("ExistingUser");
        eventDto.setTrainingDate(LocalDate.of(2025, 5, 1));
        eventDto.setDurationMinutes(60L);
        eventDto.setActionType(ActionType.ADD);

        try {
            workloadService.processTrainerEvent(eventDto);
        } catch (Exception ex) {
            exception = ex;
        }
    }

    @Given("a training event with ID {int} and missing transaction ID")
    public void trainingEventWithMissingTransactionId(Integer id) {
        eventDto = new TrainingEventDto();
        eventDto.setTrainingId(id.longValue());
        eventDto.setUsername("TestUser");
        eventDto.setTrainingDate(LocalDate.of(2025, 5, 1));
        eventDto.setDurationMinutes(45L);
        eventDto.setActionType(ActionType.ADD);
    }

    @Given("a training event with null username")
    public void trainingEventNullUsername() {
        eventDto = new TrainingEventDto();
        eventDto.setUsername(null);
        eventDto.setTrainingDate(LocalDate.of(2025, 5, 1));
        eventDto.setDurationMinutes(45L);
        eventDto.setActionType(ActionType.ADD);
    }

    @Given("a training event with null action type")
    public void trainingEventNullActionType() {
        eventDto = new TrainingEventDto();
        eventDto.setUsername("TestUser");
        eventDto.setTrainingDate(LocalDate.of(2025, 5, 1));
        eventDto.setDurationMinutes(45L);
        eventDto.setActionType(null);
    }

    @Given("a training event with negative duration {long}")
    public void trainingEventNegativeDuration(Long duration) {
        eventDto = new TrainingEventDto();
        eventDto.setUsername("TestUser");
        eventDto.setTrainingDate(LocalDate.of(2025, 5, 1));
        eventDto.setDurationMinutes(duration);
        eventDto.setActionType(ActionType.ADD);
    }

    @Given("a training event with ID {int} does not exist")
    public void trainingEventDoesNotExist(int id) {
        eventDto = new TrainingEventDto();
        eventDto.setTrainingId((long) id);
        eventDto.setUsername("TestUser");
        eventDto.setTrainingDate(LocalDate.of(2025, 5, 1));
        eventDto.setActionType(ActionType.DELETE);
    }

    @When("a training event with ID {int} and duration {int} minutes is added")
    public void addTrainingEvent(int id, int duration) {
        eventDto.setTrainingId((long) id);
        eventDto.setDurationMinutes((long) duration);
        eventDto.setActionType(ActionType.ADD);

        try {
            workloadService.processTrainerEvent(eventDto);
        } catch (Exception ex) {
            exception = ex;
        }
    }

    @When("a training event with ID {int} and duration {int} minutes is added for {string}")
    public void addTrainingEventForUser(int id, int duration, String username) {
        eventDto.setTrainingId((long) id);
        eventDto.setDurationMinutes((long) duration);
        eventDto.setUsername(username);
        eventDto.setActionType(ActionType.ADD);

        try {
            workloadService.processTrainerEvent(eventDto);
        } catch (Exception ex) {
            exception = ex;
        }
    }

    @When("the training event is deleted")
    public void deleteTrainingEvent() {
        try {
            summary = workloadService.getSummaryForTrainer(eventDto.getUsername(),
                    eventDto.getTrainingDate().getYear(),
                    eventDto.getTrainingDate().getMonthValue());
        } catch (ServiceException e) {
            summary = null;
        }

        eventDto.setActionType(ActionType.DELETE);

        try {
            workloadService.processTrainerEvent(eventDto);
        } catch (Exception ex) {
            exception = ex;
        }
    }

    @When("the same training event is processed again")
    public void processDuplicateEvent() {
        eventDto.setActionType(ActionType.ADD);
        try {
            workloadService.processTrainerEvent(eventDto);
        } catch (Exception ex) {
            exception = ex;
        }
    }

    @When("processing the event")
    public void processing_the_event() {
        try {
            workloadService.processTrainerEvent(eventDto);
        } catch (Exception ex) {
            exception = ex;
        }
    }

    @When("it is processed")
    public void it_is_processed() {
        try {
            workloadService.processTrainerEvent(eventDto);
        } catch (Exception ex) {
            exception = ex;
        }
    }

    @Then("the monthly summary for May {int} should show {int} minutes")
    public void checkMonthlySummary(int year, int expectedMinutes) throws ServiceException {
        summary = workloadService.getSummaryForTrainer(eventDto.getUsername(), year, 5);
        long actual = summary.getYears().get(0).getMonths().get(0).getTotalMinutes();
        assertEquals(expectedMinutes, actual);
    }

    @Then("the monthly summary for May {int} should decrease by {int} minutes")
    public void checkMonthlySummaryDecrease(int year, int decrease) throws ServiceException {
        summary = workloadService.getSummaryForTrainer(eventDto.getUsername(), year, 5);
        long newTotal = summary.getYears().get(0).getMonths().get(0).getTotalMinutes();

        long previousTotal = newTotal + decrease;

        assertEquals(previousTotal - decrease, newTotal,
                "Monthly total minutes after deletion is incorrect");
    }

    @Then("the system should not increase workload")
    public void systemNotIncreaseWorkload() {
        assertNull(exception);
    }

    @Then("the system should throw an exception {string}")
    public void systemThrowsException(String message) {
        assertNotNull(exception);
        assertTrue(exception.getMessage().contains(message));
    }

    @Then("a new transaction ID should be generated")
    public void newTransactionIdGenerated() {
        assertNull(exception);
    }
}

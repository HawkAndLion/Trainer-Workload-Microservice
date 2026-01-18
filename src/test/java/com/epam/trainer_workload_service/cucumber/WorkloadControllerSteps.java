package com.epam.trainer_workload_service.cucumber;

import com.epam.trainer_workload_service.controller.WorkloadController;
import com.epam.trainer_workload_service.exception.GlobalExceptionHandler;
import com.epam.trainer_workload_service.model.TrainingSummaryResponseDto;
import com.epam.trainer_workload_service.service.ServiceException;
import com.epam.trainer_workload_service.service.WorkloadService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


public class WorkloadControllerSteps {

    private WorkloadService workloadService = mock(WorkloadService.class);
    private MockMvc mockMvc;
    private MvcResult mvcResult;

    @Given("the workload service returns a valid summary")
    public void the_workload_service_returns_a_valid_summary() {
        TrainingSummaryResponseDto summary = new TrainingSummaryResponseDto();
        summary.setUsername("trainer1");

        when(workloadService.getSummaryForTrainer(anyString(), anyInt(), anyInt())).thenReturn(summary);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new WorkloadController(workloadService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

    }

    @Given("the workload service throws ServiceException")
    public void the_workload_service_throws_service_exception() {
        when(workloadService.getSummaryForTrainer(anyString(), anyInt(), anyInt())).thenThrow(new ServiceException("Service error"));

        mockMvc = MockMvcBuilders
                .standaloneSetup(new WorkloadController(workloadService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @When("the client requests monthly summary for {string} year {int} month {int}")
    public void the_client_requests_monthly_summary_for_year_month(String username, Integer year, Integer month) throws Exception {
        mvcResult = mockMvc.perform(
                get("/api/v1/workload/{username}/{year}/{month}", username, year, month)
                        .header("transactionId", "tx-123")
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn();

    }


    @When("the client requests monthly summary without transactionId")
    public void the_client_requests_monthly_summary_without_transaction_id() throws Exception {
        String username = "trainer1";
        Integer year = 2025;
        Integer month = 5;

        mvcResult = mockMvc.perform(
                get("/api/v1/workload/{username}/{year}/{month}", username, year, month)
                        .accept(MediaType.APPLICATION_JSON)
        ).andReturn();
    }


    @Then("the response status should be {int}")
    public void the_response_status_should_be(Integer status) {
        assertEquals(status.intValue(), mvcResult.getResponse().getStatus());
    }

    @Then("the response should contain username {string}")
    public void the_response_should_contain_username(String username) throws UnsupportedEncodingException {
        String responseBody = mvcResult.getResponse().getContentAsString();
        assertTrue(responseBody.contains(username));
    }
}
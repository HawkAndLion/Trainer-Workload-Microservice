package com.epam.trainer_workload_service.controller;

import com.epam.trainer_workload_service.dto.TrainingSummaryDto;
import com.epam.trainer_workload_service.service.ServiceException;
import com.epam.trainer_workload_service.service.WorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WorkloadControllerTest {

    @Mock
    private WorkloadService workloadService;

    @InjectMocks
    private WorkloadController controller;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void shouldReturnSummaryWhenGetMonthlySummaryRequested() throws Exception {
        // Given
        TrainingSummaryDto summary = new TrainingSummaryDto();
        summary.setUsername("trainer1");
        summary.setFirstName("John");
        summary.setLastName("Doe");
        summary.setActive(true);
        summary.setYears(Collections.emptyList());

        when(workloadService.getSummaryForTrainer(eq("trainer1"), eq(2025), eq(5))).thenReturn(summary);

        // When
        var result = mockMvc.perform(get("/api/v1/workload/trainer1/2025/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("trainer1"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn();

        // Then
        verify(workloadService, times(1)).getSummaryForTrainer("trainer1", 2025, 5);
        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("\"username\":\"trainer1\""));
        assertTrue(responseBody.contains("\"firstName\":\"John\""));
        assertTrue(responseBody.contains("\"lastName\":\"Doe\""));
        assertTrue(responseBody.contains("\"active\":true"));
    }

    @Test
    void getMonthlySummary_shouldThrowRuntimeExceptionOnServiceException() throws ServiceException {
        // Given
        when(workloadService.getSummaryForTrainer(anyString(), anyInt(), anyInt()))
                .thenThrow(new ServiceException("error"));

        // When
        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            controller.getMonthlySummary("trainer1", 2025, 5, null);
        });

        // Then
        verify(workloadService, times(1)).getSummaryForTrainer("trainer1", 2025, 5);
        assertEquals("Check input arguments. They might be null.", ex.getMessage());
        assertEquals("error", ex.getCause().getMessage());
    }
}
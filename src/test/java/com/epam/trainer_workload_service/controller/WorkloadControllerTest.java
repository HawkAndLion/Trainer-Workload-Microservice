package com.epam.trainer_workload_service.controller;

import com.epam.trainer_workload_service.model.TrainingSummaryResponseDto;
import com.epam.trainer_workload_service.service.ServiceException;
import com.epam.trainer_workload_service.service.WorkloadService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkloadControllerTest {

    @Mock
    private WorkloadService workloadService;

    @InjectMocks
    private WorkloadController controller;

    @Test
    void shouldUseProvidedTransactionIdWhenGetMonthlySummaryCalled() throws Exception {
        // Given
        String providedTxId = "custom-transaction-id";
        TrainingSummaryResponseDto summary = new TrainingSummaryResponseDto();
        summary.setUsername("John.Doe");
        summary.setFirstName("John");
        summary.setLastName("Doe");
        summary.setActive(true);
        summary.setYears(Collections.emptyList());

        when(workloadService.getSummaryForTrainer("John.Doe", 2025, 5)).thenReturn(summary);

        // When
        var response = controller.getMonthlySummary("John.Doe", 2025, 5, providedTxId);

        // Then
        verify(workloadService, times(1)).getSummaryForTrainer("John.Doe", 2025, 5);
        assertEquals("John.Doe", response.getBody().getUsername());
    }

    @Test
    void shouldGenerateNewTransactionIdWhenBlank() throws Exception {
        // Given
        TrainingSummaryResponseDto summary = new TrainingSummaryResponseDto();
        summary.setUsername("John.Doe");
        summary.setFirstName("John");
        summary.setLastName("Doe");
        summary.setActive(true);
        summary.setYears(Collections.emptyList());

        when(workloadService.getSummaryForTrainer(eq("John.Doe"), eq(2025), eq(5))).thenReturn(summary);

        // When
        var response = controller.getMonthlySummary("John.Doe", 2025, 5, "");

        // Then
        verify(workloadService, times(1)).getSummaryForTrainer("John.Doe", 2025, 5);
        assertEquals("John.Doe", response.getBody().getUsername());
    }

    @Test
    void shouldThrowRuntimeExceptionOnServiceExceptionWhenTransactionIdIsNull() throws ServiceException {
        // Given
        when(workloadService.getSummaryForTrainer(anyString(), anyInt(), anyInt()))
                .thenThrow(new ServiceException("Check input arguments. They might be null."));

        // When
        ServiceException ex = assertThrows(ServiceException.class, () -> {
            controller.getMonthlySummary("trainer1", 2025, 5, null);
        });

        // Then
        verify(workloadService, times(1)).getSummaryForTrainer("trainer1", 2025, 5);
        assertEquals("Check input arguments. They might be null.", ex.getMessage());
    }


    @Test
    void shouldThrowServiceExceptionWhenError() throws Exception {
        // Given
        when(workloadService.getSummaryForTrainer(anyString(), anyInt(), anyInt()))
                .thenThrow(new ServiceException("error"));

        // When
        ServiceException exception = assertThrows(ServiceException.class,
                () -> controller.getMonthlySummary("trainer", 2025, 5, null));

        // Then
        verify(workloadService, times(1)).getSummaryForTrainer("trainer", 2025, 5);
        assertEquals("error", exception.getMessage());
    }
}
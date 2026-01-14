package com.epam.trainer_workload_service.controller;

import com.epam.trainer_workload_service.api.WorkloadApi;
import com.epam.trainer_workload_service.model.TrainingSummaryResponseDto;
import com.epam.trainer_workload_service.service.WorkloadService;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class WorkloadController implements WorkloadApi {

    private final WorkloadService workloadService;

    public WorkloadController(WorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    @Override
    public ResponseEntity<TrainingSummaryResponseDto> getMonthlySummary(
            String username,
            Integer year,
            Integer month,
            String transactionId
    ) {
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }

        MDC.put("transactionId", transactionId);

        try {
            TrainingSummaryResponseDto summary =
                    workloadService.getSummaryForTrainer(username, year, month);

            return ResponseEntity.ok(summary);
        } finally {
            MDC.remove("transactionId");
        }
    }
}

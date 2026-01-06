package com.epam.trainer_workload_service.controller;

import com.epam.trainer_workload_service.api.WorkloadApi;
import com.epam.trainer_workload_service.model.TrainingSummary;
import com.epam.trainer_workload_service.service.ServiceException;
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
    public ResponseEntity<TrainingSummary> getMonthlySummary(
            String username,
            Integer year,
            Integer month,
            String transactionId
    ) throws ServiceException {
        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }

        MDC.put("transactionId", transactionId);

        try {
            TrainingSummary summary =
                    workloadService.getSummaryForTrainer(username, year, month);

            return ResponseEntity.ok(summary);
        } finally {
            MDC.remove("transactionId");
        }
    }
}

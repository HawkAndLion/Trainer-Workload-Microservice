package com.epam.trainer_workload_service.controller;

import com.epam.trainer_workload_service.dto.TrainingEventDto;
import com.epam.trainer_workload_service.dto.TrainingSummaryDto;
import com.epam.trainer_workload_service.service.ServiceException;
import com.epam.trainer_workload_service.service.WorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/api/v1/workload")
public class WorkloadController {
    private static final Logger log = LoggerFactory.getLogger(WorkloadController.class);
    private static final String TRANSACTION_ID = "transactionId";
    private static final String POST_WORKLOAD = "POST /api/v1/workload called. transactionId={}, dto={}";
    private static final String SUCCESS_POST_WORKLOAD = "POST /api/v1/workload processed successfully. transactionId={}";
    private static final String GET_WORKLOAD = "GET /api/v1/workload/{}/{}/{} called. transactionId={}";
    private static final String SUCCESS_GET_WORKLOAD = "GET /api/v1/workload returned. transactionId={}, resultYears={}";
    private static final String ERROR_MESSAGE = "Check input arguments. They might be null.";

    private final WorkloadService workloadService;

    public WorkloadController(WorkloadService workloadService) {
        this.workloadService = workloadService;
    }

    @PostMapping
    public ResponseEntity<Void> processTrainingEvent(@RequestBody TrainingEventDto dto,
                                                     @RequestHeader(value = "transactionId", required = false) String transactionId) {

        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }

        MDC.put(TRANSACTION_ID, transactionId);

        log.info(POST_WORKLOAD, transactionId, dto);

        try {
            workloadService.processTrainerEvent(dto);
            log.info(SUCCESS_POST_WORKLOAD, transactionId);

            return ResponseEntity.ok().header(TRANSACTION_ID, transactionId).build();
        } finally {
            MDC.remove(TRANSACTION_ID);
        }
    }


    @GetMapping("/{username}/{year}/{month}")
    public ResponseEntity<TrainingSummaryDto> getMonthlySummary(@PathVariable String username,
                                                                @PathVariable int year,
                                                                @PathVariable int month,
                                                                @RequestHeader(value = "transactionId", required = false) String transactionId) {

        if (transactionId == null || transactionId.isBlank()) {
            transactionId = UUID.randomUUID().toString();
        }

        MDC.put(TRANSACTION_ID, transactionId);
        log.info(GET_WORKLOAD, username, year, month, transactionId);

        try {
            TrainingSummaryDto summary = workloadService.getSummaryForTrainer(username, year, month);

            log.info(SUCCESS_GET_WORKLOAD, transactionId, summary.getYears() == null ? 0 : summary.getYears().size());

            return ResponseEntity.ok(summary);
        } catch (ServiceException e) {
            throw new RuntimeException(ERROR_MESSAGE, e);
        } finally {
            MDC.remove(TRANSACTION_ID);
        }
    }
}

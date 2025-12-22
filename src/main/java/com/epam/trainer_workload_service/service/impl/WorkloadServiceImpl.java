package com.epam.trainer_workload_service.service.impl;

import com.epam.trainer_workload_service.dto.*;
import com.epam.trainer_workload_service.entity.TrainerMonthlyWorkload;
import com.epam.trainer_workload_service.entity.TrainingEventRecord;
import com.epam.trainer_workload_service.repository.TrainerMonthlyWorkloadRepository;
import com.epam.trainer_workload_service.repository.TrainingEventRecordRepository;
import com.epam.trainer_workload_service.service.ServiceException;
import com.epam.trainer_workload_service.service.WorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkloadServiceImpl implements WorkloadService {

    private static final Logger log = LoggerFactory.getLogger(WorkloadServiceImpl.class);

    private static final String NULL_DTO = "TrainingEventDto must not be null";
    private static final String NULL_USERNAME = "Username must not be null";
    private static final String NULL_TRAINING_DATE = "Training date must not be null";
    private static final String NULL_ACTION_TYPE = "Action type must not be null";

    private final TrainerMonthlyWorkloadRepository workloadRepository;
    private final TrainingEventRecordRepository eventRepository;

    public WorkloadServiceImpl(TrainerMonthlyWorkloadRepository workloadRepository,
                               TrainingEventRecordRepository eventRepository) {
        this.workloadRepository = workloadRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    @Override
    public void processTrainerEvent(TrainingEventDto dto) {

        validateDto(dto);

        int year = dto.getTrainingDate().getYear();
        int month = dto.getTrainingDate().getMonthValue();
        long minutes = dto.getDurationMinutes();

        if (dto.getActionType() == ActionType.ADD) {
            handleAdd(dto, year, month, minutes);
        } else {
            handleDelete(dto, year, month);
        }
    }

    private void handleAdd(TrainingEventDto dto, int year, int month, long minutes) {

        if (eventRepository.existsByTrainingId(dto.getTrainingId())) {
            log.info("Duplicate ADD ignored for trainingId={}", dto.getTrainingId());
            return;
        }

        TrainingEventRecord record = new TrainingEventRecord(
                dto.getTrainingId(),
                dto.getUsername(),
                dto.getTrainingDate(),
                minutes
        );

        eventRepository.save(record);
        upsertMonthlyWorkload(dto, year, month, minutes);

        log.info("ADD processed trainingId={}, minutes={}", dto.getTrainingId(), minutes);
    }

    private void handleDelete(TrainingEventDto dto, int year, int month) {

        TrainingEventRecord record = eventRepository
                .findByTrainingId(dto.getTrainingId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Training not found: " + dto.getTrainingId())
                );

        eventRepository.delete(record);
        upsertMonthlyWorkload(dto, year, month, -record.getDurationMinutes());

        log.info("DELETE processed trainingId={}", dto.getTrainingId());
    }

    private void upsertMonthlyWorkload(
            TrainingEventDto dto,
            int year,
            int month,
            long deltaMinutes
    ) {
        TrainerMonthlyWorkload workload =
                workloadRepository
                        .findByUsernameAndYearAndMonth(dto.getUsername(), year, month)
                        .orElseGet(() ->
                                new TrainerMonthlyWorkload(
                                        dto.getUsername(),
                                        dto.getFirstName(),
                                        dto.getLastName(),
                                        dto.isActive(),
                                        year,
                                        month,
                                        0L
                                )
                        );

        workload.setTotalMinutes(
                Math.max(0, workload.getTotalMinutes() + deltaMinutes)
        );

        workloadRepository.save(workload);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainingSummaryDto getSummaryForTrainer(String username, int year, int month)
            throws ServiceException {

        if (username == null) {
            throw new ServiceException("Username must not be null");
        }

        List<TrainerMonthlyWorkload> records = workloadRepository.findByUsername(username);

        if (records.isEmpty()) {
            return new TrainingSummaryDto(username, null, null, false, Collections.emptyList());
        }

        TrainerMonthlyWorkload base = records.get(0);

        return new TrainingSummaryDto(
                base.getUsername(),
                base.getFirstName(),
                base.getLastName(),
                base.isActive(),
                buildYearSummaries(records)
        );
    }

    private void validateDto(TrainingEventDto dto) {
        if (dto == null) {
            throw new IllegalArgumentException(NULL_DTO);
        }
        if (dto.getUsername() == null) {
            throw new IllegalArgumentException(NULL_USERNAME);
        }
        if (dto.getTrainingDate() == null) {
            throw new IllegalArgumentException(NULL_TRAINING_DATE);
        }
        if (dto.getActionType() == null) {
            throw new IllegalArgumentException(NULL_ACTION_TYPE);
        }
    }

    private static List<YearSummaryDto> buildYearSummaries(
            List<TrainerMonthlyWorkload> records) {

        Map<Integer, List<TrainerMonthlyWorkload>> byYear =
                records.stream().collect(Collectors.groupingBy(
                        TrainerMonthlyWorkload::getYear
                ));

        List<YearSummaryDto> years = new ArrayList<>();

        for (Map.Entry<Integer, List<TrainerMonthlyWorkload>> entry : byYear.entrySet()) {
            List<MonthSummaryDto> months = entry.getValue().stream()
                    .map(w -> new MonthSummaryDto(w.getMonth(), w.getTotalMinutes()))
                    .sorted(Comparator.comparing(MonthSummaryDto::getMonth))
                    .toList();

            years.add(new YearSummaryDto(entry.getKey(), months));
        }

        years.sort(Comparator.comparingInt(YearSummaryDto::getYear));
        return years;
    }
}

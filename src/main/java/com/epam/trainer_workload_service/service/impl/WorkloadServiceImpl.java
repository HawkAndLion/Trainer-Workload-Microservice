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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WorkloadServiceImpl implements WorkloadService {
    private static final Logger log = LoggerFactory.getLogger(WorkloadServiceImpl.class);
    private static final String NULL_ARGUMENT = "Check the argument. It might be null.";
    private static final String NULL_USER = "Username should not be null.";
    private static final String NULL_DTO = "dto must not be null";
    private static final String NULL_USERNAME = "username must not be null";
    private static final String NULL_TRAINING_DATE = "trainingDate must not be null";
    private static final String NULL_ACTION_TYPE = "actionType must not be null";
    private static final String NEGATIVE_DURATION_MINUTES = "durationMinutes must be non-negative";
    private static final String DELETE_MESSAGE = "DELETE event processed for {} date {} minutes {}";
    private static final String ADD_MESSAGE = "ADD event processed for {} date {} minutes {}";
    private static final String DUPLICATE_ADD_MESSAGE = "Duplicate ADD event ignored for {} date {} minutes {}";
    private static final String TRAINING_NOT_FOUND = "Training event to delete not found for user %s on %s with duration %d";

    private final TrainerMonthlyWorkloadRepository workloadRepository;
    private final TrainingEventRecordRepository eventRepository;

    public WorkloadServiceImpl(TrainerMonthlyWorkloadRepository workloadRepository,
                               TrainingEventRecordRepository eventRepository) {
        this.workloadRepository = workloadRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public void processTrainerEvent(TrainingEventDto dto) {
        if (dto != null) {
            validateDto(dto);

            String username = dto.getUsername();
            int year = dto.getTrainingDate().getYear();
            int month = dto.getTrainingDate().getMonthValue();
            long minutes = dto.getDurationMinutes();

            if (dto.getActionType() == ActionType.ADD) {
                handleAddEvent(dto, username, minutes, year, month);
            } else {
                handleDeleteEvent(dto, username, minutes, year, month);
            }
        } else {
            throw new IllegalArgumentException(NULL_ARGUMENT);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TrainingSummaryDto getSummaryForTrainer(String username, int year, int month) throws ServiceException {
        if (username != null) {
            List<TrainerMonthlyWorkload> records = workloadRepository.findByUsername(username);

            if (records.isEmpty()) return new TrainingSummaryDto(username, null, null, false, Collections.emptyList());

            List<YearSummaryDto> years = buildYearSummaries(records);

            TrainerMonthlyWorkload trainerWorkload = records.get(0);

            return new TrainingSummaryDto(
                    trainerWorkload.getUsername(),
                    trainerWorkload.getFirstName(),
                    trainerWorkload.getLastName(),
                    trainerWorkload.isActive(),
                    years
            );
        } else {
            throw new ServiceException(NULL_USER);
        }
    }

    private void upsertMonthlyWorkload(TrainingEventDto dto, int year, int month, long minutes, boolean isAdd) {
        String username = dto.getUsername();

        TrainerMonthlyWorkload workload = workloadRepository
                .findByUsernameAndYearAndMonth(username, year, month)
                .orElseGet(() -> new TrainerMonthlyWorkload(
                        dto.getUsername(),
                        dto.getFirstName(),
                        dto.getLastName(),
                        dto.isActive(),
                        year,
                        month,
                        0L
                ));

        workload.setFirstName(dto.getFirstName());
        workload.setLastName(dto.getLastName());
        workload.setActive(dto.isActive());

        long updated = workload.getTotalMinutes() + (isAdd ? minutes : -minutes);
        if (updated < 0) updated = 0;
        workload.setTotalMinutes(updated);

        workloadRepository.save(workload);
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

        if (dto.getDurationMinutes() < 0) {
            throw new IllegalArgumentException(NEGATIVE_DURATION_MINUTES);
        }
    }

    private void handleAddEvent(TrainingEventDto dto, String username, long minutes, int year, int month) {
        boolean exists = eventRepository.findFirstByUsernameAndTrainingDateAndDurationMinutes(username, dto.getTrainingDate(), minutes).isPresent();

        if (!exists) {
            TrainingEventRecord record = new TrainingEventRecord(
                    dto.getUsername(),
                    dto.getFirstName(),
                    dto.getLastName(),
                    dto.isActive(),
                    dto.getTrainingDate(),
                    minutes,
                    LocalDateTime.now()
            );
            eventRepository.save(record);
            upsertMonthlyWorkload(dto, year, month, minutes, true);

            log.info(ADD_MESSAGE, username, dto.getTrainingDate(), minutes);
        } else {
            log.info(DUPLICATE_ADD_MESSAGE, username, dto.getTrainingDate(), minutes);
        }
    }

    private void handleDeleteEvent(TrainingEventDto dto, String username, long minutes, int year, int month) {
        Optional<TrainingEventRecord> found = eventRepository.findFirstByUsernameAndTrainingDateAndDurationMinutes(username, dto.getTrainingDate(), minutes);

        if (found.isPresent()) {
            TrainingEventRecord toDelete = found.get();
            eventRepository.delete(toDelete);
            upsertMonthlyWorkload(dto, year, month, minutes, false);

            log.info(DELETE_MESSAGE, username, dto.getTrainingDate(), minutes);
        } else {
            throw new IllegalArgumentException(String.format(TRAINING_NOT_FOUND,
                    username, dto.getTrainingDate(), minutes));
        }
    }

    private static List<YearSummaryDto> buildYearSummaries(List<TrainerMonthlyWorkload> records) {
        Map<Integer, List<TrainerMonthlyWorkload>> byYear = records.stream()
                .collect(Collectors.groupingBy(TrainerMonthlyWorkload::getYear));

        List<YearSummaryDto> years = new ArrayList<>();

        for (Map.Entry<Integer, List<TrainerMonthlyWorkload>> yearEntry : byYear.entrySet()) {
            int currentYear = yearEntry.getKey();
            List<MonthSummaryDto> months = yearEntry.getValue().stream()
                    .map(workload -> new MonthSummaryDto(workload.getMonth(), workload.getTotalMinutes()))
                    .sorted(Comparator.comparing(MonthSummaryDto::getMonth))
                    .toList();

            years.add(new YearSummaryDto(currentYear, months));
        }

        years.sort(Comparator.comparingInt(YearSummaryDto::getYear));

        return years;
    }
}

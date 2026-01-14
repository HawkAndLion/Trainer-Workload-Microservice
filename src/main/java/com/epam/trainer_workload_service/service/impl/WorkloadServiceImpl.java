package com.epam.trainer_workload_service.service.impl;

import com.epam.trainer_workload_service.dto.ActionType;
import com.epam.trainer_workload_service.dto.TrainingEventDto;
import com.epam.trainer_workload_service.entity.TrainingEventRecord;
import com.epam.trainer_workload_service.mapper.WorkloadMapper;
import com.epam.trainer_workload_service.model.TrainingSummaryResponseDto;
import com.epam.trainer_workload_service.mongo.MonthWorkload;
import com.epam.trainer_workload_service.mongo.TrainerWorkloadDocument;
import com.epam.trainer_workload_service.mongo.YearWorkload;
import com.epam.trainer_workload_service.repository.TrainerWorkloadRepository;
import com.epam.trainer_workload_service.repository.TrainingEventRecordRepository;
import com.epam.trainer_workload_service.service.ServiceException;
import com.epam.trainer_workload_service.service.WorkloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class WorkloadServiceImpl implements WorkloadService {

    private static final Logger log = LoggerFactory.getLogger(WorkloadServiceImpl.class);

    private static final String NULL_DTO = "TrainingEventDto must not be null";
    private static final String NULL_USERNAME = "Username must not be null";
    private static final String NULL_TRAINING_DATE = "Training date must not be null";
    private static final String NULL_ACTION_TYPE = "Action type must not be null";
    private static final String DUPLICATE_ADD = "Duplicate ADD ignored for trainingId={}";
    private static final String ADD_PROCESSED = "ADD processed trainingId={}, minutes={}";
    private static final String TRAINING_NOT_FOUND = "Training not found: ";
    private static final String DELETE_PROCESSED = "DELETE processed trainingId={}";
    private static final String NEGATIVE_DURATION_MINUTES = "DurationMinutes must be non-negative";
    private static final String NEGATIVE_WORKLOAD = "Workload cannot be negative for user=";
    private static final String UPDATING_WORKLOAD_MESSAGE = "Updating workload: user={}, year={}, month={}, delta={}";
    private static final String SAVE_DOC_TO_MONGODB = "Saving workload document to MongoDB for username={}";
    private static final String TRAINER_NOT_FOUND = "Trainer not found: ";

    private final TrainerWorkloadRepository workloadRepository;
    private final TrainingEventRecordRepository eventRepository;
    private final WorkloadMapper workloadMapper;


    public WorkloadServiceImpl(TrainerWorkloadRepository workloadRepository, TrainingEventRecordRepository eventRepository, WorkloadMapper workloadMapper) {
        this.workloadRepository = workloadRepository;
        this.eventRepository = eventRepository;
        this.workloadMapper = workloadMapper;
    }

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

    @Override
    public TrainingSummaryResponseDto getSummaryForTrainer(String username, int year, int month)
            throws ServiceException {

        if (username != null) {

            TrainerWorkloadDocument doc = workloadRepository
                    .findByUsername(username)
                    .orElseThrow(() -> new ServiceException(TRAINER_NOT_FOUND + username));

            return workloadMapper.toTrainingSummary(doc, year, month);
        } else {
            throw new ServiceException(NULL_USERNAME);
        }
    }

    private void handleAdd(TrainingEventDto dto, int year, int month, long minutes) {

        if (eventRepository.existsByTrainingId(dto.getTrainingId())) {
            log.info(DUPLICATE_ADD, dto.getTrainingId());

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

        log.info(ADD_PROCESSED, dto.getTrainingId(), minutes);
    }

    private void handleDelete(TrainingEventDto dto, int year, int month) {

        TrainingEventRecord record = eventRepository
                .findByTrainingId(dto.getTrainingId())
                .orElseThrow(() ->
                        new IllegalArgumentException(TRAINING_NOT_FOUND + dto.getTrainingId())
                );

        eventRepository.delete(record);
        upsertMonthlyWorkload(dto, year, month, -record.getDurationMinutes());

        log.info(DELETE_PROCESSED, dto.getTrainingId());
    }

    private void upsertMonthlyWorkload(
            TrainingEventDto dto,
            int year,
            int month,
            long deltaMinutes
    ) {
        log.debug(
                UPDATING_WORKLOAD_MESSAGE,
                dto.getUsername(), year, month, deltaMinutes
        );

        TrainerWorkloadDocument document =
                workloadRepository.findByUsername(dto.getUsername())
                        .orElseGet(() -> createNewTrainer(dto));

        YearWorkload yearNode = document.getYears().stream()
                .filter(y -> y.getYear() == year)
                .findFirst()
                .orElseGet(() -> {
                    YearWorkload y = new YearWorkload(year, new ArrayList<>());
                    document.getYears().add(y);
                    return y;
                });

        MonthWorkload monthNode = yearNode.getMonths().stream()
                .filter(m -> m.getMonth() == month)
                .findFirst()
                .orElseGet(() -> {
                    MonthWorkload m = new MonthWorkload(month, 0L);
                    yearNode.getMonths().add(m);
                    return m;
                });

        long updated = monthNode.getTotalMinutes() + deltaMinutes;

        if (updated < 0) {
            throw new IllegalStateException(
                    NEGATIVE_WORKLOAD + dto.getUsername()
            );
        }

        monthNode.setTotalMinutes(updated);

        log.info(SAVE_DOC_TO_MONGODB, document.getUsername());

        workloadRepository.save(document);
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

        if (dto.getActionType() == ActionType.ADD && dto.getDurationMinutes() < 0) {
            throw new IllegalArgumentException(NEGATIVE_DURATION_MINUTES);
        }

    }

    private TrainerWorkloadDocument createNewTrainer(TrainingEventDto dto) {
        TrainerWorkloadDocument doc = new TrainerWorkloadDocument();
        doc.setUsername(dto.getUsername());
        doc.setFirstName(dto.getFirstName());
        doc.setLastName(dto.getLastName());
        doc.setActive(dto.isActive());
        doc.setYears(new ArrayList<>());

        return doc;
    }
}

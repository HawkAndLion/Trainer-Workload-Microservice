package com.epam.trainer_workload_service.service;

import com.epam.trainer_workload_service.dto.TrainingEventDto;
import com.epam.trainer_workload_service.model.TrainingSummary;

public interface WorkloadService {
    void processTrainerEvent(TrainingEventDto dto);

    TrainingSummary getSummaryForTrainer(String username, int year, int month) throws ServiceException;
}

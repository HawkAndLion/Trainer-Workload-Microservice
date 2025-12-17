package com.epam.trainer_workload_service.repository;

import com.epam.trainer_workload_service.entity.TrainingEventRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainingEventRecordRepository extends JpaRepository<TrainingEventRecord, Long> {
    List<TrainingEventRecord> findByUsername(String username);

    Optional<TrainingEventRecord> findFirstByUsernameAndTrainingDateAndDurationMinutes(String username, LocalDate trainingDate, long durationMinutes);
}

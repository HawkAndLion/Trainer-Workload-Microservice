//package com.epam.trainer_workload_service.repository;
//
//import com.epam.trainer_workload_service.entity.TrainerMonthlyWorkload;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional;
//
//@Repository
//public interface TrainerMonthlyWorkloadRepository extends JpaRepository<TrainerMonthlyWorkload, Long> {
//    Optional<TrainerMonthlyWorkload> findByUsernameAndYearAndMonth(String username, int year, int month);
//
//    List<TrainerMonthlyWorkload> findByUsername(String username);
//}

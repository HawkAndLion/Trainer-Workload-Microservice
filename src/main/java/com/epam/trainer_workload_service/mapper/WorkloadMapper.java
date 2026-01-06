package com.epam.trainer_workload_service.mapper;

import com.epam.trainer_workload_service.entity.TrainerMonthlyWorkload;
import com.epam.trainer_workload_service.model.MonthSummary;
import com.epam.trainer_workload_service.model.TrainingSummary;
import com.epam.trainer_workload_service.model.YearSummary;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class WorkloadMapper {

    private static final String NULL_ARGUMENT = "Argument should not be null";

    public TrainingSummary toTrainingSummary(String username,
                                             List<TrainerMonthlyWorkload> records) {
        if (username != null && !records.isEmpty()) {
            TrainingSummary summary = new TrainingSummary();

            summary.setUsername(username);

            if (records.isEmpty()) {
                summary.setActive(false);
                summary.setYears(Collections.emptyList());

                return summary;
            }

            TrainerMonthlyWorkload base = records.get(0);
            summary.setFirstName(base.getFirstName());
            summary.setLastName(base.getLastName());
            summary.setActive(base.isActive());

            summary.setYears(buildYearSummaries(records));

            return summary;
        } else {
            throw new IllegalArgumentException(NULL_ARGUMENT);
        }

    }

    private static List<YearSummary> buildYearSummaries(
            List<TrainerMonthlyWorkload> records) {

        return records.stream()
                .collect(Collectors.groupingBy(TrainerMonthlyWorkload::getYear))
                .entrySet().stream()
                .map(entry -> {
                    YearSummary year = new YearSummary();
                    year.setYear(entry.getKey());

                    List<MonthSummary> months = getMonthSummaries(entry);

                    year.setMonths(months);

                    return year;
                })
                .sorted(Comparator.comparing(YearSummary::getYear))
                .toList();
    }

    private static List<MonthSummary> getMonthSummaries(Map.Entry<Integer, List<TrainerMonthlyWorkload>> entry) {
        return entry.getValue().stream()
                .map(w -> {
                    MonthSummary summary = new MonthSummary();
                    summary.setMonth(w.getMonth());
                    summary.setTotalMinutes(w.getTotalMinutes());

                    return summary;
                })
                .sorted(Comparator.comparing(MonthSummary::getMonth))
                .toList();
    }
}

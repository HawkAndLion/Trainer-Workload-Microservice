package com.epam.trainer_workload_service.mapper;

import com.epam.trainer_workload_service.dto.MonthSummaryDto;
import com.epam.trainer_workload_service.dto.TrainingSummaryDto;
import com.epam.trainer_workload_service.dto.YearSummaryDto;
import com.epam.trainer_workload_service.entity.TrainerMonthlyWorkload;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class WorkloadMapper {

    public TrainingSummaryDto toTrainingSummary(String username,
                                                List<TrainerMonthlyWorkload> records) {
        if (records != null && !records.isEmpty()) {
            TrainerMonthlyWorkload base = records.get(0);

            return new TrainingSummaryDto(
                    base.getUsername(),
                    base.getFirstName(),
                    base.getLastName(),
                    base.isActive(),
                    buildYearSummaries(records)
            );
        } else {
            return new TrainingSummaryDto(username, null, null, false, Collections.emptyList());
        }

    }

    private List<MonthSummaryDto> toMonthSummaries(
            List<TrainerMonthlyWorkload> records
    ) {
        return records.stream()
                .map(w ->
                        new MonthSummaryDto(
                                w.getMonth(),
                                w.getTotalMinutes()
                        )
                )
                .sorted(Comparator.comparing(MonthSummaryDto::getMonth))
                .toList();
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

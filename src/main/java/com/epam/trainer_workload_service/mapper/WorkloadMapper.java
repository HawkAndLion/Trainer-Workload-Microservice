package com.epam.trainer_workload_service.mapper;

import com.epam.trainer_workload_service.model.MonthSummary;
import com.epam.trainer_workload_service.model.TrainingSummary;
import com.epam.trainer_workload_service.model.YearSummary;
import com.epam.trainer_workload_service.mongo.MonthWorkload;
import com.epam.trainer_workload_service.mongo.TrainerWorkloadDocument;
import com.epam.trainer_workload_service.mongo.YearWorkload;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkloadMapper {

    private static final String NULL_ARGUMENT = "Argument should not be null";

    public TrainingSummary toTrainingSummary(
            TrainerWorkloadDocument doc,
            int year,
            int month
    ) {
        if (doc != null) {
            TrainingSummary summary = new TrainingSummary()
                    .username(doc.getUsername())
                    .firstName(doc.getFirstName())
                    .lastName(doc.getLastName())
                    .active(doc.isActive());

            if (doc.getYears() == null || doc.getYears().isEmpty()) {
                return summary;
            }

            YearWorkload yearNode = doc.getYears().stream()
                    .filter(y -> y.getYear() == year)
                    .findFirst()
                    .orElse(null);

            if (yearNode == null) {
                return summary;
            }

            MonthWorkload monthNode = yearNode.getMonths().stream()
                    .filter(m -> m.getMonth() == month)
                    .findFirst()
                    .orElse(null);

            if (monthNode == null) {
                return summary;
            }

            MonthSummary monthSummary = new MonthSummary()
                    .month(month)
                    .totalMinutes(monthNode.getTotalMinutes());

            YearSummary yearSummary = new YearSummary()
                    .year(year)
                    .months(List.of(monthSummary));

            summary.setYears(List.of(yearSummary));

            return summary;

        } else {
            throw new IllegalArgumentException(NULL_ARGUMENT);
        }

    }
}

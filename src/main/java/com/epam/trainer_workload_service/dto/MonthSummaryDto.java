package com.epam.trainer_workload_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthSummaryDto {

    private int month;
    private long totalMinutes;

}

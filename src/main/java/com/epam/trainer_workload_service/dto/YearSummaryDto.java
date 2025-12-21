package com.epam.trainer_workload_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "months")
public class YearSummaryDto {

    private int year;
    private List<MonthSummaryDto> months;

}

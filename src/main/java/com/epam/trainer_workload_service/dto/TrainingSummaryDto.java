package com.epam.trainer_workload_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "years")
public class TrainingSummaryDto {

    private String username;
    private String firstName;
    private String lastName;
    private boolean isActive;
    private List<YearSummaryDto> years;

}

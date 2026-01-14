package com.epam.trainer_workload_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrainingEventErrorDto {

    private Long trainingId;
    private String errorCode;
    private String errorMessage;
}

package com.epam.trainer_workload_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrainingEventErrorDto {

    private Long trainingId;
    private String errorCode;
    private String errorMessage;
}

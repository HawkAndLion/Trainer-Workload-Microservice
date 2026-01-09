package com.epam.trainer_workload_service.mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MonthWorkload {

    private int month;
    private long totalMinutes;
}


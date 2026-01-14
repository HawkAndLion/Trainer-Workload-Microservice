package com.epam.trainer_workload_service.mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class YearWorkload {

    private int year;
    private List<MonthWorkload> months = new ArrayList<>();
}


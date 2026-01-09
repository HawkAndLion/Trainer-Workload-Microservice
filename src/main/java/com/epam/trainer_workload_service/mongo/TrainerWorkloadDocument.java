package com.epam.trainer_workload_service.mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "trainer_workloads")
@CompoundIndex(
        name = "idx_trainer_first_last_name",
        def = "{'firstName': 1, 'lastName': 1}"
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TrainerWorkloadDocument {

    @Id
    private String username;

    private String firstName;

    private String lastName;

    private boolean active;

    private List<YearWorkload> years = new ArrayList<>();
}


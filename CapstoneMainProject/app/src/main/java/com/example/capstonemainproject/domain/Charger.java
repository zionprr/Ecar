package com.example.capstonemainproject.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class Charger implements Serializable {

    private Long id;

    private Long chargerNumber;

    private String chargerName;

    private Integer type;

    private Integer mode;

    private Integer state;

    private LocalDateTime stateUpdatedAt;

    private Station station;
}

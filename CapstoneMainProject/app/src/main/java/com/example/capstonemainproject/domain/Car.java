package com.example.capstonemainproject.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class Car {

    @JsonProperty(value = "carId")
    private Long id;

    private String carModel;

    private String carModelYear;

    private String carType;

    private String carNumber;
}

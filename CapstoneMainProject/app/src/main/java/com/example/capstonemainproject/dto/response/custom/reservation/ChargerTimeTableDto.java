package com.example.capstonemainproject.dto.response.custom.reservation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
public class ChargerTimeTableDto {

    private Long chargerId;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate targetDate;

    private Map<String, Boolean> timeTable = new HashMap<>();
}

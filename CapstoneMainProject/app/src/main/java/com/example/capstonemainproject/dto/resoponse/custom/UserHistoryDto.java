package com.example.capstonemainproject.dto.resoponse.custom;

import com.example.capstonemainproject.domain.Station;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserHistoryDto {

    private Station station;

    private Integer chargerCount;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime searchedAt;
}

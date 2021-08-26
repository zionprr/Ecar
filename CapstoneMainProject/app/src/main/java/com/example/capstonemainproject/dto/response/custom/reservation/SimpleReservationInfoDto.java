package com.example.capstonemainproject.dto.response.custom.reservation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SimpleReservationInfoDto {

    private String stationName;

    private String chargerName;

    private String userName;

    private String carNumber;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime chargeStartDateTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime chargeEndDateTime;

    private Integer fares;
}

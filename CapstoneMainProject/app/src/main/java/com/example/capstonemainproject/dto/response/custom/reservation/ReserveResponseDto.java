package com.example.capstonemainproject.dto.response.custom.reservation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReserveResponseDto {

    private Long reservationId;

    private Long chargerId;

    private String userName;

    private String carNumber;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime reservedAt;

    private String state;

    private Integer fares;
}

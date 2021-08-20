package com.example.capstonemainproject.dto.request.reservation;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class RequestReservationDto {

    private Long chargerId;

    private Long carId;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime start;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime end;
}

package com.example.capstonemainproject.dto.response.custom.reservation;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ReservationStatementDto {

    private String reserveTitle;

    private Long chargerId;

    private String userName;

    private String carNumber;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime reservedAt;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime chargeStartDateTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime chargeEndDateTime;

    private String state;

    private Integer usedCashPoint;

    private Integer paidCash;

    private Integer cancellationFee;
}

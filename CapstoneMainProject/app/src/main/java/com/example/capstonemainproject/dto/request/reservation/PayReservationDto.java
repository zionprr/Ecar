package com.example.capstonemainproject.dto.request.reservation;

import lombok.Data;

@Data
public class PayReservationDto {

    private Long reservationId;

    private String paymentPassword;

    private Integer usedCashPoint;
}

package com.example.ecar_service_station.dto.request.user;

import lombok.Data;

@Data
public class UpdateNotificationDto {

    private boolean onNotificationOfReservationStart;

    private Integer minutesBeforeReservationStart;

    private boolean onNotificationOfChargingEnd;

    private Integer minutesBeforeChargingEnd;
}

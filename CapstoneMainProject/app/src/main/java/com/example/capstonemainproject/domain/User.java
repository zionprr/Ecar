package com.example.capstonemainproject.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class User implements Serializable {

    private Long id;

    private String name;

    private String email;

    private String phoneNumber;

    private LocalDateTime joinedAt;

    private Integer cash;

    private Integer cashPoint;

    private boolean isOnNotificationOfReservationStart;

    private Integer notificationMinutesBeforeReservationStart;

    private boolean isOnNotificationOfChargingEnd;

    private Integer notificationMinutesBeforeChargingEnd;
}

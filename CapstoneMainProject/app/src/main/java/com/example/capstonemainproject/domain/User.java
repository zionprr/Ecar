package com.example.capstonemainproject.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements Serializable {

    private Long id;

    private String name;

    private String email;

    private String phoneNumber;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime joinedAt;

    private Integer cash;

    private Integer cashPoint;

    @JsonProperty(value = "isOnNotificationOfReservationStart")
    private boolean isOnNotificationOfReservationStart;

    private Integer notificationMinutesBeforeReservationStart;

    @JsonProperty(value = "isOnNotificationOfChargingEnd")
    private boolean isOnNotificationOfChargingEnd;

    private Integer notificationMinutesBeforeChargingEnd;
}

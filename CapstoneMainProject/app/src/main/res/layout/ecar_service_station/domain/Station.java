package com.example.ecar_service_station.domain;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class Station implements Serializable {

    private Long id;

    private Long stationNumber;

    private String stationName;

    private String stationAddress;

    private Double latitude;

    private Double longitude;
}

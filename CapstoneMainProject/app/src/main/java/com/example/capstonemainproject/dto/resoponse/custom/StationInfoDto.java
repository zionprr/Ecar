package com.example.capstonemainproject.dto.resoponse.custom;

import com.example.capstonemainproject.domain.Charger;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "stationId")
public class StationInfoDto {

    private Long stationId;

    private Long stationNumber;

    private String stationName;

    private String stationAddress;

    private Double latitude;

    private Double longitude;

    private Set<Charger> chargers = new HashSet<>();

    private boolean bookmarked;
}

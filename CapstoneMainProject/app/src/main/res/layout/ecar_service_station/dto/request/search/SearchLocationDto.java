package com.example.ecar_service_station.dto.request.search;

import lombok.Data;

@Data
public class SearchLocationDto {

    private Double latitude;

    private Double longitude;

    private Integer cpTp;

    private Integer chargerTp;
}

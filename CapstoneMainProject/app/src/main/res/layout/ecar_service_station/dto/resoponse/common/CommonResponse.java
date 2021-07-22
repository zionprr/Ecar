package com.example.ecar_service_station.dto.resoponse.common;

import lombok.Data;

@Data
public class CommonResponse {

    private boolean success;

    private int responseCode;

    private String message;
}

package com.example.ecar_service_station.dto.resoponse.common;

import lombok.Data;

@Data
public class SingleResultResponse<T> extends CommonResponse {

    private T data;
}

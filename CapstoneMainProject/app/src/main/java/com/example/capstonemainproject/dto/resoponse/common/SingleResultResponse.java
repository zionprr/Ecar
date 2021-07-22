package com.example.capstonemainproject.dto.resoponse.common;

import lombok.Data;

@Data
public class SingleResultResponse<T> extends CommonResponse {

    private T data;
}

package com.example.capstonemainproject.dto.response.common;

import lombok.Data;

@Data
public class SingleResultResponse<T> extends CommonResponse {

    private T data;
}

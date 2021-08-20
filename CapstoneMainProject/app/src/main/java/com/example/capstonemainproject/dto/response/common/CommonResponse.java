package com.example.capstonemainproject.dto.response.common;

import lombok.Data;

@Data
public class CommonResponse {

    private boolean success;

    private int responseCode;

    private String message;
}

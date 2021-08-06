package com.example.capstonemainproject.dto.request.car;

import lombok.Data;

@Data
public class RegisterCarDto {

    private String carModel;

    private String carModelYear;

    private String carType;

    private String carNumber;
}

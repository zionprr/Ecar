package com.example.ecar_service_station.dto.request.user;

import lombok.Data;

@Data
public class LoginDto {

    private String email;

    private String password;

    private String deviceToken;
}

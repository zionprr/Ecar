package com.example.ecar_service_station.dto.request.user;

import lombok.Data;

@Data
public class SignUpDto {

    private String userName;

    private String email;

    private String phoneNumber;

    private String password;
}

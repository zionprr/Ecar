package com.example.ecar_service_station.dto.request.user;

import lombok.Data;

@Data
public class UpdatePasswordDto {

    private String currentPassword;

    private String newPassword;
}

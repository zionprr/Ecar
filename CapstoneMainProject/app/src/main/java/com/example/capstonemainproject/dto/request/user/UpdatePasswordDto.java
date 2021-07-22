package com.example.capstonemainproject.dto.request.user;

import lombok.Data;

@Data
public class UpdatePasswordDto {

    private String currentPassword;

    private String newPassword;
}

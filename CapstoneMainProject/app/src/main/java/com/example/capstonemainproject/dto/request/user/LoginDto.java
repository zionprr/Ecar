package com.example.capstonemainproject.dto.request.user;

import lombok.Data;

@Data
public class LoginDto {

    private String email;

    private String password;

    private String deviceToken;
}

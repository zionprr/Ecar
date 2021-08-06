package com.example.capstonemainproject.dto.request.bank;

import lombok.Data;

@Data
public class AuthBankAccountDto {

    private Long bankId;

    private String paymentPassword;

    private String authMsg;
}

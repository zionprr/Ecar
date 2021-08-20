package com.example.capstonemainproject.dto.response.custom.bank;

import lombok.Data;

@Data
public class RegisteredBankAccountDto {

    private Long bankId;

    private String bankName;

    private String bankAccountNumber;

    private String msg;
}

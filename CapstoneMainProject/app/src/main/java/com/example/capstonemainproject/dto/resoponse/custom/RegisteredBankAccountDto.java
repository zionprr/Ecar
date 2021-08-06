package com.example.capstonemainproject.dto.resoponse.custom;

import lombok.Data;

@Data
public class RegisteredBankAccountDto {

    private Long bankId;

    private String bankName;

    private String bankAccountNumber;

    private String msg;
}

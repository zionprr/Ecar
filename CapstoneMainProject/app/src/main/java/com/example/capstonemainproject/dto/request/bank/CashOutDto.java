package com.example.capstonemainproject.dto.request.bank;

import lombok.Data;

@Data
public class CashOutDto {

    private Integer amount;

    private String paymentPassword;
}

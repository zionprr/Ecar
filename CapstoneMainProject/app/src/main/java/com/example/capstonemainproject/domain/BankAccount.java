package com.example.capstonemainproject.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class BankAccount {

    @JsonProperty(value = "bankId")
    private Long id;

    private String bankName;

    private String bankAccountNumber;

    private String bankAccountOwner;

    private boolean mainUsed;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime registeredAt;
}

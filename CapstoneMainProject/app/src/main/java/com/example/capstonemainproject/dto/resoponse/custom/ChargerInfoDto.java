package com.example.capstonemainproject.dto.resoponse.custom;

import com.example.capstonemainproject.domain.Station;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.time.LocalDateTime;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "chargerId")
public class ChargerInfoDto {

    @JsonIgnore
    private final String[] modeToString = {
            "B타입(5핀)", "C타입(5핀)", "BC타입(5핀)", "BC타입(7핀)", "DC차데모", "AC3상", "DC콤보",
            "DC차데모 + DC콤보", "DC차데모 + AC3상", "DC차데모 + DC콤보 + AC3상"
    };

    @JsonIgnore
    private final String[] stateToString = {
            "충전 가능", "충전중", "고장/점검", "통신 장애", "통신 미연결"
    };

    private Long chargerId;

    private Long chargerNumber;

    private String chargerName;

    private Integer type;

    private Integer mode;

    private Integer state;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime stateUpdatedAt;

    private Station station;

    public String stringValueOfMode() {
        return mode != null ? modeToString[mode - 1] : "NULL";
    }

    public String stringValueOfState() {
        return state != null ? stateToString[state - 1] : "NULL";
    }
}

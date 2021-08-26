package com.example.capstonemainproject.domain;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data
@RequiresApi(api = Build.VERSION_CODES.O)
public class ReservationStatement implements Serializable {

    @JsonIgnore
    private final Map<String,String> stateMap = new HashMap<>();

    {
        stateMap.put("STAND_BY", "예약 확정 대기 (결제 이전)");
        stateMap.put("PAYMENT", "예약 확정");
        stateMap.put("CANCEL", "예약 취소");
        stateMap.put("CHARGING", "충전중");
    }

    private Long reservationId;

    private String reserveTitle;

    private Long chargerId;

    private String userName;

    private String carNumber;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime reservedAt;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime chargeStartDateTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime chargeEndDateTime;

    private String state;

    private Integer reserveFares;

    private Integer usedCashPoint;

    private Integer paidCash;

    private Integer cancellationFee;

    public String stateValue() {
        return stateMap.getOrDefault(state, "NULL");
    }
}

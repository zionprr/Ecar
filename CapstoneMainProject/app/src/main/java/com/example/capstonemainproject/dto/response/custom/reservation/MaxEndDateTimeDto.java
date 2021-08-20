package com.example.capstonemainproject.dto.response.custom.reservation;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.time.Duration;
import java.time.LocalDateTime;

import lombok.Data;

@Data
@RequiresApi(api = Build.VERSION_CODES.O)
public class MaxEndDateTimeDto {

    private Long chargerId;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime targetDateTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime maxEndDateTime;

    private Integer faresPerHour;

    public int calMinuteDifference() {
        Duration duration = Duration.between(targetDateTime, maxEndDateTime);
        long seconds = duration.getSeconds();

        return (int) seconds / 60;
    }
}

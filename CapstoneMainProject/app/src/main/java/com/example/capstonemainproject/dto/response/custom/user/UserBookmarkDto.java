package com.example.capstonemainproject.dto.response.custom.user;

import com.example.capstonemainproject.domain.Station;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserBookmarkDto {

    private Station station;

    private Integer chargerCount;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime registeredAt;
}

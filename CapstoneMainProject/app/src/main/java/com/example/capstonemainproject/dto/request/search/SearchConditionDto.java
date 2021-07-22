package com.example.capstonemainproject.dto.request.search;

import lombok.Data;

@Data
public class SearchConditionDto {

    private String search;

    private Integer cpTp;

    private Integer chargerTp;

    private Double latitude;

    private Double longitude;

    private String sortType;
}

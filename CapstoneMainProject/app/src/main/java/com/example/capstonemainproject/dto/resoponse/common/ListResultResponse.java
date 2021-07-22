package com.example.capstonemainproject.dto.resoponse.common;

import java.util.List;

import lombok.Data;

@Data
public class ListResultResponse<T> extends CommonResponse {

    private List<T> dataList;
}

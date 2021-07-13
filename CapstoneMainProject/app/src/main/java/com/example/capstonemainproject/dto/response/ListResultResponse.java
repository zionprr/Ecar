package com.example.capstonemainproject.dto.response;

import java.util.List;

public class ListResultResponse extends CommonResponse {

    private List<String> dataList;

    public List<String> getDataList() {
        return dataList;
    }

    public void setDataList(List<String> dataList) {
        this.dataList = dataList;
    }
}

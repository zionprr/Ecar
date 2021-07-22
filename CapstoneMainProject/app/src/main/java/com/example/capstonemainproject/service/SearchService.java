package com.example.capstonemainproject.service;

import android.os.AsyncTask;
import android.util.Log;

import com.example.capstonemainproject.domain.Charger;
import com.example.capstonemainproject.dto.request.search.SearchConditionDto;
import com.example.capstonemainproject.dto.request.search.SearchLocationDto;
import com.example.capstonemainproject.dto.resoponse.common.CommonResponse;
import com.example.capstonemainproject.dto.resoponse.common.ListResultResponse;
import com.example.capstonemainproject.infra.network.HttpConnectionProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;

public class SearchService extends AsyncTask<Integer, Void, CommonResponse> {

    private static final int SEARCH_SERVICE_BY_TEXT = -15;
    private static final int SEARCH_SERVICE_BY_LOCATION = -16;

    private final HttpConnectionProvider httpConnectionProvider;
    private final ObjectMapper objectMapper;
    private final String loginAccessToken;

    private SearchConditionDto searchCondition;
    private SearchLocationDto searchLocation;

    public SearchService(String loginAccessToken, SearchConditionDto searchConditionDto) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
        this.searchCondition = searchConditionDto;
    }

    public SearchService(String loginAccessToken, SearchLocationDto searchLocationDto) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
        this.searchLocation = searchLocationDto;
    }

    @Override
    protected CommonResponse doInBackground(Integer... requestCode) {
        String hostServer = httpConnectionProvider.getHostServer();
        String URI = hostServer + "/ecar/find";

        try {
            if (requestCode[0] == SEARCH_SERVICE_BY_TEXT) {
                URI += searchConditionToUri();

            } else if (requestCode[0] == SEARCH_SERVICE_BY_LOCATION) {
                URI += "/location";
                URI += searchLocationToUri();
            }

            HttpURLConnection getConnection = httpConnectionProvider.createGETConnection(URI);

            // 헤더에 로그인 토큰 추가
            httpConnectionProvider.addHeader(getConnection, "X-AUTH-TOKEN", loginAccessToken);

            if (getConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String jsonString = httpConnectionProvider.readData(getConnection);

                return objectMapper.readValue(jsonString, new TypeReference<ListResultResponse<Charger>>() {});

            } else {
                String jsonString = httpConnectionProvider.readError(getConnection);

                return objectMapper.readValue(jsonString, CommonResponse.class);
            }

        } catch (JsonProcessingException e) {
            Log.w("Search service", "ObjectMapper error");

        } catch (IOException e) {
            Log.w("Search service", "Connection error");
        }

        return null;
    }

    private String searchConditionToUri() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append("?search=").append(searchCondition.getSearch())
                .append("&latitude=").append(searchCondition.getLatitude())
                .append("&longitude=").append(searchCondition.getLongitude());

        if (searchCondition.getCpTp() != null) {
            stringBuilder.append("&cpTp=").append(searchCondition.getCpTp());
        }

        if (searchCondition.getChargerTp() != null) {
            stringBuilder.append("&chargerTp=").append(searchCondition.getChargerTp());
        }

        return stringBuilder.toString();
    }

    private String searchLocationToUri() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append("?latitude=").append(searchLocation.getLatitude())
                .append("&longitude=").append(searchLocation.getLongitude());

        if (searchLocation.getCpTp() != null) {
            stringBuilder.append("&cpTp=").append(searchLocation.getCpTp());
        }

        if (searchLocation.getChargerTp() != null) {
            stringBuilder.append("&chargerTp=").append(searchLocation.getChargerTp());
        }

        return stringBuilder.toString();
    }
}

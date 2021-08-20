package com.example.capstonemainproject.service;

import android.os.AsyncTask;
import android.util.Log;

import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.SingleResultResponse;
import com.example.capstonemainproject.infra.network.HttpConnectionProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;

public class StationService extends AsyncTask<Long, Void, CommonResponse> {

    private static final long STATION_SERVICE_GET_INFO = -17;
    private static final long STATION_SERVICE_GET_INFO_RECORD = -18;

    private final HttpConnectionProvider httpConnectionProvider;
    private final ObjectMapper objectMapper;
    private final String loginAccessToken;

    public StationService(String loginAccessToken) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
    }

    @Override
    protected CommonResponse doInBackground(Long... stationId) {
        String hostServer = httpConnectionProvider.getHostServer();
        String URI = hostServer + "/ecar/station/" + stationId[0];

        try {
            if (stationId[1] == STATION_SERVICE_GET_INFO_RECORD) {
                URI += "/record";
            }

            HttpURLConnection getConnection = httpConnectionProvider.createGETConnection(URI);

            httpConnectionProvider.addHeader(getConnection, "X-AUTH-TOKEN", loginAccessToken);

            if (getConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String jsonString = httpConnectionProvider.readData(getConnection);

                return objectMapper.readValue(jsonString, new TypeReference<SingleResultResponse<StationInfoDto>>() {});

            } else {
                String jsonString = httpConnectionProvider.readError(getConnection);

                return objectMapper.readValue(jsonString, CommonResponse.class);
            }

        } catch (JsonProcessingException e) {
            Log.w("Station service", "ObjectMapper error");

        } catch (IOException e) {
            Log.w("Station service", "Connection error");
        }

        return null;
    }
}

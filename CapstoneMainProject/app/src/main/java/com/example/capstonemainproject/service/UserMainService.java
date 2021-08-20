package com.example.capstonemainproject.service;

import android.os.AsyncTask;
import android.util.Log;

import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.ListResultResponse;
import com.example.capstonemainproject.infra.network.HttpConnectionProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;

public class UserMainService extends AsyncTask<Long, Void, CommonResponse> {

    private static final int USER_MAIN_SERVICE_GET_HISTORIES = -21;
    private static final int USER_MAIN_SERVICE_GET_BOOKMARKS = -22;
    private static final int USER_MAIN_SERVICE_REGISTER_BOOKMARK = -23;
    private static final int USER_MAIN_SERVICE_DELETE_BOOKMARK = -24;
    private static final int USER_MAIN_SERVICE_GET_RESERVATION_STATEMENTS = -25;

    private final HttpConnectionProvider httpConnectionProvider;
    private final ObjectMapper objectMapper;
    private final String loginAccessToken;

    public UserMainService(String loginAccessToken) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
    }

    @Override
    protected CommonResponse doInBackground(Long... requestCode) {
        String hostServer = httpConnectionProvider.getHostServer();
        String URI = hostServer + "/user";

        try {
            HttpURLConnection httpURLConnection = null;

            switch (requestCode[0].intValue()) {
                case USER_MAIN_SERVICE_GET_HISTORIES:
                    URI += "/histories";
                    httpURLConnection = httpConnectionProvider.createGETConnection(URI);
                    break;

                case USER_MAIN_SERVICE_GET_BOOKMARKS:
                    URI += "/bookmarks";
                    httpURLConnection = httpConnectionProvider.createGETConnection(URI);
                    break;

                case USER_MAIN_SERVICE_REGISTER_BOOKMARK:
                    URI += "/bookmark/" + requestCode[1];
                    httpURLConnection = httpConnectionProvider.createPUTConnection(URI);
                    break;

                case USER_MAIN_SERVICE_DELETE_BOOKMARK:
                    URI += "/bookmark/" + requestCode[1];
                    httpURLConnection = httpConnectionProvider.createDELETEConnection(URI);
                    break;

                case USER_MAIN_SERVICE_GET_RESERVATION_STATEMENTS:
                    URI += "/reservation-statements?state=" + requestCode[1];
                    httpURLConnection = httpConnectionProvider.createGETConnection(URI);
            }

            // 로그인 헤더
            httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String jsonString = httpConnectionProvider.readData(httpURLConnection);

                if (requestCode[0] == USER_MAIN_SERVICE_GET_HISTORIES) {
                    return objectMapper.readValue(jsonString, new TypeReference<ListResultResponse<UserHistoryDto>>() {});

                } else if(requestCode[0] == USER_MAIN_SERVICE_GET_BOOKMARKS) {
                    return objectMapper.readValue(jsonString, new TypeReference<ListResultResponse<UserBookmarkDto>>() {});

                } else if(requestCode[0] == USER_MAIN_SERVICE_GET_RESERVATION_STATEMENTS) {
                    return objectMapper.readValue(jsonString, new TypeReference<ListResultResponse<ReservationStatementDto>>() {});
                }

                return objectMapper.readValue(jsonString, CommonResponse.class);

            } else {
                String jsonString = httpConnectionProvider.readError(httpURLConnection);

                return objectMapper.readValue(jsonString, CommonResponse.class);
            }

        } catch (JsonProcessingException e) {
            Log.w("User main service", "ObjectMapper error");

        } catch (IOException e) {
            Log.w("User main service", "Connection error");
        }

        return null;
    }
}

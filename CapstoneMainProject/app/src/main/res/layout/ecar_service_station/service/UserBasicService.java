package com.example.ecar_service_station.service;

import android.os.AsyncTask;
import android.util.Log;

import com.example.ecar_service_station.domain.User;
import com.example.ecar_service_station.dto.request.user.UpdateNotificationDto;
import com.example.ecar_service_station.dto.request.user.UpdatePasswordDto;
import com.example.ecar_service_station.dto.request.user.UpdateUserDto;
import com.example.ecar_service_station.dto.resoponse.common.CommonResponse;
import com.example.ecar_service_station.dto.resoponse.common.SingleResultResponse;
import com.example.ecar_service_station.infra.network.HttpConnectionProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;

public class UserBasicService extends AsyncTask<Integer, Void, CommonResponse> {

    private static final int USER_BASIC_SERVICE_GET_USER_INFO = -1;
    private static final int USER_BASIC_SERVICE_UPDATE_USER_INFO = -2;
    private static final int USER_BASIC_SERVICE_UPDATE_PASSWORD = -3;
    private static final int USER_BASIC_SERVICE_UPDATE_NOTIFICATION = -4;

    private final HttpConnectionProvider httpConnectionProvider;
    private final ObjectMapper objectMapper;
    private final String loginAccessToken;

    private UpdateUserDto updateUser;
    private UpdatePasswordDto updatePassword;
    private UpdateNotificationDto updateNotification;

    public UserBasicService(String loginAccessToken) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
    }

    public UserBasicService(String loginAccessToken, UpdateUserDto updateUserDto) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
        this.updateUser = updateUserDto;
    }

    public UserBasicService(String loginAccessToken, UpdatePasswordDto updatePasswordDto) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
        this.updatePassword = updatePasswordDto;
    }

    public UserBasicService(String loginAccessToken, UpdateNotificationDto updateNotificationDto) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
        this.updateNotification = updateNotificationDto;
    }

    @Override
    protected CommonResponse doInBackground(Integer... requestCode) {
        String hostServer = httpConnectionProvider.getHostServer();
        String URI = hostServer + "/user";

        try {
            HttpURLConnection httpURLConnection = null;

            switch (requestCode[0]) {
                case USER_BASIC_SERVICE_GET_USER_INFO:
                    httpURLConnection = httpConnectionProvider.createGETConnection(URI);

                    break;

                case USER_BASIC_SERVICE_UPDATE_USER_INFO:
                    httpURLConnection =
                            httpConnectionProvider.createPOSTConnection(URI, objectMapper.writeValueAsString(updateUser));

                    break;

                case USER_BASIC_SERVICE_UPDATE_PASSWORD:
                    URI += "/password";
                    httpURLConnection =
                            httpConnectionProvider.createPOSTConnection(URI, objectMapper.writeValueAsString(updatePassword));

                    break;

                case USER_BASIC_SERVICE_UPDATE_NOTIFICATION:
                    URI += "/notification";
                    httpURLConnection =
                            httpConnectionProvider.createPOSTConnection(URI, objectMapper.writeValueAsString(updateNotification));
            }

            // 헤더에 로그인 토큰 추가
            httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String jsonString = httpConnectionProvider.readData(httpURLConnection);

                if (requestCode[0] == USER_BASIC_SERVICE_GET_USER_INFO) {
                    return objectMapper.readValue(jsonString, new TypeReference<SingleResultResponse<User>>() {});
                }

                return objectMapper.readValue(jsonString, CommonResponse.class);

            } else {
                String jsonString = httpConnectionProvider.readError(httpURLConnection);

                return objectMapper.readValue(jsonString, CommonResponse.class);
            }

        } catch (JsonProcessingException e) {
            Log.w("User basic service", "ObjectMapper error");

        } catch (IOException e) {
            Log.w("User basic service", "Connection error");
        }

        return null;
    }
}

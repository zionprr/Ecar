package com.example.capstonemainproject.service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.example.capstonemainproject.domain.User;
import com.example.capstonemainproject.dto.request.user.UpdateNotificationDto;
import com.example.capstonemainproject.dto.request.user.UpdatePasswordDto;
import com.example.capstonemainproject.dto.request.user.UpdateUserDto;
import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.SingleResultResponse;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.network.HttpConnectionProvider;
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

    private Context context;
    private UpdateUserDto updateUser;
    private UpdatePasswordDto updatePassword;
    private UpdateNotificationDto updateNotification;

    public UserBasicService(String loginAccessToken) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
    }

    public UserBasicService(String loginAccessToken, Context context) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
        this.context = context;
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

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    break;

                case USER_BASIC_SERVICE_UPDATE_USER_INFO:
                    httpURLConnection = httpConnectionProvider.createPOSTConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    httpConnectionProvider.addData(httpURLConnection, objectMapper.writeValueAsString(updateUser));
                    break;

                case USER_BASIC_SERVICE_UPDATE_PASSWORD:
                    URI += "/password";
                    httpURLConnection = httpConnectionProvider.createPOSTConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    httpConnectionProvider.addData(httpURLConnection, objectMapper.writeValueAsString(updatePassword));
                    break;

                case USER_BASIC_SERVICE_UPDATE_NOTIFICATION:
                    URI += "/notification";
                    httpURLConnection = httpConnectionProvider.createPOSTConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    httpConnectionProvider.addData(httpURLConnection, objectMapper.writeValueAsString(updateNotification));
            }

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String jsonString = httpConnectionProvider.readData(httpURLConnection);

                if (requestCode[0] == USER_BASIC_SERVICE_GET_USER_INFO) {
                    SingleResultResponse<User> result =
                            objectMapper.readValue(jsonString, new TypeReference<SingleResultResponse<User>>() {});

                    if (context != null) {
                        User user = result.getData();

                        PreferenceManager.setString(context, "USER_NAME", user.getName());
                        PreferenceManager.setString(context, "USER_EMAIL", user.getEmail());
                        PreferenceManager.setInt(context, "USER_CASH", user.getCash());
                        PreferenceManager.setInt(context, "USER_CASH_POINT", user.getCashPoint());
                    }

                    return result;
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

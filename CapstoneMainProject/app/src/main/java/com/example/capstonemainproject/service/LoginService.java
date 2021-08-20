package com.example.capstonemainproject.service;

import android.os.AsyncTask;
import android.util.Log;

import com.example.capstonemainproject.dto.request.search.SearchConditionDto;
import com.example.capstonemainproject.dto.request.search.SearchLocationDto;
import com.example.capstonemainproject.dto.request.user.LoginDto;
import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.SingleResultResponse;
import com.example.capstonemainproject.infra.network.HttpConnectionProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;

public class LoginService extends AsyncTask<LoginDto, Void, CommonResponse> {

    private final HttpConnectionProvider httpConnectionProvider;
    private final ObjectMapper objectMapper;

    private SearchConditionDto searchCondition;
    private SearchLocationDto searchLocation;

    public LoginService() {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected CommonResponse doInBackground(LoginDto... loginDtos) {
        String hostServer = httpConnectionProvider.getHostServer();
        String URI = hostServer + "/login";

        try {
            HttpURLConnection postConnection = httpConnectionProvider.createPOSTConnection(URI);

            httpConnectionProvider.addData(postConnection, objectMapper.writeValueAsString(loginDtos[0]));

            if (postConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String jsonString = httpConnectionProvider.readData(postConnection);

                return objectMapper.readValue(jsonString, new TypeReference<SingleResultResponse<String>>() {});

            } else {
                String jsonString = httpConnectionProvider.readError(postConnection);

                return objectMapper.readValue(jsonString, CommonResponse.class);
            }

        } catch (JsonProcessingException e) {
            Log.w("Login service", "ObjectMapper error");

        } catch (IOException e) {
            Log.w("Login service", "Connection error");
        }

        return null;
    }
}

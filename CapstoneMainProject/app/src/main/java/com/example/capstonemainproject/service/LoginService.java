package com.example.capstonemainproject.service;

import com.example.capstonemainproject.dto.request.LoginDto;
import com.example.capstonemainproject.dto.response.CommonResponse;
import com.example.capstonemainproject.dto.response.SingleResultResponse;
import com.example.capstonemainproject.infra.HttpConnectionProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;

public class LoginService {

    private final HttpConnectionProvider httpConnectionProvider = new HttpConnectionProvider();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String loginAccessToken;
    private String message;
    private int responseCode;

    public String login(LoginDto loginDto) {
        Thread thread = new Thread(() -> {
            try {
                String hostServer = httpConnectionProvider.getHostServer();
                String URI = hostServer + "/user/login";

                HttpURLConnection postConnection =
                        httpConnectionProvider.createPOSTConnection(URI, objectMapper.writeValueAsString(loginDto));

                if (postConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    String jsonString = httpConnectionProvider.readData(postConnection);
                    SingleResultResponse response = objectMapper.readValue(jsonString, SingleResultResponse.class);

                    if (response.isSuccess()) {
                        loginAccessToken = response.getData();
                        responseCode = response.getResponseCode();
                        message = response.getMessage();
                    }

                } else {
                    String jsonString = httpConnectionProvider.readError(postConnection);
                    CommonResponse response = objectMapper.readValue(jsonString, CommonResponse.class);

                    responseCode = response.getResponseCode();
                    message = response.getMessage();
                }

            } catch (JsonProcessingException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });


        thread.start();

        return loginAccessToken;
    }

    public String getMessage() {
        return this.message;
    }

    public int getResponseCode() {
        return this.responseCode;
    }
}

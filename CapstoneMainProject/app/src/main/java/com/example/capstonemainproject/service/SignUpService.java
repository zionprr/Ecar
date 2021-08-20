package com.example.capstonemainproject.service;

import android.os.AsyncTask;
import android.util.Log;

import com.example.capstonemainproject.dto.request.user.SignUpDto;
import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.infra.network.HttpConnectionProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;

public class SignUpService extends AsyncTask<SignUpDto, Void, CommonResponse> {

    private final HttpConnectionProvider httpConnectionProvider;
    private final ObjectMapper objectMapper;

    public SignUpService() {
        this.httpConnectionProvider = new HttpConnectionProvider();         // HTTP 서비스
        this.objectMapper = new ObjectMapper();                             // 객체 <-> json 매핑 도구
    }

    @Override
    protected CommonResponse doInBackground(SignUpDto... signUpDtos) {
        String hostServer = httpConnectionProvider.getHostServer();
        String URI = hostServer + "/sign-up";

        try {
            // POST 커넥션 획득(요청 객체 -> json)
            HttpURLConnection postConnection = httpConnectionProvider.createPOSTConnection(URI);

            httpConnectionProvider.addData(postConnection, objectMapper.writeValueAsString(signUpDtos[0]));

            // 응답(서버로부터) 성공
            if (postConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String jsonString = httpConnectionProvider.readData(postConnection);        // 응답 데이터 -> json

                return objectMapper.readValue(jsonString, CommonResponse.class);            // json -> 응답 객체

            } else {    // 응답 실패
                String jsonString = httpConnectionProvider.readError(postConnection);

                return objectMapper.readValue(jsonString, CommonResponse.class);
            }

        } catch (JsonProcessingException e) {
            Log.w("Sign-up service", "ObjectMapper error");

        } catch (IOException e) {
            Log.w("Sign-up service", "Connection error");
        }

        return null;
    }
}

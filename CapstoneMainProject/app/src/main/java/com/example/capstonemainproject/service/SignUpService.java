package com.example.capstonemainproject.service;

import com.example.capstonemainproject.dto.request.SignUpDto;
import com.example.capstonemainproject.dto.response.CommonResponse;
import com.example.capstonemainproject.infra.HttpConnectionProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;

public class SignUpService {

    private final HttpConnectionProvider httpConnectionProvider = new HttpConnectionProvider(); // Http 서비스
    private final ObjectMapper objectMapper = new ObjectMapper();  // 객체와 json 매핑

    private String message;
    private int responseCode;

    public void signUp(SignUpDto signUpDto) {
        Thread thread = new Thread(() -> {
            try {
                String hostServer = httpConnectionProvider.getHostServer();
                String URI = hostServer + "/user/sign-up";  // signUp URI

                HttpURLConnection postConnection =
                        httpConnectionProvider.createPOSTConnection(URI, objectMapper.writeValueAsString(signUpDto));  // POST Http 커넥션 획득(요청 객체 -> json)

                if (postConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {  // 회원가입 성공
                    String jsonString = httpConnectionProvider.readData(postConnection); // 응답 데이터를 json 파싱
                    CommonResponse response = objectMapper.readValue(jsonString, CommonResponse.class); // json -> 응답 객체 변환

                    if (response.isSuccess()) {
                        responseCode = response.getResponseCode();
                        message = response.getMessage();
                    }

                } else {  // 회원가입 실패
                    String jsonString = httpConnectionProvider.readError(postConnection);
                    CommonResponse response = objectMapper.readValue(jsonString, CommonResponse.class);

                    responseCode = response.getResponseCode();  // 실패 코드
                    message = response.getMessage();  // 실패 사유
                }

            } catch (JsonProcessingException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        thread.start();
    }

    public String getMessage() { // getter 생성
        return this.message;
    }

    public int getResponseCode() {
        return this.responseCode;
    }
}

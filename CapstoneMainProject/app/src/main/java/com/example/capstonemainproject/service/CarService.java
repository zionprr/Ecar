package com.example.capstonemainproject.service;

import android.os.AsyncTask;
import android.util.Log;

import com.example.capstonemainproject.domain.Car;
import com.example.capstonemainproject.dto.request.car.RegisterCarDto;
import com.example.capstonemainproject.dto.resoponse.common.CommonResponse;
import com.example.capstonemainproject.dto.resoponse.common.ListResultResponse;
import com.example.capstonemainproject.infra.network.HttpConnectionProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;

public class CarService extends AsyncTask<Integer, Void, CommonResponse> {

    private static final int CAR_SERVICE_GET_USER_CARS = -5;
    private static final int CAR_SERVICE_REGISTER_USER_CAR = -6;
    private static final int CAR_SERVICE_DELETE_USER_CAR = -7;

    private final HttpConnectionProvider httpConnectionProvider;
    private final ObjectMapper objectMapper;
    private final String loginAccessToken;

    private RegisterCarDto registerCarDto;
    private long carId;

    public CarService(String loginAccessToken) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
    }

    public CarService(String loginAccessToken, RegisterCarDto registerCarDto) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
        this.registerCarDto = registerCarDto;
    }

    public CarService(String loginAccessToken, long carId) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
        this.carId = carId;
    }

    @Override
    protected CommonResponse doInBackground(Integer... requestCode) {
        String hostServer = httpConnectionProvider.getHostServer();
        String URI = hostServer + "/user/car";

        try {
            HttpURLConnection httpURLConnection = null;

            switch (requestCode[0]) {
                case CAR_SERVICE_GET_USER_CARS:
                    httpURLConnection = httpConnectionProvider.createGETConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    break;

                case CAR_SERVICE_REGISTER_USER_CAR:
                    URI += "/register";
                    httpURLConnection = httpConnectionProvider.createPOSTConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    httpConnectionProvider.addData(httpURLConnection, objectMapper.writeValueAsString(registerCarDto));
                    break;

                case CAR_SERVICE_DELETE_USER_CAR:
                    URI += ("/" + carId);
                    httpURLConnection = httpConnectionProvider.createDELETEConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
            }

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String jsonString = httpConnectionProvider.readData(httpURLConnection);

                if (requestCode[0] == CAR_SERVICE_GET_USER_CARS) {
                    ListResultResponse<Car> result =
                            objectMapper.readValue(jsonString, new TypeReference<ListResultResponse<Car>>() {});

                    return result;
                }

                return objectMapper.readValue(jsonString, CommonResponse.class);

            } else {
                String jsonString = httpConnectionProvider.readError(httpURLConnection);

                return objectMapper.readValue(jsonString, CommonResponse.class);
            }

        } catch (JsonProcessingException e) {
            Log.w("Car service", "ObjectMapper error");

        } catch (IOException e) {
            Log.w("Car service", "Connection error");
        }

        return null;
    }
}

package com.example.capstonemainproject.service;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.example.capstonemainproject.domain.ReservationStatement;
import com.example.capstonemainproject.dto.request.reservation.PayReservationDto;
import com.example.capstonemainproject.dto.request.reservation.RequestReservationDto;
import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.SingleResultResponse;
import com.example.capstonemainproject.dto.response.custom.reservation.ChargerTimeTableDto;
import com.example.capstonemainproject.dto.response.custom.reservation.MaxEndDateTimeDto;
import com.example.capstonemainproject.dto.response.custom.reservation.SimpleReservationInfoDto;
import com.example.capstonemainproject.infra.network.HttpConnectionProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ReservationService extends AsyncTask<Integer, Void, CommonResponse> {

    private static final int RESERVATION_SERVICE_GET_TIME_TABLE = -26;
    private static final int RESERVATION_SERVICE_GET_MAX_END_DATE = -27;
    private static final int RESERVATION_SERVICE_RESERVE = -28;
    private static final int RESERVATION_SERVICE_PAY = -29;
    private static final int RESERVATION_SERVICE_CANCEL = -30;
    private static final int RESERVATION_SERVICE_GET_SIMPLE_INFO = -31;

    private final HttpConnectionProvider httpConnectionProvider;
    private final ObjectMapper objectMapper;
    private final String loginAccessToken;

    private long chargerId, reservationId;
    private String startDateTime, reservedTitle;
    private RequestReservationDto requestReservationDto;
    private PayReservationDto payReservationDto;

    public ReservationService(String loginAccessToken) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
    }

    public ReservationService(String loginAccessToken, long chargerId, String startDateTime) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
        this.chargerId = chargerId;

        this.startDateTime =
                LocalDateTime
                        .parse(startDateTime, DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm"))
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
    }

    public ReservationService(String loginAccessToken, String reservedTitle) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
        this.reservedTitle = reservedTitle;
    }

    public ReservationService(String loginAccessToken, RequestReservationDto requestReservationDto) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
        this.requestReservationDto = requestReservationDto;
    }

    public ReservationService(String loginAccessToken, PayReservationDto payReservationDto) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
        this.payReservationDto = payReservationDto;
    }

    public void setChargerId(long chargerId) {
        this.chargerId = chargerId;
    }

    public void setReservationId(long reservationId) {
        this.reservationId = reservationId;
    }

    @Override
    protected CommonResponse doInBackground(Integer... requestCode) {
        String hostServer = httpConnectionProvider.getHostServer();
        String URI = hostServer + "/ecar/reserve";

        try {
            HttpURLConnection httpURLConnection = null;

            switch (requestCode[0]) {
                case RESERVATION_SERVICE_GET_TIME_TABLE:
                    URI += ("/" + chargerId + "?day=" + requestCode[1]);
                    httpURLConnection = httpConnectionProvider.createGETConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    break;

                case RESERVATION_SERVICE_GET_MAX_END_DATE:
                    URI += ("/" + chargerId + "/max?startDateTIme=" + startDateTime);
                    httpURLConnection = httpConnectionProvider.createGETConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    break;

                case RESERVATION_SERVICE_RESERVE:
                    httpURLConnection = httpConnectionProvider.createPOSTConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    httpConnectionProvider.addData(httpURLConnection, objectMapper.writeValueAsString(requestReservationDto));
                    break;

                case RESERVATION_SERVICE_PAY:
                    URI += "/payment";
                    httpURLConnection = httpConnectionProvider.createPOSTConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    httpConnectionProvider.addData(httpURLConnection, objectMapper.writeValueAsString(payReservationDto));
                    break;

                case RESERVATION_SERVICE_CANCEL:
                    URI += ("?reserveTitle=" + reservedTitle);
                    httpURLConnection = httpConnectionProvider.createDELETEConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    break;

                case RESERVATION_SERVICE_GET_SIMPLE_INFO:
                    URI += ("/" + reservationId + "/info");
                    httpURLConnection = httpConnectionProvider.createGETConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
            }

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String jsonString = httpConnectionProvider.readData(httpURLConnection);

                if (requestCode[0] == RESERVATION_SERVICE_GET_TIME_TABLE) {
                    return objectMapper.readValue(jsonString, new TypeReference<SingleResultResponse<ChargerTimeTableDto>>() {});

                } else if (requestCode[0] == RESERVATION_SERVICE_GET_MAX_END_DATE) {
                    return objectMapper.readValue(jsonString, new TypeReference<SingleResultResponse<MaxEndDateTimeDto>>() {});

                } else if(requestCode[0] == RESERVATION_SERVICE_GET_SIMPLE_INFO) {
                    return objectMapper.readValue(jsonString, new TypeReference<SingleResultResponse<SimpleReservationInfoDto>>() {});
                }

                return objectMapper.readValue(jsonString, new TypeReference<SingleResultResponse<ReservationStatement>>() {});

            } else {
                String jsonString = httpConnectionProvider.readError(httpURLConnection);

                return objectMapper.readValue(jsonString, CommonResponse.class);
            }

        } catch (JsonProcessingException e) {
            Log.w("Reservation service", "ObjectMapper error");

        } catch (IOException e) {
            Log.w("Reservation service", "Connection error");
        }

        return null;
    }
}

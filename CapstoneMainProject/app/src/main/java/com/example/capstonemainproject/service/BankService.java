package com.example.capstonemainproject.service;

import android.os.AsyncTask;
import android.util.Log;

import com.example.capstonemainproject.domain.BankAccount;
import com.example.capstonemainproject.dto.request.bank.AuthBankAccountDto;
import com.example.capstonemainproject.dto.request.bank.CashInDto;
import com.example.capstonemainproject.dto.request.bank.CashOutDto;
import com.example.capstonemainproject.dto.request.bank.RegisterBankAccountDto;
import com.example.capstonemainproject.dto.resoponse.common.CommonResponse;
import com.example.capstonemainproject.dto.resoponse.common.ListResultResponse;
import com.example.capstonemainproject.dto.resoponse.common.SingleResultResponse;
import com.example.capstonemainproject.dto.resoponse.custom.RegisteredBankAccountDto;
import com.example.capstonemainproject.infra.network.HttpConnectionProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;

public class BankService extends AsyncTask<Integer, Void, CommonResponse> {

    private static final int BANK_SERVICE_GET_USER_ACCOUNTS = -8;
    private static final int BANK_SERVICE_REGISTER_USER_ACCOUNT = -9;
    private static final int BANK_SERVICE_AUTH_USER_ACCOUNT = -10;
    private static final int BANK_SERVICE_DELETE_USER_ACCOUNT = -11;
    private static final int BANK_SERVICE_CHANGE_MAIN_USED_ACCOUNT = -12;
    private static final int BANK_SERVICE_CHARGE_CASH = -13;
    private static final int BANK_SERVICE_REFUND_CASH = -14;

    private final HttpConnectionProvider httpConnectionProvider;
    private final ObjectMapper objectMapper;
    private final String loginAccessToken;

    private RegisterBankAccountDto registerBankAccountDto;
    private AuthBankAccountDto authBankAccountDto;
    private CashInDto cashInDto;
    private CashOutDto cashOutDto;
    private long bankId;

    public BankService(String loginAccessToken) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
    }

    public BankService(String loginAccessToken, RegisterBankAccountDto registerBankAccountDto) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
        this.registerBankAccountDto = registerBankAccountDto;
    }

    public BankService(String loginAccessToken, AuthBankAccountDto authBankAccountDto) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
        this.authBankAccountDto = authBankAccountDto;
    }

    public BankService(String loginAccessToken, CashInDto cashInDto) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
        this.cashInDto = cashInDto;
    }

    public BankService(String loginAccessToken, CashOutDto cashOutDto) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
        this.cashOutDto = cashOutDto;
    }

    public BankService(String loginAccessToken, long bankId) {
        this.httpConnectionProvider = new HttpConnectionProvider();
        this.objectMapper = new ObjectMapper();
        this.loginAccessToken = loginAccessToken;
        this.bankId = bankId;
    }

    @Override
    protected CommonResponse doInBackground(Integer... requestCode) {
        String hostServer = httpConnectionProvider.getHostServer();
        String URI = hostServer + "/user/bank";

        try {
            HttpURLConnection httpURLConnection = null;

            switch (requestCode[0]) {
                case BANK_SERVICE_GET_USER_ACCOUNTS:
                    httpURLConnection = httpConnectionProvider.createGETConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    break;

                case BANK_SERVICE_REGISTER_USER_ACCOUNT:
                    URI += "/register";
                    httpURLConnection = httpConnectionProvider.createPOSTConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    httpConnectionProvider.addData(httpURLConnection, objectMapper.writeValueAsString(registerBankAccountDto));
                    break;

                case BANK_SERVICE_AUTH_USER_ACCOUNT:
                    URI += "/auth";
                    httpURLConnection = httpConnectionProvider.createPOSTConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    httpConnectionProvider.addData(httpURLConnection, objectMapper.writeValueAsString(authBankAccountDto));
                    break;

                case BANK_SERVICE_DELETE_USER_ACCOUNT:
                    URI += ("/" + bankId);

                    httpURLConnection = httpConnectionProvider.createDELETEConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    break;

                case BANK_SERVICE_CHANGE_MAIN_USED_ACCOUNT:
                    URI += ("/main-used/" + bankId);
                    httpURLConnection = httpConnectionProvider.createPOSTConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    break;

                case BANK_SERVICE_CHARGE_CASH:
                    URI += "/cash-in";
                    httpURLConnection = httpConnectionProvider.createPOSTConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    httpConnectionProvider.addData(httpURLConnection, objectMapper.writeValueAsString(cashInDto));
                    break;

                case BANK_SERVICE_REFUND_CASH:
                    URI += "/cash-out";
                    httpURLConnection = httpConnectionProvider.createPOSTConnection(URI);

                    httpConnectionProvider.addHeader(httpURLConnection, "X-AUTH-TOKEN", loginAccessToken);
                    httpConnectionProvider.addData(httpURLConnection, objectMapper.writeValueAsString(cashOutDto));
            }

            if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                String jsonString = httpConnectionProvider.readData(httpURLConnection);

                if (requestCode[0] == BANK_SERVICE_GET_USER_ACCOUNTS) {
                    ListResultResponse<BankAccount> result =
                            objectMapper.readValue(jsonString, new TypeReference<ListResultResponse<BankAccount>>() {});

                    return result;
                }

                if (requestCode[0] == BANK_SERVICE_REGISTER_USER_ACCOUNT) {
                    SingleResultResponse<RegisteredBankAccountDto> result =
                            objectMapper.readValue(jsonString, new TypeReference<SingleResultResponse<RegisteredBankAccountDto>>() {});

                    return result;
                }

                return objectMapper.readValue(jsonString, CommonResponse.class);

            } else {
                String jsonString = httpConnectionProvider.readError(httpURLConnection);

                return objectMapper.readValue(jsonString, CommonResponse.class);
            }


        } catch (JsonProcessingException e) {
            Log.w("Bank service", "ObjectMapper error");

        } catch (IOException e) {
            Log.w("Bank service", "Connection error");
        }

        return null;
    }
}

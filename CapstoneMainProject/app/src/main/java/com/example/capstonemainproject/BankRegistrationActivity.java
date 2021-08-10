package com.example.capstonemainproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.capstonemainproject.dto.request.bank.AuthBankAccountDto;
import com.example.capstonemainproject.dto.request.bank.RegisterBankAccountDto;
import com.example.capstonemainproject.dto.resoponse.common.CommonResponse;
import com.example.capstonemainproject.dto.resoponse.common.SingleResultResponse;
import com.example.capstonemainproject.dto.resoponse.custom.RegisteredBankAccountDto;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.BankService;

import java.util.concurrent.ExecutionException;

public class BankRegistrationActivity extends AppCompatActivity {

    private static final int BANK_REGISTRATION_ACTIVITY_RESULT_OK = 101;
    private static final int BANK_SERVICE_REGISTER_USER_ACCOUNT = -9;
    private static final int BANK_SERVICE_AUTH_USER_ACCOUNT = -10;

    private Toolbar toolbarBankRegistration;

    private EditText eTextBankName, eTextAccountNumber, eTextOwner;
    private EditText eTextAuthMsg, eTextPayPassword, eTextPayPasswordCheck;
    private Button btnAccountAuth, btnAccountRegister;

    private BankService bankService;

    private Long registeredBankId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_registration);

        // 로그인 토큰 저장
        saveLoginToken();

        // 화면 설정
        toolbarBankRegistration = findViewById(R.id.toolbar_bank_registration);
        eTextBankName = findViewById(R.id.editText_account_bankName);
        eTextAccountNumber = findViewById(R.id.editText_account_number);
        eTextOwner = findViewById(R.id.editText_account_owner);
        eTextAuthMsg = findViewById(R.id.editText_account_auth_msg);
        eTextPayPassword = findViewById(R.id.editText_account_pay_password);
        eTextPayPasswordCheck = findViewById(R.id.editText_account_pay_password_check);
        btnAccountAuth = findViewById(R.id.btn_account_auth);
        btnAccountRegister = findViewById(R.id.btn_account_register);

        // 상단바 설정
        settingActionBar();

        // 화면 동작(1) : 계좌 인증
        btnAccountAuth.setOnClickListener(v -> {
            String bankName = eTextBankName.getText().toString();
            String accountNumber = eTextAccountNumber.getText().toString();
            String owner = eTextOwner.getText().toString();

            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.BankRegistrationActivity.this, "LOGIN_ACCESS_TOKEN");
            RegisterBankAccountDto registerBankAccountDto = getRegisterBankAccountDto(bankName, accountNumber, owner);

            bankService = new BankService(loginAccessToken, registerBankAccountDto);

            try {
                CommonResponse commonResponse = bankService.execute(BANK_SERVICE_REGISTER_USER_ACCOUNT).get();

                if (commonResponse.isSuccess()) {
                    SingleResultResponse<RegisteredBankAccountDto> singleResultResponse =
                            (SingleResultResponse<RegisteredBankAccountDto>) commonResponse;

                    RegisteredBankAccountDto registeredResponse = singleResultResponse.getData();
                    registeredBankId = registeredResponse.getBankId();

                    String accountAuthMsg = String.format("계좌 인증 메시지 : %s", registeredResponse.getMsg());

                    SnackBarManager.showIndefiniteMessage(v, accountAuthMsg, "SKIP");

                } else {
                    String registerAccountFailedMsg = "입력된 계좌 정보가 부족하거나 중복된 계좌 번호입니다.";

                    SnackBarManager.showMessage(v, registerAccountFailedMsg);
                }

            } catch (ExecutionException | InterruptedException e) {
                Log.w("Bank", "Registration user account failed.");
            }
        });

        // 화면 동작(2) : 새 계좌 추가
        btnAccountRegister.setOnClickListener(v -> {
            if (registeredBankId == null) {
                String authAccountFailedMsg = "계좌를 먼저 등록해주세요.";

                SnackBarManager.showMessage(v, authAccountFailedMsg);

            } else {
                String authMsg = eTextAuthMsg.getText().toString();
                String payPassword = eTextPayPassword.getText().toString();
                String payPasswordCheck = eTextPayPasswordCheck.getText().toString();

                if (!payPassword.equals(payPasswordCheck)) {
                    eTextPayPassword.setText("");
                    eTextPayPasswordCheck.setText("");
                    eTextPayPassword.requestFocus();

                    String passwordMismatchedMsg = "결제 비밀번호가 일치하지 않습니다.";

                    SnackBarManager.showMessage(v, passwordMismatchedMsg);

                } else {
                    String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.BankRegistrationActivity.this, "LOGIN_ACCESS_TOKEN");
                    AuthBankAccountDto authBankAccountDto = getAuthBankAccountDto(registeredBankId.longValue(), authMsg, payPassword);

                    bankService = new BankService(loginAccessToken, authBankAccountDto);

                    try {
                        CommonResponse commonResponse = bankService.execute(BANK_SERVICE_AUTH_USER_ACCOUNT).get();

                        if (commonResponse.isSuccess()) {
                            setResult(BANK_REGISTRATION_ACTIVITY_RESULT_OK);
                            finish();

                        } else {
                            String authAccountFailedMsg = "계좌 인증 메시지를 다시 확인해 주세요.";

                            SnackBarManager.showMessage(findViewById(R.id.scrollView_bank_registration), authAccountFailedMsg);
                        }

                    } catch (ExecutionException | InterruptedException e) {
                        Log.w("Bank", "Auth user account failed.");
                    }
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();

            return true;

        } else if (item.getItemId() == R.id.action_home) {
            finish();
            startActivity(new Intent(com.example.capstonemainproject.BankRegistrationActivity.this, MainActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

    }

    private void saveLoginToken() {
        if (getIntent().hasExtra("LOGIN_ACCESS_TOKEN")) {
            String loginAccessToken = getIntent().getStringExtra("LOGIN_ACCESS_TOKEN");

            PreferenceManager.setString(com.example.capstonemainproject.BankRegistrationActivity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
        }
    }

    private void settingActionBar() {
        setSupportActionBar(toolbarBankRegistration);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_left_solid);
    }

    private RegisterBankAccountDto getRegisterBankAccountDto(String bankName, String accountNumber, String owner) {
        RegisterBankAccountDto registerBankAccountDto = new RegisterBankAccountDto();
        registerBankAccountDto.setBankName(bankName);
        registerBankAccountDto.setBankAccountNumber(accountNumber);
        registerBankAccountDto.setBankAccountOwner(owner);
        registerBankAccountDto.setCertificateId(1L);
        registerBankAccountDto.setCertificatePassword("CERTIFICATE_PASSWORD");

        return registerBankAccountDto;
    }

    private AuthBankAccountDto getAuthBankAccountDto(long bankId, String authMsg, String payPassword) {
        AuthBankAccountDto authBankAccountDto = new AuthBankAccountDto();
        authBankAccountDto.setBankId(bankId);
        authBankAccountDto.setAuthMsg(authMsg);
        authBankAccountDto.setPaymentPassword(payPassword);

        return authBankAccountDto;
    }
}
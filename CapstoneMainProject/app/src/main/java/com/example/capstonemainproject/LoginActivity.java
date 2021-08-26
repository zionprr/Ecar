package com.example.capstonemainproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonemainproject.dto.request.user.LoginDto;
import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.SingleResultResponse;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.LoginService;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.concurrent.ExecutionException;

public class LoginActivity extends AppCompatActivity {

    private EditText eTextEmail, eTextPassword;
    private Button btnLogin, btnSignUp;

    private LoginService loginService;

    // StartActivityForResult (회원가입)
    private final ActivityResultLauncher<Intent> startActivityResultForSignUp =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        // 회원가입 성공
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            String signUpSuccessMsg = "회원가입이 되었습니다.\n서비스 이용을 위해 이메일 인증을 완료해주세요.";

                            SnackBarManager.showMessage(findViewById(R.id.layout_login), signUpSuccessMsg);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // FCM 디바이스 토큰 로컬 저장
        saveDeviceToken();

        // 화면 설정
        eTextEmail = findViewById(R.id.editText_login_email);
        eTextPassword = findViewById(R.id.editText_login_password);
        btnLogin = findViewById(R.id.btn_login);
        btnSignUp = findViewById(R.id.btn_signUp);

        // 로그인 서비스 주입
        loginService = new LoginService();

        // 화면 동작(1) : 로그인 버튼
        btnLogin.setOnClickListener(v -> {
            String email = eTextEmail.getText().toString().trim();
            String password = eTextPassword.getText().toString().trim();
            String deviceToken = PreferenceManager.getString(LoginActivity.this, "DEVICE_TOKEN");

            if (deviceToken.isEmpty()) {
                String deviceTokenNullMsg = "구글 플레이 서비스 로그인이 필요합니다.";

                SnackBarManager.showMessage(v, deviceTokenNullMsg);

            } else {
                // 로그인 요청 객체 생성
                LoginDto loginDto = getLoginDto(email, password, deviceToken);

                // 로그인
                try {
                    CommonResponse commonResponse = loginService.execute(loginDto).get();

                    if (commonResponse.isSuccess()) {       // 로그인 성공
                        SingleResultResponse<String> singleResultResponse = (SingleResultResponse<String>) commonResponse;

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("LOGIN_ACCESS_TOKEN", singleResultResponse.getData());

                        startActivity(intent);

                    } else {                                // 로그인 실패
                        String loginFailedMsg = null;

                        if (commonResponse.getResponseCode() == -1) {
                            loginFailedMsg = "로그인 아이디(이메일)와 비밀번호를 모두 입력하세요.";

                        } else {
                            loginFailedMsg = commonResponse.getMessage();
                        }

                        SnackBarManager.showMessage(v, loginFailedMsg);
                    }

                } catch (ExecutionException | InterruptedException e) {
                    Log.w("Login", "Login failed.");
                }
            }
        });

        // 화면 동작(2) : 회원가입 버튼
        btnSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);

            startActivityResultForSignUp.launch(intent);
        });
    }

    @Override
    public void onBackPressed() {

    }

    private void saveDeviceToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                            if (!task.isSuccessful()) {
                                Log.w("FCM", "Token registration failed.", task.getException());
                            }

                            PreferenceManager.setString(LoginActivity.this, "DEVICE_TOKEN", task.getResult());
                        }
                );
    }

    private LoginDto getLoginDto(String email, String password, String deviceToken) {
        LoginDto loginDto = new LoginDto();
        loginDto.setEmail(email);
        loginDto.setPassword(password);
        loginDto.setDeviceToken(deviceToken);

        return loginDto;
    }
}
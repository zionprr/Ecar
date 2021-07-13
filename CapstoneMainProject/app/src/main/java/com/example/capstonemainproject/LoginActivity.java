package com.example.capstonemainproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonemainproject.dto.request.LoginDto;
import com.example.capstonemainproject.service.LoginService;
import com.google.android.material.snackbar.Snackbar;

public class LoginActivity extends AppCompatActivity {

    private final LoginService loginService = new LoginService();

    private EditText eTextLoginEmail, eTextLoginPassword;
    private Button btnLogin, btnSignUp;
    private String LOGIN_ACCESS_TOKEN;

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // 화면 설정
        eTextLoginEmail = findViewById(R.id.editText_login_email);
        eTextLoginPassword = findViewById(R.id.editText_login_password);
        btnLogin = findViewById(R.id.btn_login);
        btnSignUp = findViewById(R.id.btn_signUp);

        // 회원가입 성공시
        Intent currentIntent = getIntent();

        if (currentIntent.hasExtra("SignUpSuccess") &&
                currentIntent.getExtras().getBoolean("SignUpSuccess")) {

            View layout = findViewById(R.id.layout_login);
            Snackbar.make(layout, "회원가입이 되었습니다.\n서비스 이용을 위해 이메일 인증을 완료해주세요.", Snackbar.LENGTH_LONG).show();
        }

        // 화면 동작(1) : 로그인 버튼
        btnLogin.setOnClickListener(v -> {
            String email = eTextLoginEmail.getText().toString().trim();
            String password = eTextLoginPassword.getText().toString().trim();

            // 로그인 요청 객체 생성
            LoginDto loginDto = new LoginDto();
            loginDto.setEmail(email);
            loginDto.setPassword(password);

            // 로그인
            LOGIN_ACCESS_TOKEN = loginService.login(loginDto);

            if (LOGIN_ACCESS_TOKEN != null) {    // 로그인 성공
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("LOGIN_ACCESS_TOKEN", LOGIN_ACCESS_TOKEN);

                startActivity(intent);

            } else {                            // 로그인 실패
                String message = loginService.getMessage();

                if (message != null) {
                    Snackbar.make(v, message, Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        // 화면 동작(2) : 회원가입 버튼
        btnSignUp.setOnClickListener(v -> {
            startActivity(new Intent(this, SignUpActivity.class));
        });
    }
}

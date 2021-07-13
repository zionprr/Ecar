package com.example.capstonemainproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.capstonemainproject.dto.request.SignUpDto;
import com.example.capstonemainproject.service.SignUpService;
import com.google.android.material.snackbar.Snackbar;

public class SignUpActivity extends AppCompatActivity {

    private final SignUpService signUpService = new SignUpService();
    private final int RESPONSE_OK = 0;

    private EditText eTextSignUpName, eTextSignUpPhoneNumber, eTextSignUpEmail, eTextSignUpPwd, eTextSignUpPwdCheck;
    private Button btnSignUpRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // 화면 설정
        eTextSignUpName = findViewById(R.id.editText_SignUp_name);
        eTextSignUpPhoneNumber = findViewById(R.id.editText_SignUp_phoneNumber);
        eTextSignUpEmail = findViewById(R.id.editText_SignUp_email);
        eTextSignUpPwd = findViewById(R.id.editText_SignUp_pwd);
        eTextSignUpPwdCheck = findViewById(R.id.editText_SignUp_pwdcheck);
        btnSignUpRegister = findViewById(R.id.btn_signUp_register);

        // Intent redirection check
        Intent currentIntent = getIntent();

        if (currentIntent.hasExtra("passwordMatched") &&
                !currentIntent.getExtras().getBoolean("passwordMatched")) {

            String name = currentIntent.getExtras().getString("name");
            String phoneNumber = currentIntent.getExtras().getString("phoneNumber");
            String email = currentIntent.getExtras().getString("email");

            eTextSignUpName.setText(name);
            eTextSignUpPhoneNumber.setText(phoneNumber);
            eTextSignUpEmail.setText(email);

            View layout = findViewById(R.id.layout_SignUp);
            Snackbar.make(layout, "비밀번호가 일치하지 않습니다.", Snackbar.LENGTH_LONG).show();
        }

        // 화면 동작 : 등록 버튼
        btnSignUpRegister.setOnClickListener(v -> {
            String name = eTextSignUpName.getText().toString().trim();
            String phoneNumber = eTextSignUpPhoneNumber.getText().toString().trim();
            String email = eTextSignUpEmail.getText().toString().trim();
            String password = eTextSignUpPwd.getText().toString().trim();
            String password_check = eTextSignUpPwdCheck.getText().toString().trim();

            if (!password.equals(password_check)) {
                Intent intent = new Intent(this, SignUpActivity.class);
                intent.putExtra("passwordMatched", false);
                intent.putExtra("name", name);
                intent.putExtra("phoneNumber", phoneNumber);
                intent.putExtra("email", email);

                startActivity(intent);

            } else {

                // 회원가입 요청 객체 생성
                SignUpDto signUpDto = new SignUpDto();
                signUpDto.setUserName(name);
                signUpDto.setEmail(email);
                signUpDto.setPhoneNumber(phoneNumber);
                signUpDto.setPassword(password);

                // 회원가입(signUp)
                signUpService.signUp(signUpDto);

                // 서버 OK 응답 확인(getter로 받은 응답 코드 저장)
                int responseCode = signUpService.getResponseCode();

                if (responseCode == RESPONSE_OK) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra("SignUpSuccess", true);

                    startActivity(intent);

                } else {
                    Snackbar.make(v, signUpService.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });

    }
}

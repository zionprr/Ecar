package com.example.capstonemainproject;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.example.capstonemainproject.dto.request.user.SignUpDto;
import com.example.capstonemainproject.dto.resoponse.common.CommonResponse;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.SignUpService;

import java.util.concurrent.ExecutionException;

public class SignUpActivity extends AppCompatActivity {

    private EditText eTextName, eTextPhoneNumber, eTextEmail, eTextPassword, eTextPasswordCheck;
    private Button btnRegister;

    private SignUpService signUpService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // 화면 설정
        eTextName = findViewById(R.id.editText_signUp_name);
        eTextPhoneNumber = findViewById(R.id.editText_signUp_phoneNumber);
        eTextEmail = findViewById(R.id.editText_signUp_email);
        eTextPassword = findViewById(R.id.editText_signUp_password);
        eTextPasswordCheck = findViewById(R.id.editText_signUp_passwordCheck);
        btnRegister = findViewById(R.id.btn_signUp_register);

        // 회원가입 서비스 주입
        signUpService = new SignUpService();

        // 화면 동작 : 가입 버튼
        btnRegister.setOnClickListener(v -> {
            String name = eTextName.getText().toString().trim();
            String phoneNumber = eTextPhoneNumber.getText().toString().trim();
            String email = eTextEmail.getText().toString().trim();
            String password = eTextPassword.getText().toString().trim();
            String password_check = eTextPasswordCheck.getText().toString().trim();

            // 회원가입 폼(비밀번호) 검증
            if (!password.equals(password_check)) {
                eTextPassword.setText("");
                eTextPasswordCheck.setText("");
                eTextPassword.requestFocus();

                String passwordMismatchedMsg = "비밀번호가 일치하지 않습니다.";

                SnackBarManager.showMessage(v, passwordMismatchedMsg);

            } else {
                // 회원가입 요청 객체 생성
                SignUpDto signUpDto = getSignUpDto(name, phoneNumber, email, password);

                // 회원가입
                try {
                    CommonResponse commonResponse = signUpService.execute(signUpDto).get();

                    if (commonResponse.isSuccess()) {       // 회원가입 성공
                        String authEmailRequestMsg = "인증 메일 요청 중입니다.";

                        SnackBarManager.showMessage(v, authEmailRequestMsg);

                        setResult(Activity.RESULT_OK);
                        finish();

                    } else {                                // 회원가입 실패
                        String signUpFailedMsg = null;

                        if (commonResponse.getResponseCode() == -1) {
                            signUpFailedMsg = "가입 정보를 모두 입력하세요.";

                        } else {
                            signUpFailedMsg = commonResponse.getMessage();
                        }

                        SnackBarManager.showMessage(v, signUpFailedMsg);
                    }

                } catch (ExecutionException | InterruptedException e) {
                    Log.w("Sign-up", "Sign-up failed.");
                }
            }
        });
    }

    private SignUpDto getSignUpDto(String name, String phoneNumber, String email, String password) {
        SignUpDto signUpDto = new SignUpDto();
        signUpDto.setUserName(name);
        signUpDto.setEmail(email);
        signUpDto.setPhoneNumber(phoneNumber);
        signUpDto.setPassword(password);

        return signUpDto;
    }
}
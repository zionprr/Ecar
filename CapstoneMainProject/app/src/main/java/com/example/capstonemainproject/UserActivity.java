package com.example.capstonemainproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.capstonemainproject.domain.User;
import com.example.capstonemainproject.dto.resoponse.common.CommonResponse;
import com.example.capstonemainproject.dto.resoponse.common.SingleResultResponse;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.UserBasicService;

import java.util.concurrent.ExecutionException;

public class UserActivity extends AppCompatActivity {

    private static final int CAR_REGISTRATION_ACTIVITY_RESULT_OK = 100;
    private static final int BANK_REGISTRATION_ACTIVITY_RESULT_OK = 101;
    private static final int USER_BASIC_SERVICE_GET_USER_INFO = -1;

    private Toolbar toolbarUser;

    private TextView textProfileName, textProfileEmail, textProfilePhoneNumber;
    private TextView textUserCash, textUserCashPoint;
    private LinearLayout layoutUserSetting, layoutUserPassword, layoutUserNotification;
    private LinearLayout layoutCarList, layoutNewCar;
    private LinearLayout layoutAccountList, layoutNewAccount, layoutUserCash;

    private UserBasicService userBasicService;

    private final ActivityResultLauncher<Intent> startActivityResultForUser =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == CAR_REGISTRATION_ACTIVITY_RESULT_OK) {
                            startActivity(new Intent(com.example.capstonemainproject.UserActivity.this, com.example.capstonemainproject.CarActivity.class));

                        } else if (result.getResultCode() == BANK_REGISTRATION_ACTIVITY_RESULT_OK) {
                            startActivity(new Intent(com.example.capstonemainproject.UserActivity.this, BankActivity.class));
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // 로그인 토큰 저장
        saveLoginToken();

        // 화면 설정
        toolbarUser = findViewById(R.id.toolbar_user);
        textProfileName = findViewById(R.id.user_profile_name);
        textProfileEmail = findViewById(R.id.user_profile_email);
        textProfilePhoneNumber = findViewById(R.id.user_profile_phoneNumber);
        textUserCash = findViewById(R.id.user_profile_cash);
        textUserCashPoint = findViewById(R.id.user_profile_cash_point);
        layoutUserSetting = findViewById(R.id.layout_user_setting);
        layoutUserPassword = findViewById(R.id.layout_user_password);
        layoutUserNotification = findViewById(R.id.layout_user_notification);
        layoutCarList = findViewById(R.id.layout_user_car_list);
        layoutNewCar = findViewById(R.id.layout_user_new_car);
        layoutAccountList = findViewById(R.id.layout_user_account_list);
        layoutNewAccount = findViewById(R.id.layout_user_new_account);
        layoutUserCash = findViewById(R.id.layout_user_cash);

        // 상단바 설정
        settingActionBar();

        // 화면 동작(1) : 사용자 정보 수정
        layoutUserSetting.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.UserActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(com.example.capstonemainproject.UserActivity.this, UserSettingActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
            intent.putExtra("REQUEST_POSITION", 0);

            startActivity(intent);
        });

        // 화면 동작(2) : 비밀번호 변경
        layoutUserPassword.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.UserActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(com.example.capstonemainproject.UserActivity.this, UserSettingActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
            intent.putExtra("REQUEST_POSITION", 1);

            startActivity(intent);
        });

        // 화면 동작(3) : 알림 설정
        layoutUserNotification.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.UserActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(com.example.capstonemainproject.UserActivity.this, UserSettingActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
            intent.putExtra("REQUEST_POSITION", 2);

            startActivity(intent);
        });

        // 화면 동작(4) : 등록 차량 목록
        layoutCarList.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.UserActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(com.example.capstonemainproject.UserActivity.this, com.example.capstonemainproject.CarActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

            startActivity(intent);
        });

        // 화면 동작(5) : 새 차량 등록
        layoutNewCar.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.UserActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(com.example.capstonemainproject.UserActivity.this, CarRegistrationActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

            startActivityResultForUser.launch(intent);
        });

        // 화면 동작(6) : 연결 계좌 목록
        layoutAccountList.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.UserActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(com.example.capstonemainproject.UserActivity.this, BankActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

            startActivity(intent);
        });

        // 화면 동작(7) : 새 계좌 추가
        layoutNewAccount.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.UserActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(com.example.capstonemainproject.UserActivity.this, BankRegistrationActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

            startActivityResultForUser.launch(intent);
        });

        // 화면 동작(8) : 금액 충전/환불
        layoutUserCash.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.UserActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(com.example.capstonemainproject.UserActivity.this, CashActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            startActivity(new Intent(com.example.capstonemainproject.UserActivity.this, MainActivity.class));

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

            PreferenceManager.setString(com.example.capstonemainproject.UserActivity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
        }
    }

    private void settingActionBar() {
        setSupportActionBar(toolbarUser);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_home_solid);
    }

    private void loadUserInfo() {
        String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.UserActivity.this, "LOGIN_ACCESS_TOKEN");

        userBasicService = new UserBasicService(loginAccessToken);

        try {
            CommonResponse commonResponse = userBasicService.execute(USER_BASIC_SERVICE_GET_USER_INFO).get();

            if (commonResponse.isSuccess()) {
                SingleResultResponse<User> singleResultResponse = (SingleResultResponse<User>) commonResponse;
                User user = singleResultResponse.getData();

                textProfileName.setText(user.getName());
                textProfileEmail.setText(user.getEmail());
                textProfilePhoneNumber.setText(user.getPhoneNumber());
                textUserCash.setText(String.valueOf(user.getCash()));
                textUserCashPoint.setText(String.valueOf(user.getCashPoint()));

            } else {
                String loadUserInfoFailedMsg = "사용자 정보를 불러올 수 없습니다.";

                SnackBarManager.showMessage(findViewById(R.id.scrollView_user), loadUserInfoFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("User", "Loading user info failed.");
        }
    }
}
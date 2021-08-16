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

import com.example.capstonemainproject.dto.request.car.RegisterCarDto;
import com.example.capstonemainproject.dto.resoponse.common.CommonResponse;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.CarService;

import java.util.concurrent.ExecutionException;

public class CarRegistrationActivity extends AppCompatActivity {

    private static final int CAR_REGISTRATION_ACTIVITY_RESULT_OK = 100;
    private static final int CAR_SERVICE_REGISTER_USER_CAR = -6;

    private Toolbar toolbarCarRegistration;

    private EditText eTextCarModel, eTextCarModelYear, eTextCarType, eTextCarNumber;
    private Button btnCarRegister;

    private CarService carService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_registration);

        // 로그인 토큰 저장
        saveLoginToken();

        // 화면 설정
        toolbarCarRegistration = findViewById(R.id.toolbar_car_registration);
        eTextCarModel = findViewById(R.id.editText_car_model);
        eTextCarModelYear = findViewById(R.id.editText_car_model_year);
        eTextCarType = findViewById(R.id.editText_car_type);
        eTextCarNumber = findViewById(R.id.editText_car_number);
        btnCarRegister = findViewById(R.id.btn_car_register);

        // 상단바 설정
        settingActionBar();

        // 화면 동작 : 차량 등록
        btnCarRegister.setOnClickListener(v -> {
            String carModel = eTextCarModel.getText().toString();
            String carModelYear = eTextCarModelYear.getText().toString();
            String carType = eTextCarType.getText().toString();
            String carNumber = eTextCarNumber.getText().toString();

            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.CarRegistrationActivity.this, "LOGIN_ACCESS_TOKEN");
            RegisterCarDto registerCarDto = getRegisterCarDto(carModel, carModelYear, carType, carNumber);

            carService = new CarService(loginAccessToken, registerCarDto);

            try {
                CommonResponse commonResponse = carService.execute(CAR_SERVICE_REGISTER_USER_CAR).get();

                if (commonResponse.isSuccess()) {
                    setResult(CAR_REGISTRATION_ACTIVITY_RESULT_OK);
                    finish();

                } else {
                    String registerCarFailedMsg = "등록할 차량의 입력 정보가 부족하거나 이미 등록된 차량입니다.";

                    SnackBarManager.showMessage(v, registerCarFailedMsg);
                }

            } catch (ExecutionException | InterruptedException e) {
                Log.w("Car", "Loading user car list failed.");
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
            startActivity(new Intent(com.example.capstonemainproject.CarRegistrationActivity.this, com.example.capstonemainproject.MainActivity.class));

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

            PreferenceManager.setString(com.example.capstonemainproject.CarRegistrationActivity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
        }
    }

    private void settingActionBar() {
        setSupportActionBar(toolbarCarRegistration);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_left_solid);
    }

    private RegisterCarDto getRegisterCarDto(String model, String modelYear, String type, String number) {
        RegisterCarDto registerCarDto = new RegisterCarDto();
        registerCarDto.setCarModel(model);
        registerCarDto.setCarModelYear(modelYear);
        registerCarDto.setCarType(type);
        registerCarDto.setCarNumber(number);

        return registerCarDto;
    }
}
package com.example.capstonemainproject;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.SingleResultResponse;
import com.example.capstonemainproject.dto.response.custom.search.ChargerInfoDto;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.infra.app.TextHyperLinker;
import com.example.capstonemainproject.service.ChargerService;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ChargerActivity extends AppCompatActivity {

    private static final long CHARGER_SERVICE_GET_INFO = -19;
    private static final long CHARGER_SERVICE_GET_INFO_RECORD = -20;

    private Toolbar toolbarCharger;

    private TextView textStationName, textStationAddress;
    private TextView textChargerName, textChargerMode, textChargerState, textStateUpdatedAt;
    private Button btnReservation;

    private ChargerService chargerService;

    private ChargerInfoDto chargerInfo;
    private boolean isRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charger);

        // 인텐트 정보 저장
        saveIntentValues();

        // 화면 설정
        toolbarCharger = findViewById(R.id.toolbar_charger);
        textStationName = findViewById(R.id.textView_charger_station_name);
        textStationAddress = findViewById(R.id.textView_charger_station_address);
        textChargerName = findViewById(R.id.textView_charger_name);
        textChargerMode = findViewById(R.id.textView_charger_mode);
        textChargerState = findViewById(R.id.textView_charger_state);
        textStateUpdatedAt = findViewById(R.id.textView_charger_updatedAt);
        btnReservation = findViewById(R.id.btn_charger_reservation);

        // 상단바
        settingActionBar();

        // 화면 동작(1) : 충전소 링크
        textStationName.setOnClickListener(v -> {
            if (chargerInfo != null) {
                String loginAccessToken = PreferenceManager.getString(ChargerActivity.this, "LOGIN_ACCESS_TOKEN");

                Intent intent = new Intent(ChargerActivity.this, StationActivity.class);
                intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                intent.putExtra("StationId", chargerInfo.getStation().getId());
                intent.putExtra("Record", isRecord);

                startActivity(intent);
            }
        });

        // 화면 동작(2) : 예약하기
        btnReservation.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(ChargerActivity.this, "LOGIN_ACCESS_TOKEN");
            long chargerId = PreferenceManager.getLong(ChargerActivity.this, "ChargerId");

            Intent intent = new Intent(ChargerActivity.this, Reservation1Activity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
            intent.putExtra("ChargerId", chargerId);

            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChargerInfo();
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
            startActivity(new Intent(ChargerActivity.this, MainActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

    }

    private void saveIntentValues() {
        Intent currentIntent = getIntent();

        if (currentIntent.hasExtra("LOGIN_ACCESS_TOKEN")) {
            String loginAccessToken = currentIntent.getStringExtra("LOGIN_ACCESS_TOKEN");

            PreferenceManager.setString(ChargerActivity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
        }

        if (currentIntent.hasExtra("ChargerId")) {
            long chargerId = currentIntent.getLongExtra("ChargerId", -1);

            PreferenceManager.setLong(ChargerActivity.this, "CHARGER_ID", chargerId);
        }

        isRecord = getIntent().getBooleanExtra("Record", false);
    }

    private void settingActionBar() {
        setSupportActionBar(toolbarCharger);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_left_solid);
    }

    private void loadChargerInfo() {
        String loginAccessToken = PreferenceManager.getString(ChargerActivity.this, "LOGIN_ACCESS_TOKEN");
        long chargerId = PreferenceManager.getLong(ChargerActivity.this, "CHARGER_ID");

        chargerService = new ChargerService(loginAccessToken);

        try {
            CommonResponse commonResponse;

            if (!isRecord) {
                commonResponse = chargerService.execute(CHARGER_SERVICE_GET_INFO, chargerId).get();

            } else {
                commonResponse = chargerService.execute(CHARGER_SERVICE_GET_INFO_RECORD, chargerId).get();
            }

            if (commonResponse.isSuccess()) {
                SingleResultResponse<ChargerInfoDto> singleResultResponse = (SingleResultResponse<ChargerInfoDto>) commonResponse;

                chargerInfo = singleResultResponse.getData();

                textStationName.setText(chargerInfo.getStation().getStationName());
                textStationAddress.setText(chargerInfo.getStation().getStationAddress());
                textChargerName.setText(chargerInfo.getChargerName());
                textChargerMode.setText(chargerInfo.stringValueOfMode());
                textChargerState.setText(chargerInfo.stringValueOfState());
                textStateUpdatedAt.setText(chargerInfo.getStateUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                TextHyperLinker.makeTextViewHyperLink(textStationName);

            } else {
                String loadChargerInfoFailedMsg = "충전기 정보를 불러올 수 없습니다.";

                SnackBarManager.showMessage(findViewById(R.id.layout_charger), loadChargerInfoFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("Charger", "Loading charger info failed.");
        }
    }
}
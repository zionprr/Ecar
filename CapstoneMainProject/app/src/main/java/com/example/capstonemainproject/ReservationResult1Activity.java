package com.example.capstonemainproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.capstonemainproject.domain.ReservationStatement;
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
public class ReservationResult1Activity extends AppCompatActivity {

    private static final int RESERVATION_PAYMENT_ACTIVITY_RESULT_FAIL = 103;
    private static final long CHARGER_SERVICE_GET_INFO = -19;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd - HH:mm");

    private Toolbar toolbarReservationResult;

    private TextView textReservationResultNew;
    private TextView textReservedAt, textUserNameAndCarNumber, textState, textFares;
    private TextView textStationAndCharger, textStartDateTime, textEndDateTime;
    private Button btnReservationPayment;

    private ChargerService chargerService;

    private boolean isNewReservation;
    private ReservationStatement reservationStatement;

    // 예약 결제 결과
    private final ActivityResultLauncher<Intent> startActivityResultForReservationPayment =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            String loginAccessToken = PreferenceManager.getString(ReservationResult1Activity.this, "LOGIN_ACCESS_TOKEN");
                            ReservationStatement statement = (ReservationStatement) result.getData().getSerializableExtra("RESERVATION_STATEMENT");

                            Intent intent = new Intent(ReservationResult1Activity.this, ReservationResult2Activity.class);
                            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                            intent.putExtra("RESERVATION_STATEMENT", statement);
                            intent.putExtra("IsPaidNow", true);

                            finish();
                            startActivity(intent);

                        } else if (result.getResultCode() == RESERVATION_PAYMENT_ACTIVITY_RESULT_FAIL) {
                            String reservationPaymentFailedMsg =
                                    "결제를 완료하지 못했습니다.\n결제 비밀번호가 일치하지 않거나 연결된 계좌가 없습니다.\n또는 결제 시간을 초과하여 자동 취소된 예약입니다.";

                            SnackBarManager.showMessage(findViewById(R.id.layout_reservation_result1), reservationPaymentFailedMsg);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_result1);

        // 인텐트 정보 저장
        saveIntentValues();

        // 화면 설정
        toolbarReservationResult = findViewById(R.id.toolbar_reservation_result1);
        textReservationResultNew = findViewById(R.id.textView_reservation_result1_new);
        textReservedAt = findViewById(R.id.textView_reservation_result1_reservedAt);
        textUserNameAndCarNumber = findViewById(R.id.textView_reservation_result1_userName_and_carNumber);
        textState = findViewById(R.id.textView_reservation_result1_state);
        textFares = findViewById(R.id.textView_reservation_result1_fares);
        textStationAndCharger = findViewById(R.id.textView_reservation_result1_station_and_charger);
        textStartDateTime = findViewById(R.id.textView_reservation_result1_start_dateTime);
        textEndDateTime = findViewById(R.id.textView_reservation_result1_end_dateTime);
        btnReservationPayment = findViewById(R.id.btn_reservation_result1_payment);

        // 상단바
        settingActionBar();

        // 화면 동작(1) : 충전기 링크
        textStationAndCharger.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(ReservationResult1Activity.this, "LOGIN_ACCESS_TOKEN");
            long chargerId = PreferenceManager.getLong(ReservationResult1Activity.this, "CHARGER_ID");

            Intent intent = new Intent(ReservationResult1Activity.this, ChargerActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
            intent.putExtra("ChargerId", chargerId);

            startActivity(intent);
        });

        // 화면 동작(2) : 예약 결제
        btnReservationPayment.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(ReservationResult1Activity.this, "LOGIN_ACCESS_TOKEN");
            long reservationId = PreferenceManager.getLong(ReservationResult1Activity.this, "RESERVATION_ID");

            Intent intent = new Intent(ReservationResult1Activity.this, ReservationPaymentActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
            intent.putExtra("ReservationId", reservationId);

            startActivityResultForReservationPayment.launch(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isNewReservation) {
            textReservationResultNew.setVisibility(View.VISIBLE);

        } else {
            textReservationResultNew.setVisibility(View.GONE);
        }

        showReservationResult();
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

            if (isNewReservation) {
                String loginAccessToken = PreferenceManager.getString(ReservationResult1Activity.this, "LOGIN_ACCESS_TOKEN");

                Intent intent = new Intent(new Intent(ReservationResult1Activity.this, ReservationStatementActivity.class));
                intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                intent.putExtra("REQUEST_POSITION", 0);

                startActivity(intent);
            }

            return true;

        } else if (item.getItemId() == R.id.action_home) {
            finish();
            startActivity(new Intent(ReservationResult1Activity.this, MainActivity.class));

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

            PreferenceManager.setString(ReservationResult1Activity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
        }

        if (currentIntent.hasExtra("IsNewReservation")) {
            isNewReservation = currentIntent.getBooleanExtra("IsNewReservation", false);
        }

        if (currentIntent.hasExtra("RESERVATION_STATEMENT")) {
            reservationStatement = (ReservationStatement) currentIntent.getSerializableExtra("RESERVATION_STATEMENT");

            PreferenceManager.setLong(ReservationResult1Activity.this, "RESERVATION_ID", reservationStatement.getReservationId());
            PreferenceManager.setLong(ReservationResult1Activity.this, "CHARGER_ID", reservationStatement.getChargerId());
        }
    }

    private void settingActionBar() {
        setSupportActionBar(toolbarReservationResult);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_left_solid);
    }

    private void showReservationResult() {
        if (reservationStatement != null) {
            textReservedAt.setText(reservationStatement.getReservedAt().format(dateTimeFormatter));
            textUserNameAndCarNumber.setText(String.format("%s / %s", reservationStatement.getUserName(), reservationStatement.getCarNumber()));
            textState.setText(reservationStatement.stateValue());
            textFares.setText(String.format("%d 원", reservationStatement.getReserveFares()));
            textStartDateTime.setText(reservationStatement.getChargeStartDateTime().format(dateTimeFormatter));
            textEndDateTime.setText(reservationStatement.getChargeEndDateTime().format(dateTimeFormatter));
        }
    }

    private void loadChargerInfo() {
        String loginAccessToken = PreferenceManager.getString(ReservationResult1Activity.this, "LOGIN_ACCESS_TOKEN");
        long chargerId = PreferenceManager.getLong(ReservationResult1Activity.this, "CHARGER_ID");

        chargerService = new ChargerService(loginAccessToken);

        try {
            CommonResponse commonResponse = chargerService.execute(CHARGER_SERVICE_GET_INFO, chargerId).get();;

            if (commonResponse.isSuccess()) {
                SingleResultResponse<ChargerInfoDto> singleResultResponse = (SingleResultResponse<ChargerInfoDto>) commonResponse;
                ChargerInfoDto chargerInfo = singleResultResponse.getData();

                textStationAndCharger.setText(String.format("%s / %s",chargerInfo.getStation().getStationName(), chargerInfo.getChargerName()));

                TextHyperLinker.makeTextViewHyperLink(textStationAndCharger);

            } else {
                String loadChargerInfoFailedMsg = "충전기 정보를 불러올 수 없습니다.";

                SnackBarManager.showMessage(findViewById(R.id.layout_charger), loadChargerInfoFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("Charger", "Loading charger info failed.");
        }
    }
}
package com.example.capstonemainproject;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.capstonemainproject.domain.ReservationStatement;
import com.example.capstonemainproject.domain.User;
import com.example.capstonemainproject.dto.request.reservation.PayReservationDto;
import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.SingleResultResponse;
import com.example.capstonemainproject.dto.response.custom.reservation.SimpleReservationInfoDto;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.ReservationService;
import com.example.capstonemainproject.service.UserBasicService;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ReservationPaymentActivity extends AppCompatActivity {

    private static final int RESERVATION_PAYMENT_ACTIVITY_RESULT_FAIL = 103;
    private static final int USER_BASIC_SERVICE_GET_USER_INFO = -1;
    private static final int RESERVATION_SERVICE_PAY = -29;
    private static final int RESERVATION_SERVICE_GET_SIMPLE_INFO = -31;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd  HH:mm");

    private Toolbar toolbarReservationPayment;

    private TextView textStationAndCharger, textUserNameAndCarNumber, textStartDateTime, textEndDateTime;
    private TextView textUserCash, textUserCashPoint;
    private TextView textFares, textFinalFares;
    private EditText eTextUsedCashPoint;
    private ImageView iViewLayout1Hide, iViewLayout1Show, iViewLayout2Hide, iViewLayout2Show;
    private LinearLayout layoutContent1, layoutContent2;
    private Button btnReservationPaymentComplete;

    private UserBasicService userBasicService;
    private ReservationService reservationService;

    private int reservationFares, userCashPoint, usedCashPoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_payment);

        // 인텐트 정보 저장
        saveIntentValues();

        // 화면 설정
        toolbarReservationPayment = findViewById(R.id.toolbar_reservation_payment);
        textStationAndCharger = findViewById(R.id.textView_reservation_payment_station_and_charger);
        textUserNameAndCarNumber = findViewById(R.id.textView_reservation_payment_userName_and_carNumber);
        textStartDateTime = findViewById(R.id.textView_reservation_payment_start_dateTime);
        textEndDateTime = findViewById(R.id.textView_reservation_payment_end_dateTime);
        textUserCash = findViewById(R.id.textView_reservation_payment_cash);
        textUserCashPoint = findViewById(R.id.textView_reservation_payment_cash_point);
        textFares = findViewById(R.id.textView_reservation_payment_fares);
        textFinalFares = findViewById(R.id.textView_reservation_payment_final_fares);
        eTextUsedCashPoint = findViewById(R.id.editText_reservation_payment_used_cash_point);
        iViewLayout1Hide = findViewById(R.id.imageView_reservation_payment_hide1);
        iViewLayout1Show = findViewById(R.id.imageView_reservation_payment_show1);
        iViewLayout2Hide = findViewById(R.id.imageView_reservation_payment_hide2);
        iViewLayout2Show = findViewById(R.id.imageView_reservation_payment_show2);
        layoutContent1 = findViewById(R.id.layout_reservation_payment_content1);
        layoutContent2 = findViewById(R.id.layout_reservation_payment_content2);
        btnReservationPaymentComplete = findViewById(R.id.btn_reservation_payment_complete);

        // 액션바
        settingActionBar();

        // 화면 동작(1) : 예약 내역 숨기기
        iViewLayout1Hide.setOnClickListener(v -> {
            layoutContent1.setVisibility(View.GONE);
            iViewLayout1Hide.setVisibility(View.GONE);
            iViewLayout1Show.setVisibility(View.VISIBLE);
        });

        // 화면 동작(2) : 예약 내역 표시
        iViewLayout1Show.setOnClickListener(v -> {
            iViewLayout1Show.setVisibility(View.GONE);
            iViewLayout1Hide.setVisibility(View.VISIBLE);
            layoutContent1.setVisibility(View.VISIBLE);
        });

        // 화면 동작(3) : 결제 내역 숨기기
        iViewLayout2Hide.setOnClickListener(v -> {
            layoutContent2.setVisibility(View.GONE);
            iViewLayout2Hide.setVisibility(View.GONE);
            iViewLayout2Show.setVisibility(View.VISIBLE);
        });

        // 화면 동작(4) : 결제 내역 표시
        iViewLayout2Show.setOnClickListener(v -> {
            iViewLayout2Show.setVisibility(View.GONE);
            iViewLayout2Hide.setVisibility(View.VISIBLE);
            layoutContent2.setVisibility(View.VISIBLE);
        });

        // 화면 동작(5) : 사용 포인트트 입력
        eTextUsedCashPoint.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                usedCashPoint = 0;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().isEmpty()) {
                    int inputUsedCashPoint = Integer.parseInt(s.toString());

                    if (inputUsedCashPoint > userCashPoint) {
                        String userCashPointNotEnoughMsg = "보유 포인트보다 많은 포인트를 입력할 수 없습니다.";

                        SnackBarManager.showMessage(findViewById(R.id.scrollView_reservation_payment), userCashPointNotEnoughMsg);
                        s.clear();

                    } else {
                        textFinalFares.setText(String.format("%d 원", reservationFares - inputUsedCashPoint));
                        usedCashPoint = inputUsedCashPoint;
                    }
                }
            }
        });

        // 화면 동작(6) : 결제 완료
        btnReservationPaymentComplete.setOnClickListener(v -> showDialogForPayment());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserInfo();
        loadReservationInfo();
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
            startActivity(new Intent(ReservationPaymentActivity.this, MainActivity.class));

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

            PreferenceManager.setString(ReservationPaymentActivity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
        }

        if (currentIntent.hasExtra("ReservationId")) {
            long reservationId = currentIntent.getLongExtra("ReservationId", -1);

            PreferenceManager.setLong(ReservationPaymentActivity.this, "RESERVATION_ID", reservationId);
        }

    }

    private void settingActionBar() {
        setSupportActionBar(toolbarReservationPayment);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_left_solid);
    }

    private void loadUserInfo() {
        String loginAccessToken = PreferenceManager.getString(ReservationPaymentActivity.this, "LOGIN_ACCESS_TOKEN");

        userBasicService = new UserBasicService(loginAccessToken);

        try {
            CommonResponse commonResponse = userBasicService.execute(USER_BASIC_SERVICE_GET_USER_INFO).get();

            if (commonResponse.isSuccess()) {
                SingleResultResponse<User> singleResultResponse = (SingleResultResponse<User>) commonResponse;
                User user = singleResultResponse.getData();

                textUserCash.setText(String.format("%d 원", user.getCash()));
                textUserCashPoint.setText(String.format("%d 포인트", user.getCashPoint()));

                userCashPoint = user.getCashPoint();

            } else {
                String loadUserInfoFailedMsg = "사용자 정보를 불러올 수 없습니다.";

                SnackBarManager.showMessage(findViewById(R.id.scrollView_reservation_payment), loadUserInfoFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("User", "Loading user info failed.");
        }
    }

    private void loadReservationInfo() {
        String loginAccessToken = PreferenceManager.getString(ReservationPaymentActivity.this, "LOGIN_ACCESS_TOKEN");
        long reservationId = PreferenceManager.getLong(ReservationPaymentActivity.this, "RESERVATION_ID");

        reservationService = new ReservationService(loginAccessToken);
        reservationService.setReservationId(reservationId);

        try {
            CommonResponse commonResponse = reservationService.execute(RESERVATION_SERVICE_GET_SIMPLE_INFO).get();

            if (commonResponse.isSuccess()) {
                SingleResultResponse<SimpleReservationInfoDto> singleResultResponse = (SingleResultResponse<SimpleReservationInfoDto>) commonResponse;
                SimpleReservationInfoDto reservationInfo = singleResultResponse.getData();

                textStationAndCharger.setText(String.format("%s / %s", reservationInfo.getStationName(), reservationInfo.getChargerName()));
                textUserNameAndCarNumber.setText(String.format("%s / %s", reservationInfo.getUserName(), reservationInfo.getCarNumber()));
                textStartDateTime.setText(reservationInfo.getChargeStartDateTime().format(dateTimeFormatter));
                textFares.setText(String.format("%d 원", reservationInfo.getFares()));
                textFinalFares.setText(String.format("%d 원", reservationInfo.getFares()));

                textEndDateTime.setText(
                        String.format("~  %s  (%d 분)",
                                reservationInfo.getChargeEndDateTime().format(dateTimeFormatter),
                                getMinuteOfDuration(reservationInfo.getChargeStartDateTime(), reservationInfo.getChargeEndDateTime())
                        )
                );

                reservationFares = reservationInfo.getFares();

            } else {
                String loadReservationInfoFailedMsg = "예약 정보를 불러올 수 없습니다.";

                SnackBarManager.showMessage(findViewById(R.id.scrollView_reservation_payment), loadReservationInfoFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("User", "Loading user info failed.");
        }
    }

    private int getMinuteOfDuration(LocalDateTime start, LocalDateTime end) {
        Duration duration = Duration.between(start, end);

        return (int) (duration.getSeconds() / 60);
    }

    private void showDialogForPayment() {
        Dialog dialog = new Dialog(ReservationPaymentActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_reservation_payment);

        dialog.show();

        // 커스텀 다이얼로그 설정
        EditText paymentPassword = dialog.findViewById(R.id.editText_reservation_payment_password);
        Button btnOk = dialog.findViewById(R.id.btn_reservation_payment_ok);
        Button btnCancel = dialog.findViewById(R.id.btn_reservation_payment_cancel);

        btnOk.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(ReservationPaymentActivity.this, "LOGIN_ACCESS_TOKEN");
            long reservationId = PreferenceManager.getLong(ReservationPaymentActivity.this, "RESERVATION_ID");

            reservationService = new ReservationService(loginAccessToken, getPayReservationDto(reservationId, paymentPassword.getText().toString()));

            try {
                CommonResponse commonResponse = reservationService.execute(RESERVATION_SERVICE_PAY).get();

                if (commonResponse.isSuccess()) {
                    SingleResultResponse<ReservationStatement> singleResultResponse = (SingleResultResponse<ReservationStatement>) commonResponse;

                    Intent intent = new Intent();
                    intent.putExtra("RESERVATION_STATEMENT", singleResultResponse.getData());

                    setResult(RESULT_OK, intent);

                } else {
                    setResult(RESERVATION_PAYMENT_ACTIVITY_RESULT_FAIL);
                }

                finish();

            } catch (ExecutionException | InterruptedException e) {
                Log.w("User", "Loading user info failed.");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private PayReservationDto getPayReservationDto(long reservationId, String paymentPassword) {
        PayReservationDto payReservationDto = new PayReservationDto();
        payReservationDto.setReservationId(reservationId);
        payReservationDto.setPaymentPassword(paymentPassword);
        payReservationDto.setUsedCashPoint(usedCashPoint);

        return payReservationDto;
    }
}
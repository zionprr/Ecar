package com.example.capstonemainproject;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.capstonemainproject.domain.ReservationStatement;
import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.SingleResultResponse;
import com.example.capstonemainproject.dto.response.custom.search.ChargerInfoDto;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.app.QRcodeManager;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.infra.app.TextHyperLinker;
import com.example.capstonemainproject.service.ChargerService;
import com.example.capstonemainproject.service.ReservationService;

import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ReservationResult2Activity extends AppCompatActivity {

    private static final long CHARGER_SERVICE_GET_INFO = -19;
    private static final int RESERVATION_SERVICE_CANCEL = -30;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd - HH:mm");

    private Toolbar toolbarReservationResult2;

    private TextView textReservationResultPaidNow;
    private TextView textReservationTitle, textReservedAt, textUserNameAndCarNumber, textState, textFares, textUsedCashPoint;
    private TextView textStationAndCharger, textStartDateTime, textEndDateTime;
    private ImageView iViewQRcode;
    private Button btnReservationCancel;

    private ChargerService chargerService;
    private ReservationService reservationService;

    private boolean isPaidNow;
    private ReservationStatement reservationStatement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_result2);

        // ????????? ?????? ??????
        saveIntentValues();

        // ?????? ??????
        toolbarReservationResult2 = findViewById(R.id.toolbar_reservation_result2);
        textReservationResultPaidNow = findViewById(R.id.textView_reservation_result2_paid_now);
        textReservationTitle = findViewById(R.id.textView_reservation_result2_title);
        textReservedAt = findViewById(R.id.textView_reservation_result2_reservedAt);
        textUserNameAndCarNumber = findViewById(R.id.textView_reservation_result2_userName_and_carNumber);
        textState = findViewById(R.id.textView_reservation_result2_state);
        textFares = findViewById(R.id.textView_reservation_result2_fares);
        textUsedCashPoint = findViewById(R.id.textView_reservation_result2_used_cash_point);
        textStationAndCharger = findViewById(R.id.textView_reservation_result2_station_and_charger);
        textStartDateTime = findViewById(R.id.textView_reservation_result2_start_dateTime);
        textEndDateTime = findViewById(R.id.textView_reservation_result2_end_dateTime);
        iViewQRcode = findViewById(R.id.imageVIew_reservation_result2_qrcode);
        btnReservationCancel = findViewById(R.id.btn_reservation_result2_cancel);

        // ?????????
        settingActionBar();

        // ?????? ??????(1) : ????????? ??????
        textStationAndCharger.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(ReservationResult2Activity.this, "LOGIN_ACCESS_TOKEN");
            long chargerId = PreferenceManager.getLong(ReservationResult2Activity.this, "CHARGER_ID");

            Intent intent = new Intent(ReservationResult2Activity.this, ChargerActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
            intent.putExtra("ChargerId", chargerId);

            startActivity(intent);
        });

        // ?????? ??????(2) : ?????? ??????
        btnReservationCancel.setOnClickListener(v -> showDialogForCancel());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(isPaidNow) {
            textReservationResultPaidNow.setVisibility(View.VISIBLE);

        } else {
            textReservationResultPaidNow.setVisibility(View.GONE);
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

            if (isPaidNow) {
                String loginAccessToken = PreferenceManager.getString(ReservationResult2Activity.this, "LOGIN_ACCESS_TOKEN");

                Intent intent = new Intent(new Intent(ReservationResult2Activity.this, ReservationStatementActivity.class));
                intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                intent.putExtra("REQUEST_POSITION", 0);

                startActivity(intent);
            }

            return true;

        } else if (item.getItemId() == R.id.action_home) {
            finish();
            startActivity(new Intent(ReservationResult2Activity.this, MainActivity.class));

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

            PreferenceManager.setString(ReservationResult2Activity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
        }

        if (currentIntent.hasExtra("IsPaidNow")) {
            isPaidNow = currentIntent.getBooleanExtra("IsPaidNow", false);
        }

        if (currentIntent.hasExtra("RESERVATION_STATEMENT")) {
            reservationStatement = (ReservationStatement) currentIntent.getSerializableExtra("RESERVATION_STATEMENT");

            PreferenceManager.setLong(ReservationResult2Activity.this, "RESERVATION_ID", reservationStatement.getReservationId());
            PreferenceManager.setLong(ReservationResult2Activity.this, "CHARGER_ID", reservationStatement.getChargerId());
        }
    }

    private void settingActionBar() {
        setSupportActionBar(toolbarReservationResult2);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_left_solid);
    }

    private void showReservationResult() {
        if (reservationStatement != null) {
            textReservationTitle.setText(reservationStatement.getReserveTitle());
            textReservedAt.setText(reservationStatement.getReservedAt().format(dateTimeFormatter));
            textUserNameAndCarNumber.setText(String.format("%s / %s", reservationStatement.getUserName(), reservationStatement.getCarNumber()));
            textState.setText(reservationStatement.stateValue());
            textFares.setText(String.format("%d ???", reservationStatement.getPaidCash()));
            textUsedCashPoint.setText(String.format("%d ?????????", reservationStatement.getUsedCashPoint()));
            textStartDateTime.setText(reservationStatement.getChargeStartDateTime().format(dateTimeFormatter));
            textEndDateTime.setText(reservationStatement.getChargeEndDateTime().format(dateTimeFormatter));

            QRcodeManager.createQRCodeAndLoadOnView(iViewQRcode, reservationStatement.getReserveTitle());
        }
    }

    private void loadChargerInfo() {
        String loginAccessToken = PreferenceManager.getString(ReservationResult2Activity.this, "LOGIN_ACCESS_TOKEN");
        long chargerId = PreferenceManager.getLong(ReservationResult2Activity.this, "CHARGER_ID");

        chargerService = new ChargerService(loginAccessToken);

        try {
            CommonResponse commonResponse = chargerService.execute(CHARGER_SERVICE_GET_INFO, chargerId).get();;

            if (commonResponse.isSuccess()) {
                SingleResultResponse<ChargerInfoDto> singleResultResponse = (SingleResultResponse<ChargerInfoDto>) commonResponse;
                ChargerInfoDto chargerInfo = singleResultResponse.getData();

                textStationAndCharger.setText(String.format("%s / %s",chargerInfo.getStation().getStationName(), chargerInfo.getChargerName()));

                TextHyperLinker.makeTextViewHyperLink(textStationAndCharger);

            } else {
                String loadChargerInfoFailedMsg = "????????? ????????? ????????? ??? ????????????.";

                SnackBarManager.showMessage(findViewById(R.id.scrollView_reservation_result2), loadChargerInfoFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("Charger", "Loading charger info failed.");
        }
    }

    private void showDialogForCancel() {
        Dialog dialog = new Dialog(ReservationResult2Activity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_reservation_payment);

        dialog.show();

        // ????????? ??????????????? ??????
        TextView cancellationFee = dialog.findViewById(R.id.textView_reservation_cancel_cancellationFee);
        Button btnOk = dialog.findViewById(R.id.btn_reservation_cancel_ok);
        Button btnNo = dialog.findViewById(R.id.btn_reservation_cancel_no);

        cancellationFee.setText(String.format("%d ???", reservationStatement.getCancellationFee()));

        btnOk.setOnClickListener(v-> {
            String loginAccessToken = PreferenceManager.getString(ReservationResult2Activity.this, "LOGIN_ACCESS_TOKEN");
            String reservationTitle = reservationStatement.getReserveTitle();

            reservationService = new ReservationService(loginAccessToken, reservationTitle);

            try {
                CommonResponse commonResponse = reservationService.execute(RESERVATION_SERVICE_CANCEL).get();

                if (commonResponse.isSuccess()) {
                    Intent intent = new Intent(new Intent(ReservationResult2Activity.this, ReservationStatementActivity.class));
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                    intent.putExtra("REQUEST_POSITION", 0);

                    finish();
                    startActivity(intent);

                } else {
                    String reservationCancelFailedMsg = "?????? ?????? ?????? ???????????? ????????? ????????? ??? ????????????.";

                    SnackBarManager.showMessage(findViewById(R.id.scrollView_reservation_result2), reservationCancelFailedMsg);

                    dialog.dismiss();
                }

            } catch (ExecutionException | InterruptedException e) {
                Log.w("Reservation", "Reservation cancel failed.");
            }
        });

        btnNo.setOnClickListener(v -> dialog.dismiss());
    }
}
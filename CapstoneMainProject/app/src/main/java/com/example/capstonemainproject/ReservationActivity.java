package com.example.capstonemainproject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.SingleResultResponse;
import com.example.capstonemainproject.dto.response.custom.reservation.ChargerTimeTableDto;
import com.example.capstonemainproject.dto.response.custom.search.ChargerInfoDto;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.ChargerService;
import com.example.capstonemainproject.service.ReservationService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ReservationActivity extends AppCompatActivity {

    private static final int RESERVATION2_ACTIVITY_RESULT_FAIL = 102;
    private static final long CHARGER_SERVICE_GET_INFO = -19;
    private static final int RESERVATION_SERVICE_GET_TIME_TABLE = -26;

    private Toolbar toolbarReservation;

    private TextView textStationName, textStationAddress;
    private TextView textChargerName, textChargerMode, textChargerState;
    private TextView textReservationTimeNotFound;
    private Button btnReservationDate;
    private CheckBox checkBoxReservationTimeAvailable;
    private ListView listViewReservationTime;

    private ChargerService chargerService;
    private ReservationService reservationService;

    private LocalDate targetDate;
    private List<ReservationTime> chargerTimeTable;

    // 예약 결과
    private final ActivityResultLauncher<Intent> startActivityResultForReservation2 =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.ReservationActivity.this, "LOGIN_ACCESS_TOKEN");

                            Intent intent = new Intent(com.example.capstonemainproject.ReservationActivity.this, ReservationStatementActivity.class);
                            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

                            finish();
                            startActivity(intent);

                        } else if (result.getResultCode() == RESERVATION2_ACTIVITY_RESULT_FAIL) {
                            String reservationFailedMsg = "예약을 완료하지 못했습니다.\n이미 예약된 시간이거나 예약이 불가능한 상태입니다.";

                            SnackBarManager.showMessage(findViewById(R.id.layout_reservation), reservationFailedMsg);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        // 인텐트 정보 저장
        saveIntentValues();

        // 화면 설정
        toolbarReservation = findViewById(R.id.toolbar_reservation);
        textStationName = findViewById(R.id.textView_reservation_station_name);
        textStationAddress = findViewById(R.id.textView_reservation_station_address);
        textChargerName = findViewById(R.id.textView_reservation_charger_name);
        textChargerMode = findViewById(R.id.textView_reservation_charger_mode);
        textChargerState = findViewById(R.id.textView_reservation_charger_state);
        textReservationTimeNotFound = findViewById(R.id.textView_reservation_time_notFound);
        btnReservationDate = findViewById(R.id.btn_reservation_date);
        checkBoxReservationTimeAvailable = findViewById(R.id.checkbox_reservation_available);
        listViewReservationTime = findViewById(R.id.listView_reservation_time);

        // 상단바 및 리스트뷰 스크롤
        settingActionBar();
        settingScroll();

        // 화면 동작(1) : 날짜 선택
        btnReservationDate.setOnClickListener(v -> showDatePicker());

        // 화면 동작(2) : 예약 가능한 시간만 보기 선택
        checkBoxReservationTimeAvailable.setOnClickListener(v -> {
            if (checkBoxReservationTimeAvailable.isChecked()) {     // 적용
                List<ReservationTime> availableTimeTable =
                        chargerTimeTable.stream().filter(ReservationTime::isReserved).collect(Collectors.toList());

                if (availableTimeTable.size() == 0) {
                    listViewReservationTime.setVisibility(View.GONE);
                    textReservationTimeNotFound.setVisibility(View.VISIBLE);

                } else {
                    textReservationTimeNotFound.setVisibility(View.GONE);
                    listViewReservationTime.setVisibility(View.VISIBLE);

                    listViewReservationTime.setAdapter(new CustomReservationTimeList(this, availableTimeTable));
                }

            } else {                                                // 해제
                if (chargerTimeTable.size() == 0) {
                    listViewReservationTime.setVisibility(View.GONE);
                    textReservationTimeNotFound.setVisibility(View.VISIBLE);

                } else {
                    textReservationTimeNotFound.setVisibility(View.GONE);
                    listViewReservationTime.setVisibility(View.VISIBLE);

                    listViewReservationTime.setAdapter(new CustomReservationTimeList(this, chargerTimeTable));
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChargerInfo();
        loadReservationTimeTable(0);
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
            startActivity(new Intent(com.example.capstonemainproject.ReservationActivity.this, MainActivity.class));

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

            PreferenceManager.setString(com.example.capstonemainproject.ReservationActivity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
        }

        if (currentIntent.hasExtra("ChargerId")) {
            long chargerId = currentIntent.getLongExtra("ChargerId", -1);

            PreferenceManager.setLong(com.example.capstonemainproject.ReservationActivity.this, "CHARGER_ID", chargerId);
        }
    }

    private void settingActionBar() {
        setSupportActionBar(toolbarReservation);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_left_solid);
    }

    private void settingScroll() {
        NestedScrollView scrollView = findViewById(R.id.scrollView_reservation);

        listViewReservationTime.setOnTouchListener((v, event) -> {
            scrollView.requestDisallowInterceptTouchEvent(true);

            return false;
        });
    }

    private void loadChargerInfo() {
        String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.ReservationActivity.this, "LOGIN_ACCESS_TOKEN");
        long chargerId = PreferenceManager.getLong(com.example.capstonemainproject.ReservationActivity.this, "CHARGER_ID");

        chargerService = new ChargerService(loginAccessToken);

        try {
            CommonResponse commonResponse = chargerService.execute(CHARGER_SERVICE_GET_INFO, chargerId).get();

            if (commonResponse.isSuccess()) {
                SingleResultResponse<ChargerInfoDto> singleResultResponse = (SingleResultResponse<ChargerInfoDto>) commonResponse;
                ChargerInfoDto chargerInfo = singleResultResponse.getData();

                textStationName.setText(chargerInfo.getStation().getStationName());
                textStationAddress.setText(chargerInfo.getStation().getStationAddress());
                textChargerName.setText(chargerInfo.getChargerName());
                textChargerMode.setText(chargerInfo.stringValueOfMode());
                textChargerState.setText(chargerInfo.stringValueOfState());

            } else {
                String loadChargerInfoFailedMsg = "충전기 정보를 불러올 수 없습니다.";

                SnackBarManager.showMessage(findViewById(R.id.layout_reservation), loadChargerInfoFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("Charger", "Loading charger info failed.");
        }
    }

    private void loadReservationTimeTable(int day) {
        String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.ReservationActivity.this, "LOGIN_ACCESS_TOKEN");
        long chargerId = PreferenceManager.getLong(com.example.capstonemainproject.ReservationActivity.this, "CHARGER_ID");

        reservationService = new ReservationService(loginAccessToken, chargerId);
        chargerTimeTable = new ArrayList<>();

        try {
            CommonResponse commonResponse = reservationService.execute(RESERVATION_SERVICE_GET_TIME_TABLE, day).get();

            if (commonResponse.isSuccess()) {
                SingleResultResponse<ChargerTimeTableDto> singleResultResponse = (SingleResultResponse<ChargerTimeTableDto>) commonResponse;
                ChargerTimeTableDto timeTable = singleResultResponse.getData();

                timeTable
                        .getTimeTable()
                        .forEach((time, reserved) -> chargerTimeTable.add(new ReservationTime(time, reserved)));

                chargerTimeTable.sort(Comparator.comparing(ReservationTime::getTime));

                if (checkBoxReservationTimeAvailable.isChecked()) {
                    List<ReservationTime> availableTimeTable =
                            chargerTimeTable.stream().filter(ReservationTime::isReserved).collect(Collectors.toList());

                    if (availableTimeTable.size() == 0) {
                        listViewReservationTime.setVisibility(View.GONE);
                        textReservationTimeNotFound.setVisibility(View.VISIBLE);

                    } else {
                        textReservationTimeNotFound.setVisibility(View.GONE);
                        listViewReservationTime.setVisibility(View.VISIBLE);

                        listViewReservationTime.setAdapter(new CustomReservationTimeList(this, availableTimeTable));
                    }

                } else {
                    if (chargerTimeTable.size() == 0) {
                        listViewReservationTime.setVisibility(View.GONE);
                        textReservationTimeNotFound.setVisibility(View.VISIBLE);

                    } else {
                        textReservationTimeNotFound.setVisibility(View.GONE);
                        listViewReservationTime.setVisibility(View.VISIBLE);

                        listViewReservationTime.setAdapter(new CustomReservationTimeList(this, chargerTimeTable));
                    }
                }

                targetDate = timeTable.getTargetDate();

            } else {
                String loadChargerTimeTableFailedMsg = "예약 시간 정보를 불러올 수 없습니다.";

                SnackBarManager.showMessage(findViewById(R.id.layout_reservation), loadChargerTimeTableFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("Reservation", "Loading reservation time table failed.");
        }
    }

    private void showDatePicker() {
        Calendar today = Calendar.getInstance();

        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 7);

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        this,
                        (view, year, month, dayOfMonth) -> {
                            // 시간 테이블 출력
                            Calendar selectedDate = Calendar.getInstance();
                            selectedDate.set(year, month, dayOfMonth);

                            loadReservationTimeTable(differenceDays(today, selectedDate));
                        },
                        today.get(Calendar.YEAR),
                        today.get(Calendar.MONTH),
                        today.get(Calendar.DAY_OF_MONTH)
                );

        datePickerDialog.getDatePicker().setMinDate(today.getTimeInMillis());
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        datePickerDialog.show();
    }

    private int differenceDays(Calendar baseCal, Calendar targetCal) {
        long diffSec = (targetCal.getTimeInMillis() - baseCal.getTimeInMillis()) / 1000;

        return (int) (diffSec / (24 * 60 * 60));
    }

    @Getter
    @EqualsAndHashCode(of = "time")
    private class ReservationTime {

        private final String time;
        private final boolean isReserved;

        public ReservationTime(String time, boolean isReserved) {
            this.time = time;
            this.isReserved = isReserved;
        }
    }

    private class CustomReservationTimeList extends ArrayAdapter<ReservationTime> {

        private final Activity context;
        private final List<ReservationTime> timeTable;

        public CustomReservationTimeList(Activity context, List<ReservationTime> timeTable) {
            super(context, R.layout.listview_reservation_time, timeTable);
            this.context = context;
            this.timeTable = timeTable;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listview_reservation_time, null, true);

            TextView startTime = rowView.findViewById(R.id.listView_reservation_time_start);
            Button btnReservedTrue = rowView.findViewById(R.id.btn_reservation_time_true);
            Button btnReservedFalse = rowView.findViewById(R.id.btn_reservation_time_false);

            ReservationTime reservationTime = timeTable.get(position);

            startTime.setText(reservationTime.getTime());

            if (!reservationTime.isReserved()) {
                btnReservedTrue.setVisibility(View.GONE);
                btnReservedFalse.setVisibility(View.VISIBLE);

            } else {
                btnReservedFalse.setVisibility(View.GONE);
                btnReservedTrue.setVisibility(View.VISIBLE);

                btnReservedTrue.setOnClickListener(v -> {
                    String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.ReservationActivity.this, "LOGIN_ACCESS_TOKEN");
                    long chargerId = PreferenceManager.getLong(com.example.capstonemainproject.ReservationActivity.this, "CHARGER_ID");

                    String startDateTime =
                            targetDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) + " " + reservationTime.getTime();

                    Intent intent = new Intent(com.example.capstonemainproject.ReservationActivity.this, Reservation2Activity.class);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                    intent.putExtra("ChargerId", chargerId);
                    intent.putExtra("StartDateTime", startDateTime);

                    startActivityResultForReservation2.launch(intent);
                });
            }

            return rowView;
        }
    }
}
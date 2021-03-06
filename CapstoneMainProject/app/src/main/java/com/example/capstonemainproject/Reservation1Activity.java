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

import com.example.capstonemainproject.domain.ReservationStatement;
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
public class Reservation1Activity extends AppCompatActivity {

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

    private ChargerInfoDto chargerInfo;
    private LocalDate targetDate;
    private List<ReservationTime> chargerTimeTable;

    // ?????? ??????
    private final ActivityResultLauncher<Intent> startActivityResultForReservation2 =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            String loginAccessToken = PreferenceManager.getString(Reservation1Activity.this, "LOGIN_ACCESS_TOKEN");
                            ReservationStatement statement = (ReservationStatement) result.getData().getSerializableExtra("RESERVATION_STATEMENT");

                            Intent intent = new Intent(Reservation1Activity.this, ReservationResult1Activity.class);
                            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                            intent.putExtra("RESERVATION_STATEMENT", statement);
                            intent.putExtra("IsNewReservation", true);

                            finish();
                            startActivity(intent);

                        } else if (result.getResultCode() == RESERVATION2_ACTIVITY_RESULT_FAIL) {
                            String reservationFailedMsg = "????????? ???????????? ???????????????.\n?????? ????????? ??????????????? ????????? ???????????? ???????????????.";

                            SnackBarManager.showMessage(findViewById(R.id.layout_reservation), reservationFailedMsg);
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation1);

        // ????????? ?????? ??????
        saveIntentValues();

        // ?????? ??????
        toolbarReservation = findViewById(R.id.toolbar_reservation1);
        textStationName = findViewById(R.id.textView_reservation1_station_name);
        textStationAddress = findViewById(R.id.textView_reservation1_station_address);
        textChargerName = findViewById(R.id.textView_reservation1_charger_name);
        textChargerMode = findViewById(R.id.textView_reservation1_charger_mode);
        textChargerState = findViewById(R.id.textView_reservation1_charger_state);
        textReservationTimeNotFound = findViewById(R.id.textView_reservation1_time_notFound);
        btnReservationDate = findViewById(R.id.btn_reservation1_date);
        checkBoxReservationTimeAvailable = findViewById(R.id.checkbox_reservation1_available);
        listViewReservationTime = findViewById(R.id.listView_reservation1_time);

        // ????????? ??? ???????????? ?????????
        settingActionBar();
        settingScroll();

        // ?????? ??????(1) : ?????? ??????
        btnReservationDate.setOnClickListener(v -> showDatePicker());

        // ?????? ??????(2) : ?????? ????????? ????????? ?????? ??????
        checkBoxReservationTimeAvailable.setOnClickListener(v -> {
            if (checkBoxReservationTimeAvailable.isChecked()) {     // ??????
                List<ReservationTime> availableTimeTable =
                        chargerTimeTable.stream().filter(ReservationTime::isCanReserve).collect(Collectors.toList());

                if (availableTimeTable.size() == 0) {
                    listViewReservationTime.setVisibility(View.GONE);
                    textReservationTimeNotFound.setVisibility(View.VISIBLE);

                } else {
                    textReservationTimeNotFound.setVisibility(View.GONE);
                    listViewReservationTime.setVisibility(View.VISIBLE);

                    listViewReservationTime.setAdapter(new CustomReservationTimeList(this, availableTimeTable));
                }

            } else {                                                // ??????
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

        if (chargerInfo.getState() > 2) {
            listViewReservationTime.setVisibility(View.GONE);
            textReservationTimeNotFound.setVisibility(View.VISIBLE);

        } else {
            loadReservationTimeTable(0);
        }
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
            startActivity(new Intent(Reservation1Activity.this, MainActivity.class));

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

            PreferenceManager.setString(Reservation1Activity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
        }

        if (currentIntent.hasExtra("ChargerId")) {
            long chargerId = currentIntent.getLongExtra("ChargerId", -1);

            PreferenceManager.setLong(Reservation1Activity.this, "CHARGER_ID", chargerId);
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
        NestedScrollView scrollView = findViewById(R.id.scrollView_reservation1);

        listViewReservationTime.setOnTouchListener((v, event) -> {
            scrollView.requestDisallowInterceptTouchEvent(true);

            return false;
        });
    }

    private void loadChargerInfo() {
        String loginAccessToken = PreferenceManager.getString(Reservation1Activity.this, "LOGIN_ACCESS_TOKEN");
        long chargerId = PreferenceManager.getLong(Reservation1Activity.this, "CHARGER_ID");

        chargerService = new ChargerService(loginAccessToken);

        try {
            CommonResponse commonResponse = chargerService.execute(CHARGER_SERVICE_GET_INFO, chargerId).get();

            if (commonResponse.isSuccess()) {
                SingleResultResponse<ChargerInfoDto> singleResultResponse = (SingleResultResponse<ChargerInfoDto>) commonResponse;
                chargerInfo = singleResultResponse.getData();

                textStationName.setText(chargerInfo.getStation().getStationName());
                textStationAddress.setText(chargerInfo.getStation().getStationAddress());
                textChargerName.setText(chargerInfo.getChargerName());
                textChargerMode.setText(chargerInfo.stringValueOfMode());
                textChargerState.setText(chargerInfo.stringValueOfState());

            } else {
                String loadChargerInfoFailedMsg = "????????? ????????? ????????? ??? ????????????.";

                SnackBarManager.showMessage(findViewById(R.id.layout_reservation), loadChargerInfoFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("Charger", "Loading charger info failed.");
        }
    }

    private void loadReservationTimeTable(int day) {
        String loginAccessToken = PreferenceManager.getString(Reservation1Activity.this, "LOGIN_ACCESS_TOKEN");
        long chargerId = PreferenceManager.getLong(Reservation1Activity.this, "CHARGER_ID");

        chargerTimeTable = new ArrayList<>();

        reservationService = new ReservationService(loginAccessToken);
        reservationService.setChargerId(chargerId);

        try {
            CommonResponse commonResponse = reservationService.execute(RESERVATION_SERVICE_GET_TIME_TABLE, day).get();

            if (commonResponse.isSuccess()) {
                SingleResultResponse<ChargerTimeTableDto> singleResultResponse = (SingleResultResponse<ChargerTimeTableDto>) commonResponse;
                ChargerTimeTableDto timeTable = singleResultResponse.getData();

                timeTable
                        .getTimeTable()
                        .forEach((time, canReserve) -> chargerTimeTable.add(new ReservationTime(time, canReserve)));

                chargerTimeTable.sort(Comparator.comparing(ReservationTime::getTime));

                if (checkBoxReservationTimeAvailable.isChecked()) {
                    List<ReservationTime> availableTimeTable =
                            chargerTimeTable.stream().filter(ReservationTime::isCanReserve).collect(Collectors.toList());

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
                String loadChargerTimeTableFailedMsg = "?????? ?????? ????????? ????????? ??? ????????????.";

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
                            // ?????? ????????? ??????
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
        private final boolean canReserve;

        public ReservationTime(String time, boolean canReserve) {
            this.time = time;
            this.canReserve = canReserve;
        }
    }

    private class CustomReservationTimeList extends ArrayAdapter<ReservationTime> {

        private final Activity context;
        private final List<ReservationTime> timeTable;

        public CustomReservationTimeList(Activity context, List<ReservationTime> timeTable) {
            super(context, R.layout.listview_reservation1_time, timeTable);
            this.context = context;
            this.timeTable = timeTable;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listview_reservation1_time, null, true);

            TextView startTime = rowView.findViewById(R.id.listView_reservation_time_start);
            Button btnReservedTrue = rowView.findViewById(R.id.btn_reservation_time_true);
            Button btnReservedFalse = rowView.findViewById(R.id.btn_reservation_time_false);

            ReservationTime reservationTime = timeTable.get(position);

            startTime.setText(reservationTime.getTime());

            if (!reservationTime.isCanReserve()) {
                btnReservedTrue.setVisibility(View.GONE);
                btnReservedFalse.setVisibility(View.VISIBLE);

            } else {
                btnReservedFalse.setVisibility(View.GONE);
                btnReservedTrue.setVisibility(View.VISIBLE);

                btnReservedTrue.setOnClickListener(v -> {
                    String loginAccessToken = PreferenceManager.getString(Reservation1Activity.this, "LOGIN_ACCESS_TOKEN");
                    long chargerId = PreferenceManager.getLong(Reservation1Activity.this, "CHARGER_ID");

                    String startDateTime =
                            targetDate.format(DateTimeFormatter.ofPattern("yyyy.MM.dd")) + " " + reservationTime.getTime();

                    Intent intent = new Intent(Reservation1Activity.this, Reservation2Activity.class);
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
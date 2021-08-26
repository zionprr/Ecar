package com.example.capstonemainproject;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.example.capstonemainproject.domain.Car;
import com.example.capstonemainproject.domain.ReservationStatement;
import com.example.capstonemainproject.dto.request.reservation.RequestReservationDto;
import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.ListResultResponse;
import com.example.capstonemainproject.dto.response.common.SingleResultResponse;
import com.example.capstonemainproject.dto.response.custom.reservation.MaxEndDateTimeDto;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.infra.app.TextHyperLinker;
import com.example.capstonemainproject.service.CarService;
import com.example.capstonemainproject.service.ReservationService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RequiresApi(api = Build.VERSION_CODES.O)
public class Reservation2Activity extends AppCompatActivity {

    private static final int RESERVATION2_ACTIVITY_RESULT_FAIL = 102;
    private static final int CAR_SERVICE_GET_USER_CARS = -5;
    private static final int RESERVATION_SERVICE_GET_MAX_END_DATE = -27;
    private static final int RESERVATION_SERVICE_RESERVE = -28;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd - HH:mm");

    private Toolbar toolbarReservation;

    private TextView textStartDateTime, textEndDateTime, textMaxEndDateTime, textFares;
    private TextView textCarNumber, textCarModel, textCarType, linkCarRegistration;
    private LinearLayout layoutFares, layoutCarNotFound;
    private ListView listViewCar;
    private Button btnReservationComplete;

    private Spinner spinnerReservationTotalTime;
    private List<Integer> spinnerMinutes;

    private CarService carService;
    private ReservationService reservationService;

    private String stringOfStartDateTime;
    private LocalDateTime startDateTime, endDateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation2);

        // 인텐트 정보 저장
        saveIntentValues();

        // 화면 설정
        toolbarReservation = findViewById(R.id.toolbar_reservation2);
        textStartDateTime = findViewById(R.id.textView_reservation2_start_dateTime);
        textEndDateTime = findViewById(R.id.textView_reservation2_end_dateTime);
        textMaxEndDateTime = findViewById(R.id.textView_reservation2_max_end_dateTime);
        textFares = findViewById(R.id.textView_reservation2_fares);
        textCarNumber = findViewById(R.id.textView_reservation2_carNumber);
        textCarModel = findViewById(R.id.textView_reservation2_carModel);
        textCarType = findViewById(R.id.textView_reservation2_carType);
        linkCarRegistration = findViewById(R.id.link_car_registration);
        layoutFares = findViewById(R.id.layout_reservation2_fares);
        layoutCarNotFound = findViewById(R.id.layout_reservation2_car_notFound);
        listViewCar = findViewById(R.id.listView_reservation2_car);
        btnReservationComplete = findViewById(R.id.btn_reservation2_complete);
        spinnerReservationTotalTime = findViewById(R.id.spinner_reservation2_total_time);

        // 상단바 및 스크롤
        settingActionBar();
        settingScroll();

        // 화면 동작(1) : 차량 등록 링크
        linkCarRegistration.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(Reservation2Activity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(Reservation2Activity.this, CarRegistrationActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

            startActivity(intent);
        });

        // 화면 동작(2) : 예약 완료
        btnReservationComplete.setOnClickListener(v -> {
            long chargerId = PreferenceManager.getLong(Reservation2Activity.this, "CHARGER_ID");
            long carId = PreferenceManager.getLong(Reservation2Activity.this, "CAR_ID");

            if (endDateTime == null || carId == -1) {
                String inputValuesEmptyMsg = "예약 시간과 차량을 모두 선택해야 합니다.";

                SnackBarManager.showMessage(v, inputValuesEmptyMsg);

            } else {
                String loginAccessToken = PreferenceManager.getString(Reservation2Activity.this, "LOGIN_ACCESS_TOKEN");
                RequestReservationDto requestReservationDto = getRequestReservationDto(chargerId, carId);

                reservationService = new ReservationService(loginAccessToken, requestReservationDto);

                try {
                    CommonResponse commonResponse = reservationService.execute(RESERVATION_SERVICE_RESERVE).get();

                    if (commonResponse.isSuccess()) {
                        SingleResultResponse<ReservationStatement> singleResultResponse = (SingleResultResponse<ReservationStatement>) commonResponse;

                        Intent intent = new Intent();
                        intent.putExtra("RESERVATION_STATEMENT", singleResultResponse.getData());

                        setResult(RESULT_OK, intent);

                    } else {
                        setResult(RESERVATION2_ACTIVITY_RESULT_FAIL);
                    }

                    finish();

                } catch (ExecutionException | InterruptedException e) {
                    Log.w("Reservation", "Reservation failed.");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMaxEndDateTime();
        loadUserCarList();
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
            startActivity(new Intent(Reservation2Activity.this, MainActivity.class));

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

            PreferenceManager.setString(Reservation2Activity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
        }

        if (currentIntent.hasExtra("ChargerId")) {
            long chargerId = currentIntent.getLongExtra("ChargerId", -1);

            PreferenceManager.setLong(Reservation2Activity.this, "CHARGER_ID", chargerId);
        }

        stringOfStartDateTime = currentIntent.getStringExtra("StartDateTime");
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
        NestedScrollView scrollView = findViewById(R.id.scrollView_reservation2);

        listViewCar.setOnTouchListener((v, event) -> {
            scrollView.requestDisallowInterceptTouchEvent(true);

            return false;
        });
    }

    private void loadMaxEndDateTime() {
        String loginAccessToken = PreferenceManager.getString(Reservation2Activity.this, "LOGIN_ACCESS_TOKEN");
        long chargerId = PreferenceManager.getLong(Reservation2Activity.this, "CHARGER_ID");

        reservationService = new ReservationService(loginAccessToken, chargerId, stringOfStartDateTime);

        try {
            CommonResponse commonResponse = reservationService.execute(RESERVATION_SERVICE_GET_MAX_END_DATE).get();

            if (commonResponse.isSuccess()) {
                SingleResultResponse<MaxEndDateTimeDto> singleResultResponse = (SingleResultResponse<MaxEndDateTimeDto>) commonResponse;
                MaxEndDateTimeDto maxEndDateTime = singleResultResponse.getData();

                textStartDateTime.setText(maxEndDateTime.getTargetDateTime().format(dateTimeFormatter));
                textMaxEndDateTime.setText(
                        String.format("%s (%d 분)",
                                maxEndDateTime.getMaxEndDateTime().format(dateTimeFormatter),
                                maxEndDateTime.calMinuteDifference()
                        )
                );

                startDateTime = maxEndDateTime.getTargetDateTime();
                settingSpinner(maxEndDateTime);

            } else {
                String loadMaxEndDateFailedMsg = "최대 예약 시간 정보를 불러올 수 없습니다.";

                SnackBarManager.showMessage(findViewById(R.id.scrollView_reservation2), loadMaxEndDateFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("Reservation", "Loading max end datetime failed.");
        }
    }

    private void loadUserCarList() {
        String loginAccessToken = PreferenceManager.getString(Reservation2Activity.this, "LOGIN_ACCESS_TOKEN");

        carService = new CarService(loginAccessToken);

        try {
            CommonResponse commonResponse = carService.execute(CAR_SERVICE_GET_USER_CARS).get();

            if (commonResponse.isSuccess()) {
                ListResultResponse<Car> listResultResponse = (ListResultResponse<Car>) commonResponse;
                List<Car> userCarList = listResultResponse.getDataList();

                if (userCarList.size() != 0) {
                    layoutCarNotFound.setVisibility(View.GONE);
                    listViewCar.setVisibility(View.VISIBLE);

                    listViewCar.setAdapter(new CustomCarList(this, userCarList));

                } else {
                    listViewCar.setVisibility(View.GONE);
                    layoutCarNotFound.setVisibility(View.VISIBLE);

                    TextHyperLinker.makeTextViewHyperLink(linkCarRegistration);
                }

            } else {
                String loadCarListFailedMsg = "차량 정보를 불러올 수 없습니다.";

                SnackBarManager.showMessage(findViewById(R.id.scrollView_reservation2), loadCarListFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("Car", "Loading user car list failed.");
        }
    }

    private void settingSpinner(MaxEndDateTimeDto maxEndDateTime) {
        spinnerMinutes = new ArrayList<>();

        for (int i = 0; i <= maxEndDateTime.calMinuteDifference(); i = i + 30) {
            spinnerMinutes.add(i);
        }

        ArrayAdapter<Integer> arrayAdapter =
                new ArrayAdapter<>(Reservation2Activity.this, android.R.layout.simple_spinner_dropdown_item, spinnerMinutes);

        spinnerReservationTotalTime.setAdapter(arrayAdapter);
        spinnerReservationTotalTime.setFocusable(true);
        spinnerReservationTotalTime.setSelection(0);
        spinnerReservationTotalTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LocalDateTime targetDateTime = maxEndDateTime.getTargetDateTime();
                int fares = (int) ((spinnerMinutes.get(position) / 60.0) * maxEndDateTime.getFaresPerHour());

                if (position != 0) {
                    endDateTime = targetDateTime.plusMinutes(spinnerMinutes.get(position));

                    layoutFares.setVisibility(View.VISIBLE);

                    textEndDateTime.setText(endDateTime.format(dateTimeFormatter));
                    textFares.setText(String.format("%d  원", fares));

                } else {
                    endDateTime = null;

                    layoutFares.setVisibility(View.GONE);

                    textEndDateTime.setText("-");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private RequestReservationDto getRequestReservationDto(long chargerId, long carId) {
        RequestReservationDto requestReservationDto = new RequestReservationDto();
        requestReservationDto.setChargerId(chargerId);
        requestReservationDto.setCarId(carId);
        requestReservationDto.setStart(startDateTime);
        requestReservationDto.setEnd(endDateTime);

        return requestReservationDto;
    }

    private class CustomCarList extends ArrayAdapter<Car> {

        private final Activity context;
        private final List<Car> userCarList;

        public CustomCarList(Activity context, List<Car> userCarList) {
            super(context, R.layout.listview_reservation2_car, userCarList);
            this.context = context;
            this.userCarList = userCarList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listview_reservation2_car, null, true);

            TextView carNumber = rowView.findViewById(R.id.listView_reservation2_car_number);

            Car car = userCarList.get(position);

            carNumber.setText(car.getCarNumber());

            rowView.setOnClickListener(v -> {
                PreferenceManager.setLong(Reservation2Activity.this, "CAR_ID", car.getId());

                textCarNumber.setText(car.getCarNumber());
                textCarModel.setText(String.format("%s %s", car.getCarModelYear(), car.getCarModel()));
                textCarType.setText(car.getCarType());
            });

            return rowView;
        }
    }
}
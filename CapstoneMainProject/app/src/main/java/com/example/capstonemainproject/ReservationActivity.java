package com.example.capstonemainproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class ReservationActivity extends AppCompatActivity {

    Button btnEnd;
    RadioButton rBtnCalendar,rBtnTime;
    CalendarView calView;
    TimePicker tPicker;
    int selectYear, selectMonth, selectDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //setTitle("시간 예약");

        // 화면설정
        btnEnd = (Button)findViewById(R.id.btnEnd);
        rBtnCalendar = (RadioButton)findViewById(R.id.rBtnCalendar);
        rBtnTime = (RadioButton)findViewById(R.id.rBtnTime);
        tPicker = (TimePicker)findViewById(R.id.timePicker);
        calView = (CalendarView)findViewById(R.id.calendarView);

        // 시계, 캘린더 비가시화 초기설정
        tPicker.setVisibility(View.INVISIBLE);
        calView.setVisibility(View.INVISIBLE);

        // 캘린더형 날짜예약
        rBtnCalendar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                tPicker.setVisibility(View.INVISIBLE);
                calView.setVisibility(View.VISIBLE);
            }
        });

        // 시계형 예약 시간예약
        rBtnTime.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                calView.setVisibility(View.INVISIBLE);
                tPicker.setVisibility(View.VISIBLE);
            }
        });

        // 예약 확인
        btnEnd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Toast.makeText(getApplicationContext(),
                        Integer.toString(selectYear)+"년 " +
                                Integer.toString(selectMonth)+"월 " +
                                Integer.toString(selectDay)+"일 " +
                                Integer.toString(tPicker.getCurrentHour())+"시 " +
                                Integer.toString(tPicker.getCurrentMinute())+"분 예약되었습니다.",
                        Toast.LENGTH_LONG).show();

                Intent intent = new Intent(ReservationActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // 캘린더 날짜 변경
        calView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                selectYear = year;
                selectMonth = month + 1;
                selectDay = dayOfMonth;
            }
        });
    }
}

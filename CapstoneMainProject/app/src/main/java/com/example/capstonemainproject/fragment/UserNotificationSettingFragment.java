package com.example.capstonemainproject.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.fragment.app.Fragment;

import com.example.capstonemainproject.R;
import com.example.capstonemainproject.domain.User;
import com.example.capstonemainproject.dto.request.user.UpdateNotificationDto;
import com.example.capstonemainproject.dto.resoponse.common.CommonResponse;
import com.example.capstonemainproject.dto.resoponse.common.SingleResultResponse;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.UserBasicService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class UserNotificationSettingFragment extends Fragment {

    private static final int USER_BASIC_SERVICE_GET_USER_INFO = -1;
    private static final int USER_BASIC_SERVICE_UPDATE_NOTIFICATION = -4;

    private final List<Integer> minutes = new ArrayList<>();

    private Context currentContext;
    private View currentView;

    private LinearLayout layoutNotification1, layoutNotification2;
    private Switch switchNotification1, switchNotification2;
    private Spinner spinnerNotification1, spinnerNotification2;

    private String loginAccessToken;
    private UserBasicService userBasicService;

    private boolean isChanged;
    private int minutesNotification1, minutesNotification2;

    public UserNotificationSettingFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        currentContext = context;

        if (getArguments() != null) {
            loginAccessToken = getArguments().getString("LOGIN_ACCESS_TOKEN");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.fragment_user_notification_setting, container, false);

        // 화면 설정
        layoutNotification1 = currentView.findViewById(R.id.layout_user_setting_notification1);
        layoutNotification2 = currentView.findViewById(R.id.layout_user_setting_notification2);
        switchNotification1 = currentView.findViewById(R.id.switch_user_setting_notification1);
        switchNotification2 = currentView.findViewById(R.id.switch_user_setting_notification2);
        spinnerNotification1 = currentView.findViewById(R.id.spinner_user_setting_notification1);
        spinnerNotification2 = currentView.findViewById(R.id.spinner_user_setting_notification2);

        // 스피너 목록
        for (int i = 1; i <= 60; i++) {
            minutes.add(i);
        }

        // 화면 동작 : 스위칭
        switchNotification1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isChanged = true;

            if (isChecked) {
                layoutNotification1.setVisibility(View.VISIBLE);

                settingSpinner(spinnerNotification1, minutesNotification1 - 1);

            } else {
                layoutNotification1.setVisibility(View.INVISIBLE);
            }
        });

        switchNotification2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isChanged = true;

            if (isChecked) {
                layoutNotification2.setVisibility(View.VISIBLE);

                settingSpinner(spinnerNotification2, minutesNotification1 - 1);

            } else {
                layoutNotification2.setVisibility(View.INVISIBLE);
            }
        });

        return currentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserNotification();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isChanged) {
            updateUserNotification();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (isChanged) {
            updateUserNotification();
        }
    }

    private void loadUserNotification() {
        userBasicService = new UserBasicService(loginAccessToken);

        try {
            CommonResponse commonResponse = userBasicService.execute(USER_BASIC_SERVICE_GET_USER_INFO).get();

            if (commonResponse.isSuccess()) {
                SingleResultResponse<User> singleResultResponse = (SingleResultResponse<User>) commonResponse;
                User user = singleResultResponse.getData();

                switchNotification1.setChecked(user.isOnNotificationOfReservationStart());
                minutesNotification1 = user.getNotificationMinutesBeforeReservationStart();

                if (user.isOnNotificationOfReservationStart()) {
                    layoutNotification1.setVisibility(View.VISIBLE);

                    settingSpinner(spinnerNotification1, minutesNotification1 - 1);

                } else {
                    layoutNotification1.setVisibility(View.INVISIBLE);
                }

                switchNotification2.setChecked(user.isOnNotificationOfChargingEnd());
                minutesNotification2 = user.getNotificationMinutesBeforeChargingEnd();

                if (user.isOnNotificationOfChargingEnd()) {
                    layoutNotification2.setVisibility(View.VISIBLE);

                    settingSpinner(spinnerNotification2, minutesNotification2 - 1);

                } else {
                    layoutNotification2.setVisibility(View.INVISIBLE);
                }

            } else {
                String loadUserNotificationFailedMsg = "사용자 알림 정보를 불러올 수 없습니다.";

                SnackBarManager.showMessage(currentView, loadUserNotificationFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("User", "Loading user info failed.");
        }
    }

    private void settingSpinner(Spinner spinner, int defaultPosition) {
        ArrayAdapter<Integer> arrayAdapter =
                new ArrayAdapter<>(currentContext, android.R.layout.simple_spinner_dropdown_item, minutes);

        spinner.setAdapter(arrayAdapter);
        spinner.setFocusable(true);
        spinner.setSelection(defaultPosition);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                isChanged = true;

                if (spinner.equals(spinnerNotification1)) {
                    minutesNotification1 = position + 1;
                }

                if (spinner.equals(spinnerNotification2)) {
                    minutesNotification2 = position + 1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void updateUserNotification() {
        UpdateNotificationDto updateNotificationDto = new UpdateNotificationDto();
        updateNotificationDto.setOnNotificationOfReservationStart(switchNotification1.isChecked());
        updateNotificationDto.setMinutesBeforeReservationStart(minutesNotification1);
        updateNotificationDto.setOnNotificationOfChargingEnd(switchNotification2.isChecked());
        updateNotificationDto.setMinutesBeforeChargingEnd(minutesNotification2);

        userBasicService = new UserBasicService(loginAccessToken, updateNotificationDto);
        userBasicService.execute(USER_BASIC_SERVICE_UPDATE_NOTIFICATION);
    }
}

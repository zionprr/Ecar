package com.example.capstonemainproject.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.capstonemainproject.R;
import com.example.capstonemainproject.domain.User;
import com.example.capstonemainproject.dto.request.user.UpdateUserDto;
import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.SingleResultResponse;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.UserBasicService;

import java.util.concurrent.ExecutionException;

public class UserInfoSettingFragment extends Fragment {

    private static final int USER_BASIC_SERVICE_GET_USER_INFO = -1;
    private static final int USER_BASIC_SERVICE_UPDATE_USER_INFO = -2;

    private Context currentContext;
    private View currentView;

    private EditText eTextUserName, eTextPhoneNumber;
    private Button btnUserNameChange, btnPhoneNumberChange;

    private String loginAccessToken;

    private UserBasicService userBasicService;

    public UserInfoSettingFragment() {

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
        currentView = inflater.inflate(R.layout.fragment_user_info_setting, container, false);

        // 화면 설정
        eTextUserName = currentView.findViewById(R.id.editText_user_setting_name);
        eTextPhoneNumber = currentView.findViewById(R.id.editText_user_setting_phoneNumber);
        btnUserNameChange = currentView.findViewById(R.id.btn_user_setting_name_change);
        btnPhoneNumberChange = currentView.findViewById(R.id.btn_user_setting_phoneNumber_change);

        // 화면 동작(1) : 사용자 이름 수정
        btnUserNameChange.setOnClickListener(v -> {
            String userName = eTextUserName.getText().toString();

            if (userName.isEmpty()) {
                String userNameEmptyMsg = "사용자 이름을 입력하세요.";

                eTextUserName.setText(PreferenceManager.getString(currentContext, "USER_NAME"));

                SnackBarManager.showMessage(v, userNameEmptyMsg);

            } else {
                showDialogForUpdateUserName(userName);
            }
        });

        // 화면 동작(2) : 연락처 수정
        btnPhoneNumberChange.setOnClickListener(v -> {
            String phoneNumber = eTextPhoneNumber.getText().toString();

            if (phoneNumber.isEmpty()) {
                String phoneNumberEmptyMsg = "전화번호를 입력하세요.";

                eTextPhoneNumber.setText(PreferenceManager.getString(currentContext, "USER_PHONE_NUMBER"));

                SnackBarManager.showMessage(v, phoneNumberEmptyMsg);

            } else {
                showDialogForUpdateUserPhoneNumber(phoneNumber);
            }
        });

        return currentView;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserInfo();
    }

    private void loadUserInfo() {
        userBasicService = new UserBasicService(loginAccessToken);

        try {
            CommonResponse commonResponse = userBasicService.execute(USER_BASIC_SERVICE_GET_USER_INFO).get();

            if (commonResponse.isSuccess()) {
                SingleResultResponse<User> singleResultResponse = (SingleResultResponse<User>) commonResponse;
                User user = singleResultResponse.getData();

                PreferenceManager.setString(currentContext, "USER_NAME", user.getName());
                PreferenceManager.setString(currentContext, "USER_PHONE_NUMBER", user.getPhoneNumber());

                eTextUserName.setText(user.getName());
                eTextPhoneNumber.setText(user.getPhoneNumber());

            } else {
                String loadUserInfoFailedMsg = "사용자 정보를 불러올 수 없습니다.";

                SnackBarManager.showMessage(currentView, loadUserInfoFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("User", "Loading user info failed.");
        }
    }

    private void showDialogForUpdateUserName(String userName) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(currentContext);

        alertDialogBuilder
                .setTitle("사용자 이름 변경")
                .setMessage(String.format("'%s' (으)로\n변경하시겠습니까?", userName))
                .setCancelable(true)
                .setPositiveButton("확인", (dialog, which) -> {
                    UpdateUserDto updateUserDto = new UpdateUserDto();
                    updateUserDto.setUserName(userName);

                    userBasicService = new UserBasicService(loginAccessToken, updateUserDto);
                    userBasicService.execute(USER_BASIC_SERVICE_UPDATE_USER_INFO);
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    eTextUserName.setText(PreferenceManager.getString(currentContext, "USER_NAME"));

                    dialog.cancel();
                })
                .create()
                .show();
    }

    private void showDialogForUpdateUserPhoneNumber(String phoneNumber) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(currentContext);

        alertDialogBuilder
                .setTitle("연락처 변경")
                .setMessage(String.format("'%s' (으)로\n변경하시겠습니까?", phoneNumber))
                .setCancelable(true)
                .setPositiveButton("확인", (dialog, which) -> {
                    UpdateUserDto updateUserDto = new UpdateUserDto();
                    updateUserDto.setPhoneNumber(phoneNumber);

                    userBasicService = new UserBasicService(loginAccessToken, updateUserDto);
                    userBasicService.execute(USER_BASIC_SERVICE_UPDATE_USER_INFO);
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    eTextPhoneNumber.setText(PreferenceManager.getString(currentContext, "USER_PHONE_NUMBER"));

                    dialog.cancel();
                })
                .create()
                .show();
    }
}

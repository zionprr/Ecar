package com.example.capstonemainproject.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.example.capstonemainproject.R;
import com.example.capstonemainproject.dto.request.user.UpdatePasswordDto;
import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.UserBasicService;

import java.util.concurrent.ExecutionException;

public class UserPasswordSettingFragment extends Fragment {

    private static final int USER_BASIC_SERVICE_UPDATE_PASSWORD = -3;

    private Context currentContext;
    private View currentView;

    private EditText eTextPassword, eTextNewPassword, eTextNewPasswordCheck;
    private Button btnPasswordChange;

    private String loginAccessToken;

    private UserBasicService userBasicService;

    public UserPasswordSettingFragment() {
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
        currentView = inflater.inflate(R.layout.fragment_user_password_setting, container, false);

        // 화면 설정
        eTextPassword = currentView.findViewById(R.id.editText_user_setting_password);
        eTextNewPassword = currentView.findViewById(R.id.editText_user_setting_new_password);
        eTextNewPasswordCheck = currentView.findViewById(R.id.editText_user_setting_new_password_check);
        btnPasswordChange = currentView.findViewById(R.id.btn_user_setting_password_change);

        // 화면 동작 : 비밀번호 변경
        btnPasswordChange.setOnClickListener(v -> {
            String currentPassword = eTextPassword.getText().toString();
            String newPassword = eTextNewPassword.getText().toString();
            String newPasswordCheck = eTextNewPasswordCheck.getText().toString();

            if (!newPassword.equals(newPasswordCheck)) {
                String passwordMismatchedMsg = "새 비밀번호가 일치하지 않습니다.";

                SnackBarManager.showMessage(v, passwordMismatchedMsg);

            } else {
                showDialogForUpdatePassword(currentPassword, newPassword);
            }
        });

        return currentView;
    }

    private void showDialogForUpdatePassword(String currentPassword, String newPassword) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(currentContext);

        alertDialogBuilder
                .setTitle("비밀번호 변경")
                .setMessage("확인을 누르시면 비밀번호가 변경되며, 자동으로 로그아웃 됩니다.")
                .setCancelable(true)
                .setPositiveButton("확인", (dialog, which) -> {
                    UpdatePasswordDto updatePasswordDto = new UpdatePasswordDto();
                    updatePasswordDto.setCurrentPassword(currentPassword);
                    updatePasswordDto.setNewPassword(newPassword);

                    userBasicService = new UserBasicService(loginAccessToken, updatePasswordDto);

                    try {
                        CommonResponse commonResponse = userBasicService.execute(USER_BASIC_SERVICE_UPDATE_PASSWORD).get();

                        if (commonResponse.isSuccess()) {
                            getActivity().finish();
                            startActivity(new Intent(currentContext, LoginActivity.class));

                        } else {
                            String updatePasswordFailedMsg = "현재 비밀번호가 일치하지 않습니다.";

                            SnackBarManager.showMessage(currentView, updatePasswordFailedMsg);
                        }

                    } catch (ExecutionException | InterruptedException e) {
                        Log.w("User", "Loading user info failed.");
                    }
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.cancel())
                .create()
                .show();
    }
}

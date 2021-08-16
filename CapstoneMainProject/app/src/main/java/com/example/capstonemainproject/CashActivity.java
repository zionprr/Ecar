package com.example.capstonemainproject;

import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.capstonemainproject.domain.BankAccount;
import com.example.capstonemainproject.domain.User;
import com.example.capstonemainproject.dto.request.bank.CashInDto;
import com.example.capstonemainproject.dto.request.bank.CashOutDto;
import com.example.capstonemainproject.dto.resoponse.common.CommonResponse;
import com.example.capstonemainproject.dto.resoponse.common.ListResultResponse;
import com.example.capstonemainproject.dto.resoponse.common.SingleResultResponse;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.BankService;
import com.example.capstonemainproject.service.UserBasicService;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class CashActivity extends AppCompatActivity {

    private static final int USER_BASIC_SERVICE_GET_USER_INFO = -1;
    private static final int BANK_SERVICE_GET_USER_ACCOUNTS = -8;
    private static final int BANK_SERVICE_CHARGE_CASH = -13;
    private static final int BANK_SERVICE_REFUND_CASH = -14;

    private Toolbar toolbarCash;

    private TextView textCash, textAccountInfo;
    private TextView textMainAccountNotFound, linkBankRegistration;
    private Button btnCashCharge, btnCashRefund;

    private UserBasicService userBasicService;
    private BankService bankService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cash);

        // 로그인 토큰 저장
        saveLoginToken();

        // 화면 설정
        toolbarCash = findViewById(R.id.toolbar_cash);
        textCash = findViewById(R.id.textView_cash);
        textAccountInfo = findViewById(R.id.textView_account_info);
        textMainAccountNotFound = findViewById(R.id.textView_main_account_notFound);
        linkBankRegistration = findViewById(R.id.link_bank_registration);
        btnCashCharge = findViewById(R.id.btn_cash_charge);
        btnCashRefund = findViewById(R.id.btn_cash_refund);

        // 상단바 설정
        settingActionBar();

        // 하이퍼 링크
        makeTextViewHyperlink(linkBankRegistration);

        // 화면 동작(1) : 금액 충전
        btnCashCharge.setOnClickListener(v -> showDialogForChargeCash());

        // 화면 동작(2) : 금액 환불
        btnCashRefund.setOnClickListener(v -> {
            int userCash = PreferenceManager.getInt(com.example.capstonemainproject.CashActivity.this, "USER_CASH");

            showDialogForRefundCash(userCash);
        });

        // 화면 동작(3) : 계좌 연결
        linkBankRegistration.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.CashActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(com.example.capstonemainproject.CashActivity.this, com.example.capstonemainproject.BankRegistrationActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

            startActivity(intent);
        });
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onResume() {
        super.onResume();
        loadUserCash();
        loadUserMainAccount();
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
            startActivity(new Intent(com.example.capstonemainproject.CashActivity.this, com.example.capstonemainproject.MainActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

    }

    private void saveLoginToken() {
        if (getIntent().hasExtra("LOGIN_ACCESS_TOKEN")) {
            String loginAccessToken = getIntent().getStringExtra("LOGIN_ACCESS_TOKEN");

            PreferenceManager.setString(com.example.capstonemainproject.CashActivity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
        }
    }

    private void settingActionBar() {
        setSupportActionBar(toolbarCash);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_left_solid);
    }

    private void makeTextViewHyperlink(TextView view) {
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(view.getText());
        spannableStringBuilder.setSpan(new URLSpan("#"), 0, spannableStringBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        view.setText(spannableStringBuilder, TextView.BufferType.SPANNABLE);
    }

    private void loadUserCash() {
        String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.CashActivity.this, "LOGIN_ACCESS_TOKEN");

        userBasicService = new UserBasicService(loginAccessToken);

        try {
            CommonResponse commonResponse = userBasicService.execute(USER_BASIC_SERVICE_GET_USER_INFO).get();

            if (commonResponse.isSuccess()) {
                SingleResultResponse<User> singleResultResponse = (SingleResultResponse<User>) commonResponse;
                User user = singleResultResponse.getData();

                textCash.setText(String.format("%d (원)", user.getCash()));

                PreferenceManager.setInt(com.example.capstonemainproject.CashActivity.this, "USER_CASH", user.getCash());

            } else {
                String loadUserInfoFailedMsg = "사용자 정보를 불러올 수 없습니다.";

                SnackBarManager.showMessage(findViewById(R.id.layout_cash), loadUserInfoFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("User", "Loading user info failed.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadUserMainAccount() {
        String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.CashActivity.this, "LOGIN_ACCESS_TOKEN");

        bankService = new BankService(loginAccessToken);

        try {
            CommonResponse commonResponse = bankService.execute(BANK_SERVICE_GET_USER_ACCOUNTS).get();

            if (commonResponse.isSuccess()) {
                ListResultResponse<BankAccount> listResultResponse = (ListResultResponse<BankAccount>) commonResponse;
                List<BankAccount> userAccountList = listResultResponse.getDataList();

                if (userAccountList.size() != 0) {
                    BankAccount mainAccount =
                            userAccountList.stream()
                                    .filter(BankAccount::isMainUsed)
                                    .findFirst()
                                    .get();

                    textAccountInfo.setVisibility(View.VISIBLE);
                    textAccountInfo.setText(String.format("%s %s", mainAccount.getBankName(), mainAccount.getBankAccountNumber()));

                    textMainAccountNotFound.setVisibility(View.GONE);
                    linkBankRegistration.setVisibility(View.GONE);

                } else {
                    textAccountInfo.setVisibility(View.GONE);
                    textMainAccountNotFound.setVisibility(View.VISIBLE);
                    linkBankRegistration.setVisibility(View.VISIBLE);
                }

            } else {
                String loadAccountListFailedMsg = "계좌 정보를 불러올 수 없습니다.";

                SnackBarManager.showMessage(findViewById(R.id.layout_cash), loadAccountListFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("Bank", "Loading user account list failed.");
        }
    }

    private void showDialogForChargeCash() {
        Dialog dialog = new Dialog(com.example.capstonemainproject.CashActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_cash_charge);

        dialog.show();

        // 커스텀 다이얼로그 설정
        EditText cashAmount = dialog.findViewById(R.id.editText_cash_charge);
        EditText payPassword = dialog.findViewById(R.id.editText_cash_charge_payPassword);
        Button btnOk = dialog.findViewById(R.id.btn_cash_charge_ok);
        Button btnCancel = dialog.findViewById(R.id.btn_cash_charge_cancel);

        btnOk.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.CashActivity.this, "LOGIN_ACCESS_TOKEN");
            CashInDto cashInDto = getCashInDto(Integer.parseInt(cashAmount.getText().toString()), payPassword.getText().toString());

            bankService = new BankService(loginAccessToken, cashInDto);

            try {
                CommonResponse commonResponse = bankService.execute(BANK_SERVICE_CHARGE_CASH).get();

                if (commonResponse.isSuccess()) {
                    dialog.dismiss();

                    finish();
                    startActivity(new Intent(com.example.capstonemainproject.CashActivity.this, com.example.capstonemainproject.CashActivity.class));

                } else {
                    String chargeCashFailedMsg = "연결된 계좌가 없거나 결제 비밀번호가 틀립니다.";

                    SnackBarManager.showMessage(findViewById(R.id.layout_cash), chargeCashFailedMsg);
                }

            } catch (ExecutionException | InterruptedException e) {
                Log.w("Bank", "Cash charge failed.");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void showDialogForRefundCash(int userCash) {
        Dialog dialog = new Dialog(com.example.capstonemainproject.CashActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_cash_refund);

        dialog.show();

        // 커스텀 다이얼로그 설정
        TextView currentCash = dialog.findViewById(R.id.textView_current_cash);
        EditText cashAmount = dialog.findViewById(R.id.editText_cash_refund);
        EditText payPassword = dialog.findViewById(R.id.editText_cash_refund_payPassword);
        Button btnOk = dialog.findViewById(R.id.btn_cash_refund_ok);
        Button btnCancel = dialog.findViewById(R.id.btn_cash_refund_cancel);

        currentCash.setText(String.valueOf(userCash));

        btnOk.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.CashActivity.this, "LOGIN_ACCESS_TOKEN");
            CashOutDto cashOutDto = getCashOutDto(Integer.parseInt(cashAmount.getText().toString()), payPassword.getText().toString());

            bankService = new BankService(loginAccessToken, cashOutDto);

            try {
                CommonResponse commonResponse = bankService.execute(BANK_SERVICE_REFUND_CASH).get();

                if (commonResponse.isSuccess()) {
                    dialog.dismiss();

                    finish();
                    startActivity(new Intent(com.example.capstonemainproject.CashActivity.this, com.example.capstonemainproject.CashActivity.class));

                } else {
                    String refundCashFailedMsg = "연결된 계좌가 없거나 결제 비밀번호가 틀립니다.\n또는 보유 금액을 초과한 요청입니다.";

                    SnackBarManager.showMessage(findViewById(R.id.layout_cash), refundCashFailedMsg);
                }

            } catch (ExecutionException | InterruptedException e) {
                Log.w("Bank", "Cash refund failed.");
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private CashInDto getCashInDto(int cashAmount, String paymentPassword) {
        CashInDto cashInDto = new CashInDto();
        cashInDto.setAmount(cashAmount);
        cashInDto.setPaymentPassword(paymentPassword);

        return cashInDto;
    }

    private CashOutDto getCashOutDto(int cashAmount, String paymentPassword) {
        CashOutDto cashOutDto = new CashOutDto();
        cashOutDto.setAmount(cashAmount);
        cashOutDto.setPaymentPassword(paymentPassword);

        return cashOutDto;
    }
}
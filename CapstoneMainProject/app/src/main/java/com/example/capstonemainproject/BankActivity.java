package com.example.capstonemainproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.capstonemainproject.domain.BankAccount;
import com.example.capstonemainproject.dto.resoponse.common.CommonResponse;
import com.example.capstonemainproject.dto.resoponse.common.ListResultResponse;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.BankService;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class BankActivity extends AppCompatActivity {

    private static final int BANK_REGISTRATION_ACTIVITY_RESULT_OK = 101;
    private static final int BANK_SERVICE_GET_USER_ACCOUNTS = -8;
    private static final int BANK_SERVICE_DELETE_USER_ACCOUNT = -11;
    private static final int BANK_SERVICE_CHANGE_MAIN_USED_ACCOUNT = -12;

    private ListView listViewAccount;
    private TextView textAccountNotFound;
    private ImageView iViewNewAccount;

    private Toolbar toolbarBank;

    private BankService bankService;

    private final ActivityResultLauncher<Intent> startActivityResultForBank =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == BANK_REGISTRATION_ACTIVITY_RESULT_OK) {
                            finish();
                            startActivity(new Intent(com.example.capstonemainproject.BankActivity.this, com.example.capstonemainproject.BankActivity.class));
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);

        // 로그인 토큰 저장
        saveLoginToken();

        // 화면 설정
        listViewAccount = findViewById(R.id.listView_account);
        textAccountNotFound = findViewById(R.id.textView_account_notFound);
        iViewNewAccount = findViewById(R.id.imageView_new_account);
        toolbarBank = findViewById(R.id.toolbar_bank);

        // 상단바 설정
        settingActionBar();

        // 화면 동작
        iViewNewAccount.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.BankActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(com.example.capstonemainproject.BankActivity.this, BankRegistrationActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

            startActivityResultForBank.launch(intent);
        });
    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected void onResume() {
        super.onResume();
        loadUserAccountList();
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
            startActivity(new Intent(com.example.capstonemainproject.BankActivity.this, MainActivity.class));

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

            PreferenceManager.setString(com.example.capstonemainproject.BankActivity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
        }
    }

    private void settingActionBar() {
        setSupportActionBar(toolbarBank);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_left_solid);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadUserAccountList() {
        String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.BankActivity.this, "LOGIN_ACCESS_TOKEN");

        bankService = new BankService(loginAccessToken);

        try {
            CommonResponse commonResponse = bankService.execute(BANK_SERVICE_GET_USER_ACCOUNTS).get();

            if (commonResponse.isSuccess()) {
                ListResultResponse<BankAccount> listResultResponse = (ListResultResponse<BankAccount>) commonResponse;
                List<BankAccount> userAccountList = listResultResponse.getDataList();

                if (userAccountList.size() != 0) {
                    textAccountNotFound.setVisibility(View.GONE);
                    listViewAccount.setAdapter(new com.example.capstonemainproject.BankActivity.CustomAccountList(this, userAccountList));

                } else {
                    textAccountNotFound.setVisibility(View.VISIBLE);
                    listViewAccount.setVisibility(View.GONE);
                }

            } else {
                String loadAccountListFailedMsg = "계좌 정보를 불러올 수 없습니다.";

                SnackBarManager.showMessage(findViewById(R.id.scrollView_bank), loadAccountListFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("Bank", "Loading user account list failed.");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showDialogForAccountDetails(BankAccount account) {
        Dialog dialog = new Dialog(com.example.capstonemainproject.BankActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_bank_account_details);

        dialog.show();

        // 커스텀 다이얼로그 설정
        TextView bankName = dialog.findViewById(R.id.textView_account_bankName);
        TextView accountNumber = dialog.findViewById(R.id.textView_account_number);
        TextView owner = dialog.findViewById(R.id.textView_account_owner);
        TextView registeredAt = dialog.findViewById(R.id.textView_account_registeredAt);

        bankName.setText(account.getBankName());
        accountNumber.setText(account.getBankAccountNumber());
        owner.setText(account.getBankAccountOwner());
        registeredAt.setText(account.getRegisteredAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        Button btnOk = dialog.findViewById(R.id.btn_account_ok);
        Button btnChangeMain = dialog.findViewById(R.id.btn_account_change_main);
        Button btnDelete = dialog.findViewById(R.id.btn_account_delete);

        if (account.isMainUsed()) {
            btnChangeMain.setVisibility(View.INVISIBLE);

        } else {
            btnChangeMain.setVisibility(View.VISIBLE);
            btnChangeMain.setOnClickListener(v -> showDialogForMainAccountChange(account.getId()));
        }

        btnOk.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> showDialogForAccountDelete(account.getId()));
    }

    private void showDialogForMainAccountChange(long bankId) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(com.example.capstonemainproject.BankActivity.this);

        alertDialogBuilder
                .setTitle("주계좌 변경")
                .setMessage("선택한 계좌를 주사용 계좌로 변경하시겠습니까?")
                .setCancelable(true)
                .setPositiveButton("확인", (dialog, which) -> {
                    String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.BankActivity.this, "LOGIN_ACCESS_TOKEN");

                    bankService = new BankService(loginAccessToken, bankId);
                    bankService.execute(BANK_SERVICE_CHANGE_MAIN_USED_ACCOUNT);

                    finish();
                    startActivity(new Intent(com.example.capstonemainproject.BankActivity.this, com.example.capstonemainproject.BankActivity.class));
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.cancel())
                .create()
                .show();
    }

    private void showDialogForAccountDelete(long bankId) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(com.example.capstonemainproject.BankActivity.this);

        alertDialogBuilder
                .setTitle("계좌 삭제")
                .setMessage("선택한 계좌를 삭제하시겠습니까?")
                .setCancelable(true)
                .setPositiveButton("확인", (dialog, which) -> {
                    String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.BankActivity.this, "LOGIN_ACCESS_TOKEN");

                    bankService = new BankService(loginAccessToken, bankId);
                    bankService.execute(BANK_SERVICE_DELETE_USER_ACCOUNT);

                    finish();
                    startActivity(new Intent(com.example.capstonemainproject.BankActivity.this, com.example.capstonemainproject.BankActivity.class));
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.cancel())
                .create()
                .show();
    }

    private class CustomAccountList extends ArrayAdapter<BankAccount> {

        private final Activity context;
        private final List<BankAccount> userAccountList;

        @RequiresApi(api = Build.VERSION_CODES.N)
        public CustomAccountList(Activity context, List<BankAccount> userAccountList) {
            super(context, R.layout.listview_bank_account, userAccountList);
            this.context = context;

            this.userAccountList =
                    userAccountList.stream()
                            .sorted(Comparator.comparing(BankAccount::isMainUsed, Comparator.reverseOrder()))
                            .collect(Collectors.toList());
        }

        @Override
        @RequiresApi(api = Build.VERSION_CODES.O)
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listview_bank_account, null, true);

            TextView mainUsedAccount = rowView.findViewById(R.id.textView_main_account);
            TextView accountInfo = rowView.findViewById(R.id.listView_account_info);
            ImageView accountDetails = rowView.findViewById(R.id.imageView_account_details);

            BankAccount account = userAccountList.get(position);

            if (account.isMainUsed()) {
                mainUsedAccount.setVisibility(View.VISIBLE);

            } else {
                mainUsedAccount.setVisibility(View.GONE);
            }

            accountInfo.setText(String.format("%s %s", account.getBankName(), account.getBankAccountNumber()));
            accountDetails.setOnClickListener(v -> showDialogForAccountDetails(account));

            return rowView;
        }
    }
}
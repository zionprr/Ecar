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
import androidx.core.widget.NestedScrollView;

import com.example.capstonemainproject.domain.BankAccount;
import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.ListResultResponse;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.BankService;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.O)
public class BankActivity extends AppCompatActivity {

    private static final int BANK_REGISTRATION_ACTIVITY_RESULT_OK = 101;
    private static final int BANK_SERVICE_GET_USER_ACCOUNTS = -8;
    private static final int BANK_SERVICE_DELETE_USER_ACCOUNT = -11;
    private static final int BANK_SERVICE_CHANGE_MAIN_USED_ACCOUNT = -12;

    private Toolbar toolbarBank;

    private ListView listViewAccount;
    private TextView textAccountNotFound;
    private ImageView iViewNewAccount;

    private BankService bankService;

    private final ActivityResultLauncher<Intent> startActivityResultForBank =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == BANK_REGISTRATION_ACTIVITY_RESULT_OK) {
                            finish();
                            startActivity(new Intent(BankActivity.this, BankActivity.class));
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank);

        // ????????? ?????? ??????
        saveLoginToken();

        // ?????? ??????
        toolbarBank = findViewById(R.id.toolbar_bank);
        listViewAccount = findViewById(R.id.listView_account);
        textAccountNotFound = findViewById(R.id.textView_account_notFound);
        iViewNewAccount = findViewById(R.id.imageView_new_account);

        // ????????? ?????? ??? ?????????
        settingActionBar();
        settingScroll();

        // ?????? ??????
        iViewNewAccount.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(BankActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(BankActivity.this, BankRegistrationActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

            startActivityResultForBank.launch(intent);
        });
    }

    @Override
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
            startActivity(new Intent(BankActivity.this, MainActivity.class));

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

            PreferenceManager.setString(BankActivity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
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

    private void settingScroll() {
        NestedScrollView scrollView = findViewById(R.id.scrollView_bank);

        listViewAccount.setOnTouchListener((v, event) -> {
            scrollView.requestDisallowInterceptTouchEvent(true);

            return false;
        });
    }

    private void loadUserAccountList() {
        String loginAccessToken = PreferenceManager.getString(BankActivity.this, "LOGIN_ACCESS_TOKEN");

        bankService = new BankService(loginAccessToken);

        try {
            CommonResponse commonResponse = bankService.execute(BANK_SERVICE_GET_USER_ACCOUNTS).get();

            if (commonResponse.isSuccess()) {
                ListResultResponse<BankAccount> listResultResponse = (ListResultResponse<BankAccount>) commonResponse;
                List<BankAccount> userAccountList = listResultResponse.getDataList();

                if (userAccountList.size() != 0) {
                    textAccountNotFound.setVisibility(View.GONE);
                    listViewAccount.setVisibility(View.VISIBLE);

                    listViewAccount.setAdapter(new BankActivity.CustomAccountList(this, userAccountList));

                } else {
                    listViewAccount.setVisibility(View.GONE);
                    textAccountNotFound.setVisibility(View.VISIBLE);
                }

            } else {
                String loadAccountListFailedMsg = "?????? ????????? ????????? ??? ????????????.";

                SnackBarManager.showMessage(findViewById(R.id.scrollView_bank), loadAccountListFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("Bank", "Loading user account list failed.");
        }
    }

    private void showDialogForAccountDetails(BankAccount account) {
        Dialog dialog = new Dialog(BankActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_bank_account_details);

        dialog.show();

        // ????????? ??????????????? ??????
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
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BankActivity.this);

        alertDialogBuilder
                .setTitle("????????? ??????")
                .setMessage("????????? ????????? ????????? ????????? ?????????????????????????")
                .setCancelable(true)
                .setPositiveButton("??????", (dialog, which) -> {
                    String loginAccessToken = PreferenceManager.getString(BankActivity.this, "LOGIN_ACCESS_TOKEN");

                    bankService = new BankService(loginAccessToken, bankId);
                    bankService.execute(BANK_SERVICE_CHANGE_MAIN_USED_ACCOUNT);

                    finish();
                    startActivity(new Intent(BankActivity.this, BankActivity.class));
                })
                .setNegativeButton("??????", (dialog, which) -> dialog.cancel())
                .create()
                .show();
    }

    private void showDialogForAccountDelete(long bankId) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(BankActivity.this);

        alertDialogBuilder
                .setTitle("?????? ??????")
                .setMessage("????????? ????????? ?????????????????????????")
                .setCancelable(true)
                .setPositiveButton("??????", (dialog, which) -> {
                    String loginAccessToken = PreferenceManager.getString(BankActivity.this, "LOGIN_ACCESS_TOKEN");

                    bankService = new BankService(loginAccessToken, bankId);
                    bankService.execute(BANK_SERVICE_DELETE_USER_ACCOUNT);

                    finish();
                    startActivity(new Intent(BankActivity.this, BankActivity.class));
                })
                .setNegativeButton("??????", (dialog, which) -> dialog.cancel())
                .create()
                .show();
    }

    private class CustomAccountList extends ArrayAdapter<BankAccount> {

        private final Activity context;
        private final List<BankAccount> userAccountList;

        public CustomAccountList(Activity context, List<BankAccount> userAccountList) {
            super(context, R.layout.listview_bank_account, userAccountList);
            this.context = context;

            this.userAccountList =
                    userAccountList.stream()
                            .sorted(Comparator.comparing(BankAccount::isMainUsed, Comparator.reverseOrder()))
                            .collect(Collectors.toList());
        }

        @Override
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
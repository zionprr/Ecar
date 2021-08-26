package com.example.capstonemainproject;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.capstonemainproject.adapter.ReservationStatementFragmentAdapter;
import com.example.capstonemainproject.fragment.ReservationStatement1Fragment;
import com.example.capstonemainproject.fragment.ReservationStatement2Fragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

@RequiresApi(api = Build.VERSION_CODES.O)
public class ReservationStatementActivity extends AppCompatActivity {

    private final String[] tabTitles = {"예약/충전 목록", "충전 내역"};

    private Toolbar toolbarReservationStatement;

    private TabLayout tabLayoutReservationStatement;
    private ViewPager2 viewPager2ReservationStatement;

    private String loginAccessToken;
    private int requestPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation_statement);

        // 전달 데이터(로그인 토큰, 시작 위치) 저장
        saveIntentValues();

        // 화면 설정
        toolbarReservationStatement = findViewById(R.id.toolbar_reservation_statement);
        tabLayoutReservationStatement = findViewById(R.id.tabLayout_reservation_statement);
        viewPager2ReservationStatement = findViewById(R.id.viewPager2_reservation_statement);

        // 상단바 및 탭화면
        settingActionBar();
        settingTabView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            startActivity(new Intent(ReservationStatementActivity.this, MainActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

    }

    private void saveIntentValues() {
        if (getIntent().hasExtra("LOGIN_ACCESS_TOKEN")) {
            loginAccessToken = getIntent().getStringExtra("LOGIN_ACCESS_TOKEN");
        }

        requestPosition = getIntent().getIntExtra("REQUEST_POSITION", 0);
    }

    private void settingActionBar() {
        setSupportActionBar(toolbarReservationStatement);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_home_solid);
    }

    private void settingTabView() {
        viewPager2ReservationStatement.setAdapter(makeFragmentAdapter());
        viewPager2ReservationStatement.setCurrentItem(requestPosition);

        new TabLayoutMediator(
                tabLayoutReservationStatement,
                viewPager2ReservationStatement,
                (tab, position) -> tab.setText(tabTitles[position])

        ).attach();
    }

    private ReservationStatementFragmentAdapter makeFragmentAdapter() {
        Bundle bundle = new Bundle();
        bundle.putString("LOGIN_ACCESS_TOKEN", loginAccessToken);

        ReservationStatement1Fragment reservationStatement1Fragment = new ReservationStatement1Fragment();
        reservationStatement1Fragment.setArguments(bundle);

        ReservationStatement2Fragment reservationStatement2Fragment = new ReservationStatement2Fragment();
        reservationStatement2Fragment.setArguments(bundle);

        ReservationStatementFragmentAdapter fragmentAdapter =
                new ReservationStatementFragmentAdapter(ReservationStatementActivity.this);

        fragmentAdapter.addFragment(reservationStatement1Fragment);
        fragmentAdapter.addFragment(reservationStatement2Fragment);

        return fragmentAdapter;
    }
}
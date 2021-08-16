package com.example.capstonemainproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.capstonemainproject.adapter.UserSettingFragmentAdapter;
import com.example.capstonemainproject.fragment.UserInfoSettingFragment;
import com.example.capstonemainproject.fragment.UserNotificationSettingFragment;
import com.example.capstonemainproject.fragment.UserPasswordSettingFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class UserSettingActivity extends AppCompatActivity {

    private static String[] tabTitles = {"사용자 정보", "비밀번호 변경", "알림 설정"};

    private Toolbar toolbarUserSetting;

    private TabLayout tabLayoutUserSetting;
    private ViewPager2 viewPager2UserSetting;

    private String loginAccessToken;
    private int requestPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);

        // 전달 데이터(로그인 토큰, 시작 위치) 저장
        saveIntentValues();

        // 화면 설정
        toolbarUserSetting = findViewById(R.id.toolbar_user_setting);
        tabLayoutUserSetting = findViewById(R.id.tabLayout_user_setting);
        viewPager2UserSetting = findViewById(R.id.viewPager2_user_setting);

        settingActionBar();
        settingTabView();
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
            startActivity(new Intent(com.example.capstonemainproject.UserSettingActivity.this, com.example.capstonemainproject.MainActivity.class));

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
        setSupportActionBar(toolbarUserSetting);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_left_solid);
    }

    private void settingTabView() {
        viewPager2UserSetting.setAdapter(makeFragmentAdapter());
        viewPager2UserSetting.setCurrentItem(requestPosition);

        new TabLayoutMediator(
                tabLayoutUserSetting,
                viewPager2UserSetting,
                (tab, position) -> tab.setText(tabTitles[position])

        ).attach();
    }

    private UserSettingFragmentAdapter makeFragmentAdapter() {
        Bundle bundle = new Bundle();
        bundle.putString("LOGIN_ACCESS_TOKEN", loginAccessToken);

        UserInfoSettingFragment userInfoSettingFragment = new UserInfoSettingFragment();
        userInfoSettingFragment.setArguments(bundle);

        UserPasswordSettingFragment userPasswordSettingFragment = new UserPasswordSettingFragment();
        userPasswordSettingFragment.setArguments(bundle);

        UserNotificationSettingFragment userNotificationSettingFragment = new UserNotificationSettingFragment();
        userNotificationSettingFragment.setArguments(bundle);

        UserSettingFragmentAdapter fragmentAdapter = new UserSettingFragmentAdapter(com.example.capstonemainproject.UserSettingActivity.this);
        fragmentAdapter.addFragment(userInfoSettingFragment);
        fragmentAdapter.addFragment(userPasswordSettingFragment);
        fragmentAdapter.addFragment(userNotificationSettingFragment);

        return fragmentAdapter;
    }
}
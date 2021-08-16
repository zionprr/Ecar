package com.example.capstonemainproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.example.capstonemainproject.adapter.HistoryAndBookmarkFragmentAdapter;
import com.example.capstonemainproject.fragment.BookmarkFragment;
import com.example.capstonemainproject.fragment.HistoryFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class HistoryAndBookmarkActivity extends AppCompatActivity {

    private static String[] tabTitles = {"최근 검색 목록", "즐겨찾기"};

    private Toolbar toolbarHistoryAndBookmark;

    private TabLayout tabLayoutHistoryAndBookmark;
    private ViewPager2 viewPager2HistoryAndBookmark;

    private String loginAccessToken;
    private int requestPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_and_bookmark);

        // 전달 데이터(로그인 토큰, 시작 위치) 저장
        saveIntentValues();

        // 화면 설정
        toolbarHistoryAndBookmark = findViewById(R.id.toolbar_history_and_bookmark);
        tabLayoutHistoryAndBookmark = findViewById(R.id.tabLayout_history_and_bookmark);
        viewPager2HistoryAndBookmark = findViewById(R.id.viewPager2_history_and_bookmark);

        // 상단바 및 탭화면
        settingActionBar();
        settingTabView();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            startActivity(new Intent(com.example.capstonemainproject.HistoryAndBookmarkActivity.this, MainActivity.class));

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
        setSupportActionBar(toolbarHistoryAndBookmark);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_home_solid);
    }

    private void settingTabView() {
        viewPager2HistoryAndBookmark.setAdapter(makeFragmentAdapter());
        viewPager2HistoryAndBookmark.setCurrentItem(requestPosition);

        new TabLayoutMediator(
                tabLayoutHistoryAndBookmark,
                viewPager2HistoryAndBookmark,
                (tab, position) -> tab.setText(tabTitles[position])

        ).attach();
    }

    private HistoryAndBookmarkFragmentAdapter makeFragmentAdapter() {
        Bundle bundle = new Bundle();
        bundle.putString("LOGIN_ACCESS_TOKEN", loginAccessToken);

        HistoryFragment historyFragment = new HistoryFragment();
        historyFragment.setArguments(bundle);

        BookmarkFragment bookmarkFragment = new BookmarkFragment();
        bookmarkFragment.setArguments(bundle);

        HistoryAndBookmarkFragmentAdapter fragmentAdapter = new HistoryAndBookmarkFragmentAdapter(com.example.capstonemainproject.HistoryAndBookmarkActivity.this);
        fragmentAdapter.addFragment(historyFragment);
        fragmentAdapter.addFragment(bookmarkFragment);

        return fragmentAdapter;
    }
}
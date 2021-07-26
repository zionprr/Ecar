package com.example.capstonemainproject;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.capstonemainproject.domain.Charger;
import com.example.capstonemainproject.dto.request.search.SearchConditionDto;
import com.example.capstonemainproject.dto.request.search.SearchLocationDto;
import com.example.capstonemainproject.dto.resoponse.common.CommonResponse;
import com.example.capstonemainproject.dto.resoponse.common.ListResultResponse;
import com.example.capstonemainproject.infra.app.GpsTracker;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.SearchService;
import com.example.capstonemainproject.service.UserBasicService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_CODE = 3000;
    private static final int USER_BASIC_SERVICE_GET_USER_INFO = -1;
    private static final int SEARCH_SERVICE_BY_TEXT = -15;
    private static final int SEARCH_SERVICE_BY_LOCATION = -16;

    private EditText eTextSearch;
    private ImageView iViewSearch, iViewSpeaker, iViewGps;
    private LinearLayout layoutRecentAndBookmark, layoutReservationList;
    private Spinner spinnerCpType, spinnerChargerType;

    private Toolbar toolBarMain;
    private DrawerLayout drawerLayoutMain;
    private NavigationView navigationMain;
    private TextView textNavName, textNavEmail, textNavCash, textNavCashPoint;

    private GoogleMap map;
    private SupportMapFragment mapFragment;

    private GpsTracker gpsTracker;
    private Location currentLocation;

    private UserBasicService userBasicService;
    private SearchService searchService;
    private int conditionCpType;
    private int conditionChargerType;

    // 필요 권한
    private final String[] requiredPermissions =
            {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

    // StartActivityForResult (STT)
    private final ActivityResultLauncher<Intent> startActivityResultForStt =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                            eTextSearch.setText(results.get(0));
                        }
                    }
            );

    // StartActivityForResult (위치 서비스 설정)
    private final ActivityResultLauncher<Intent> startActivityResultForLocationServiceSetting =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            checkRuntimePermissions();
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 화면 설정
        eTextSearch = findViewById(R.id.editText_search);
        iViewSearch = findViewById(R.id.imageView_search);
        iViewSpeaker = findViewById(R.id.imageView_stt_speaker);
        iViewGps = findViewById(R.id.imageView_gps);
        layoutRecentAndBookmark = findViewById(R.id.layout_recent_search_and_bookmark);
        layoutReservationList = findViewById(R.id.layout_reservationList);
        spinnerCpType = findViewById(R.id.spinner_cpType);
        spinnerChargerType = findViewById(R.id.spinner_chargerType);

        toolBarMain = findViewById(R.id.toolbar_main);
        drawerLayoutMain = findViewById(R.id.drawer_main);
        navigationMain = findViewById(R.id.nav_main);
        textNavName = findViewById(R.id.textView_nav_name);
        textNavEmail = findViewById(R.id.textView_nav_email);
        textNavCash = findViewById(R.id.textView_nav_cash);
        textNavCashPoint = findViewById(R.id.textView_nav_cash_point);

        // 로그인 토큰 저장 및 로그인 유저 정보 업데이트
        saveLoginToken();
        updateLoginUserInfo();

        // 네비게이션바 및 커스텀 화면 설정
        settingDrawer();
        settingCustomViews();

        // 화면 동작(1) : 음성 인식 (STT)
        iViewSpeaker.setOnClickListener(v -> new Thread(this::getVoice).start());

        // 화면 동작(2) : 전기차 충전소 검색(주소/충전소명)
        iViewSearch.setOnClickListener(v -> {
            String search = eTextSearch.getText().toString();

            if (search.isEmpty()) {
                String searchEmptyMsg = "검색어를 입력해 주세요.";

                SnackBarManager.showMessage(v, searchEmptyMsg);

            } else if (currentLocation == null) {
                String currentLocationNullMsg = "현재 위치 정보를 가져올 수 없습니다.";

                SnackBarManager.showMessage(v, currentLocationNullMsg);

            } else {
                // 검색 요청 객체 생성
                SearchConditionDto searchConditionDto = getSearchConditionDto(search);
                String loginAccessToken = PreferenceManager.getString(MainActivity.this, "LOGIN_ACCESS_TOKEN");

                // 검색 서비스 주입
                searchService = new SearchService(loginAccessToken, searchConditionDto);

                // 충전소 검색
                try {
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    intent.putExtra("Search", search);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

                    CommonResponse commonResponse = searchService.execute(SEARCH_SERVICE_BY_TEXT).get();

                    if (commonResponse.isSuccess()) {
                        ListResultResponse<Charger> listResultResponse = (ListResultResponse<Charger>) commonResponse;

                        intent.putExtra("SearchResults", (Serializable) listResultResponse.getDataList());

                    } else {
                        intent.putExtra("ErrorCode", commonResponse.getResponseCode());
                    }

                    startActivity(intent);

                } catch (ExecutionException | InterruptedException e) {
                    Log.w("Main", "Search failed.");
                }
            }
        });

        // 화면 동작(3) : 전기차 충전소 검색(현재 위치 주변)
        iViewGps.setOnClickListener(v -> {
            if (currentLocation == null) {
                String currentLocationNullMsg = "현재 위치 정보를 가져올 수 없습니다.";

                SnackBarManager.showMessage(v, currentLocationNullMsg);

            } else {
                // 검색 요청 객체 생성
                SearchLocationDto searchLocationDto = getSearchLocationDto();
                String loginAccessToken = PreferenceManager.getString(MainActivity.this, "LOGIN_ACCESS_TOKEN");

                // 검색 서비스 주입
                searchService = new SearchService(loginAccessToken, searchLocationDto);

                // 충전소 검색
                try {
                    Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

                    CommonResponse commonResponse = searchService.execute(SEARCH_SERVICE_BY_LOCATION).get();

                    if (commonResponse.isSuccess()) {
                        ListResultResponse<Charger> listResultResponse = (ListResultResponse<Charger>) commonResponse;

                        intent.putExtra("SearchResults", (Serializable) listResultResponse.getDataList());

                    } else {
                        intent.putExtra("ErrorCode", commonResponse.getResponseCode());
                    }

                    startActivity(intent);

                } catch (ExecutionException | InterruptedException e) {
                    Log.w("Main", "Search failed.");
                }
            }
        });

        // 화면 동작(4) : 최근 검색/즐겨찾기
        layoutRecentAndBookmark.setOnClickListener(v -> {

        });

        // 화면 동작(6) : 예약 목록
        layoutReservationList.setOnClickListener(v -> {

        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateLoginUserInfo();

        if (conditionCpType != 0 || conditionChargerType != 0) {
            settingCustomViews();
        }

        if (!eTextSearch.getText().toString().isEmpty()) {
            eTextSearch.setText("");
        }
    }

    // 지도 업로드 후 현재 위치 마커(Marker) 설정
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();

        } else {
            checkRuntimePermissions();
        }

        gpsTracker = new GpsTracker(MainActivity.this);
        currentLocation = gpsTracker.getLocation();

        if (currentLocation != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

            map.addMarker(new MarkerOptions().position(latLng).title("현위치"));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == requiredPermissions.length) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    View layoutMain = findViewById(R.id.layout_main);
                    String permissionSettingMsg = "위치 접근 권한이 거부되었습니다.\n애플리케이션을 다시 실행하거나 설정에서 권한을 허용해야 합니다.";

                    SnackBarManager.showMessage(layoutMain, permissionSettingMsg);
                    break;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
//            String userName = PreferenceManager.getString(MainActivity.this, "USER_NAME");
//            String userEmail = PreferenceManager.getString(MainActivity.this, "USER_EMAIL");
//            int userCash = PreferenceManager.getInt(MainActivity.this, "USER_CASH");
//            int userCashPoint = PreferenceManager.getInt(MainActivity.this, "USER_CASH_POINT");
//
//            textNavName.setText(userName);
//            textNavEmail.setText(userEmail);
//            textNavCash.setText(userCash + " 원");
//            textNavCashPoint.setText(userCashPoint + " 포인트");

            drawerLayoutMain.openDrawer(GravityCompat.START);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayoutMain.isDrawerOpen(GravityCompat.START)) {
            drawerLayoutMain.closeDrawers();
        }
    }

    private void saveLoginToken() {
        String loginAccessToken = getIntent().getStringExtra("LOGIN_ACCESS_TOKEN");

        PreferenceManager.setString(MainActivity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
    }

    private void updateLoginUserInfo() {
        String loginAccessToken = PreferenceManager.getString(MainActivity.this, "LOGIN_ACCESS_TOKEN");

        userBasicService = new UserBasicService(loginAccessToken, MainActivity.this);
        userBasicService.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, USER_BASIC_SERVICE_GET_USER_INFO);
    }

    private void settingDrawer() {
        setSupportActionBar(toolBarMain);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_bars_solid);

        navigationMain.setNavigationItemSelectedListener(item -> {
            item.setChecked(true);
            drawerLayoutMain.closeDrawers();

            switch (item.getItemId()) {
                case R.id.menu_user: {

                }
                case R.id.menu_account: {

                }
                case R.id.menu_car: {

                }
                case R.id.menu_bookmark: {

                }
                case R.id.menu_reservation: {

                }
                case R.id.menu_setting: {

                }
            }

            return true;
        });
    }

    private void settingCustomViews() {
        // 검색 조건(1) : 충전 방식
        conditionCpType = 0;

        ArrayAdapter<CharSequence> adapterCpType =
                ArrayAdapter.createFromResource(MainActivity.this, R.array.custom_array_cpType, android.R.layout.simple_spinner_item);

        adapterCpType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCpType.setAdapter(adapterCpType);
        spinnerCpType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                conditionCpType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 검색 조건(2) : 충전기 타입
        conditionChargerType = 0;

        ArrayAdapter<CharSequence> adapterChargerType =
                ArrayAdapter.createFromResource(MainActivity.this, R.array.custom_array_chargerType, android.R.layout.simple_spinner_item);


        adapterChargerType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerChargerType.setAdapter(adapterChargerType);
        spinnerChargerType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                conditionChargerType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // 구글맵
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);
    }

    // 위치 서비스 설정 확인
    private boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);

        alertDialogBuilder
                .setTitle("위치 서비스 비활성화")
                .setMessage("본 애플리케이션 이용을 위해 위치 서비스가 필요합니다.")
                .setCancelable(true)
                .setPositiveButton("설정", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);

                    startActivityResultForLocationServiceSetting.launch(intent);
                })
                .setNegativeButton("취소", (dialog, which) -> {
                    dialog.cancel();
                })
                .create()
                .show();
    }

    // 권한 확인
    private void checkRuntimePermissions() {
        int fineLocationPermission
                = ContextCompat.checkSelfPermission(MainActivity.this, requiredPermissions[0]);

        int coarseLocationPermission
                = ContextCompat.checkSelfPermission(MainActivity.this, requiredPermissions[1]);

        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED
                && coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, requiredPermissions[0])
                || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, requiredPermissions[1])) {

            View layoutMain = findViewById(R.id.layout_main);
            String permissionSettingMsg = "이 애플리케이션을 실행하려면 위치 접근 권한이 필요합니다.";

            SnackBarManager.showMessage(layoutMain, permissionSettingMsg);
        }

        ActivityCompat.requestPermissions(MainActivity.this, requiredPermissions, PERMISSIONS_REQUEST_CODE);
    }

    // STT
    private void getVoice() {
        Intent intent = new Intent();
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        intent.setAction(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        startActivityResultForStt.launch(intent);
    }

    private SearchConditionDto getSearchConditionDto(String search) {
        SearchConditionDto searchConditionDto = new SearchConditionDto();
        searchConditionDto.setSearch(search);
        searchConditionDto.setLatitude(currentLocation.getLatitude());
        searchConditionDto.setLongitude(currentLocation.getLongitude());

        if (conditionCpType != 0) {
            searchConditionDto.setCpTp(conditionCpType);
        }

        if (conditionChargerType != 0) {
            searchConditionDto.setChargerTp(conditionChargerType);
        }

        return searchConditionDto;
    }

    private SearchLocationDto getSearchLocationDto() {
        SearchLocationDto searchLocationDto = new SearchLocationDto();
        searchLocationDto.setLatitude(currentLocation.getLatitude());
        searchLocationDto.setLongitude(currentLocation.getLongitude());

        if (conditionCpType != 0) {
            searchLocationDto.setCpTp(conditionCpType);
        }

        if (conditionChargerType != 0) {
            searchLocationDto.setChargerTp(conditionChargerType);
        }

        return searchLocationDto;
    }
}
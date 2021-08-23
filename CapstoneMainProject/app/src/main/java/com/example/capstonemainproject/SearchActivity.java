package com.example.capstonemainproject;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.capstonemainproject.domain.Charger;
import com.example.capstonemainproject.domain.Station;
import com.example.capstonemainproject.dto.request.search.SearchConditionDto;
import com.example.capstonemainproject.dto.request.search.SearchLocationDto;
import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.ListResultResponse;
import com.example.capstonemainproject.infra.app.BitmapConverter;
import com.example.capstonemainproject.infra.app.GpsTracker;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.SearchService;
import com.example.capstonemainproject.service.UserBasicService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.navigation.NavigationView;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import lombok.Getter;

@RequiresApi(api = Build.VERSION_CODES.O)
public class SearchActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 3000;
    private static final int CHARGER_STATE_GREEN = 1;
    private static final int CHARGER_STATE_YELLOW = 2;
    private static final int USER_BASIC_SERVICE_GET_USER_INFO = -1;
    private static final int SEARCH_SERVICE_BY_TEXT = -15;
    private static final int SEARCH_SERVICE_BY_LOCATION = -16;
    private static final int SEARCH_SERVICE_ERROR_NOT_FOUND = -3000;
    private static final int SEARCH_SERVICE_ERROR_ADDRESS_EXCEEDED = -3005;

    private Toolbar toolBarSearch;
    private DrawerLayout drawerLayoutSearch;
    private NavigationView navigationSearch;

    private EditText eTextSearch;
    private ImageView iViewSearch, iViewSpeaker, iViewGps;
    private TextView textSearchError;
    private ListView listViewCharger;

    private Spinner spinnerCpType, spinnerChargerType;
    private int conditionCpType, conditionChargerType;

    private GoogleMap map;
    private SupportMapFragment mapFragment;

    private GpsTracker gpsTracker;
    private Location currentLocation;

    private String search;
    private List<Charger> searchResults;

    private UserBasicService userBasicService;
    private SearchService searchService;

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
        setContentView(R.layout.activity_search);

        // 로그인 토큰 저장
        saveIntentValues();

        // 화면 설정
        toolBarSearch = findViewById(R.id.toolbar_search);
        drawerLayoutSearch = findViewById(R.id.drawer_search);
        navigationSearch = findViewById(R.id.nav_search);
        eTextSearch = findViewById(R.id.editText_search2);
        iViewSearch = findViewById(R.id.imageView_search2);
        iViewSpeaker = findViewById(R.id.imageView_stt_speaker2);
        iViewGps = findViewById(R.id.imageView_gps2);
        textSearchError = findViewById(R.id.textView_search_error);
        listViewCharger = findViewById(R.id.listView_charger);
        spinnerCpType = findViewById(R.id.spinner_cpType2);
        spinnerChargerType = findViewById(R.id.spinner_chargerType2);

        // 네비게이션바 및 커스텀 화면 설정
        settingDrawer();
        settingCustomViews();

        // 결과 화면 출력
        showSearchResults();

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
                String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.SearchActivity.this, "LOGIN_ACCESS_TOKEN");

                // 검색 서비스 주입
                searchService = new SearchService(loginAccessToken, searchConditionDto);

                // 충전소 검색
                try {
                    Intent intent = new Intent(com.example.capstonemainproject.SearchActivity.this, com.example.capstonemainproject.SearchActivity.class);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                    intent.putExtra("Search", search);
                    intent.putExtra("ConditionCpType", conditionCpType);
                    intent.putExtra("ConditionChargerType", conditionChargerType);

                    CommonResponse commonResponse = searchService.execute(SEARCH_SERVICE_BY_TEXT).get();

                    if (commonResponse.isSuccess()) {
                        ListResultResponse<Charger> listResultResponse = (ListResultResponse<Charger>) commonResponse;

                        intent.putExtra("SearchResults", (Serializable) listResultResponse.getDataList());

                    } else {
                        intent.putExtra("ErrorCode", commonResponse.getResponseCode());
                    }

                    finish();
                    startActivity(intent);

                } catch (ExecutionException | InterruptedException e) {
                    Log.w("Search", "Search failed.");
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
                String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.SearchActivity.this, "LOGIN_ACCESS_TOKEN");

                // 검색 서비스 주입
                searchService = new SearchService(loginAccessToken, searchLocationDto);

                // 충전소 검색
                try {
                    Intent intent = new Intent(com.example.capstonemainproject.SearchActivity.this, com.example.capstonemainproject.SearchActivity.class);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                    intent.putExtra("ConditionCpType", conditionCpType);
                    intent.putExtra("ConditionChargerType", conditionChargerType);

                    CommonResponse commonResponse = searchService.execute(SEARCH_SERVICE_BY_LOCATION).get();

                    if (commonResponse.isSuccess()) {
                        ListResultResponse<Charger> listResultResponse = (ListResultResponse<Charger>) commonResponse;

                        intent.putExtra("SearchResults", (Serializable) listResultResponse.getDataList());

                    } else {
                        intent.putExtra("ErrorCode", commonResponse.getResponseCode());
                    }

                    finish();
                    startActivity(intent);

                } catch (ExecutionException | InterruptedException e) {
                    Log.w("Search", "Search failed.");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLoginUserInfo();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();

        } else {
            checkRuntimePermissions();
        }

        gpsTracker = new GpsTracker(com.example.capstonemainproject.SearchActivity.this);
        currentLocation = gpsTracker.getLocation();

        if (currentLocation != null) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

            if (getIntent().hasExtra("ErrorCode")) {
                map.addMarker(new MarkerOptions().position(latLng).title("현위치"));
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
        }

        // 검색 결과 목록 마커
        if (searchResults != null && searchResults.size() > 0) {
            Station firstStation = searchResults.get(0).getStation();
            LatLng defaultLatLng = new LatLng(firstStation.getLatitude(), firstStation.getLongitude());

            searchResults.stream()
                    .collect(Collectors.groupingBy(Charger::getStation))
                    .forEach((station, chargers) -> {
                        int[] chargerStates = new int[6];

                        chargers.forEach(charger -> chargerStates[charger.getState()]++);

                        Bitmap bitmap;

                        if (chargerStates[CHARGER_STATE_GREEN] > 0) {
                            bitmap = BitmapConverter.getBitmapFromVectorDrawable(this, R.drawable.ic_map_marker_solid_green);

                        } else if (chargerStates[CHARGER_STATE_YELLOW] > 0) {
                            bitmap = BitmapConverter.getBitmapFromVectorDrawable(this, R.drawable.ic_map_marker_solid_yellow);

                        } else {
                            bitmap = BitmapConverter.getBitmapFromVectorDrawable(this, R.drawable.ic_map_marker_solid_red);
                        }

                        MarkerOptions markerOptions =
                                new MarkerOptions()
                                        .position(new LatLng(station.getLatitude(), station.getLongitude()))
                                        .icon(BitmapDescriptorFactory.fromBitmap(bitmap));

                        map.addMarker(markerOptions).setTag(new MarkerTag(station, chargers));
                    });

            map.setInfoWindowAdapter(new CustomMarkerWindows());
            map.setOnInfoWindowClickListener(this);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(defaultLatLng, 15));
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        MarkerTag markerTag = (MarkerTag) marker.getTag();

        showDialogForMarkerDetails(markerTag);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_CODE && grantResults.length == requiredPermissions.length) {
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    String permissionSettingMsg = "위치 접근 권한이 거부되었습니다.\n애플리케이션을 다시 실행하거나 설정에서 권한을 허용해야 합니다.";

                    SnackBarManager.showMessage(findViewById(R.id.layout_search), permissionSettingMsg);
                    break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            TextView textNavName = findViewById(R.id.textView_nav_name);
            TextView textNavEmail = findViewById(R.id.textView_nav_email);
            TextView textNavCash = findViewById(R.id.textView_nav_cash);
            TextView textNavCashPoint = findViewById(R.id.textView_nav_cash_point);
            Button btnCash = findViewById(R.id.btn_nav_cash);

            String userName = PreferenceManager.getString(com.example.capstonemainproject.SearchActivity.this, "USER_NAME");
            String userEmail = PreferenceManager.getString(com.example.capstonemainproject.SearchActivity.this, "USER_EMAIL");
            int userCash = PreferenceManager.getInt(com.example.capstonemainproject.SearchActivity.this, "USER_CASH");
            int userCashPoint = PreferenceManager.getInt(com.example.capstonemainproject.SearchActivity.this, "USER_CASH_POINT");

            textNavName.setText(userName);
            textNavEmail.setText(userEmail);
            textNavCash.setText(String.valueOf(userCash));
            textNavCashPoint.setText(String.valueOf(userCashPoint));

            btnCash.setOnClickListener(v -> {
                String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.SearchActivity.this, "LOGIN_ACCESS_TOKEN");

                Intent intent = new Intent(com.example.capstonemainproject.SearchActivity.this, com.example.capstonemainproject.CashActivity.class);
                intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

                startActivity(intent);
            });

            drawerLayoutSearch.openDrawer(GravityCompat.START);

            return true;

        } else if (item.getItemId() == R.id.action_home) {
            finish();
            startActivity(new Intent(com.example.capstonemainproject.SearchActivity.this, MainActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayoutSearch.isDrawerOpen(GravityCompat.START)) {
            drawerLayoutSearch.closeDrawers();
        }
    }

    private void saveIntentValues() {
        Intent currentIntent = getIntent();

        if (currentIntent.hasExtra("LOGIN_ACCESS_TOKEN")) {
            String loginAccessToken = currentIntent.getStringExtra("LOGIN_ACCESS_TOKEN");

            PreferenceManager.setString(com.example.capstonemainproject.SearchActivity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
        }

        if (currentIntent.hasExtra("SearchResults")) {
            searchResults = (List<Charger>) currentIntent.getSerializableExtra("SearchResults");
        }

        search = currentIntent.getStringExtra("Search");
        conditionCpType = currentIntent.getIntExtra("ConditionCpType", 0);
        conditionChargerType = currentIntent.getIntExtra("ConditionChargerType", 0);
    }

    private void updateLoginUserInfo() {
        String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.SearchActivity.this, "LOGIN_ACCESS_TOKEN");

        userBasicService = new UserBasicService(loginAccessToken, com.example.capstonemainproject.SearchActivity.this);
        userBasicService.execute(USER_BASIC_SERVICE_GET_USER_INFO);
    }

    private void settingDrawer() {
        setSupportActionBar(toolBarSearch);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_bars_solid);

        navigationSearch.setNavigationItemSelectedListener(item -> {
            drawerLayoutSearch.closeDrawers();

            Intent intent;
            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.SearchActivity.this, "LOGIN_ACCESS_TOKEN");

            switch (item.getItemId()) {
                case R.id.menu_user: {
                    intent = new Intent(com.example.capstonemainproject.SearchActivity.this, com.example.capstonemainproject.UserActivity.class);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

                    startActivity(intent);
                    break;
                }
                case R.id.menu_reservation: {

                }
                case R.id.menu_bookmark: {
                    intent = new Intent(com.example.capstonemainproject.SearchActivity.this, HistoryAndBookmarkActivity.class);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                    intent.putExtra("REQUEST_POSITION", 1);

                    startActivity(intent);
                    break;
                }
                case R.id.menu_car: {
                    intent = new Intent(com.example.capstonemainproject.SearchActivity.this, CarActivity.class);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

                    startActivity(intent);
                    break;
                }
                case R.id.menu_account: {
                    intent = new Intent(com.example.capstonemainproject.SearchActivity.this, com.example.capstonemainproject.BankActivity.class);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

                    startActivity(intent);
                    break;
                }
                case R.id.menu_notification: {
                    intent = new Intent(com.example.capstonemainproject.SearchActivity.this, com.example.capstonemainproject.UserSettingActivity.class);
                    intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
                    intent.putExtra("REQUEST_POSITION", 2);

                    startActivity(intent);
                }
            }

            return true;
        });
    }

    private void settingCustomViews() {
        // 검색 조건(1) : 충전 방식
        ArrayAdapter<CharSequence> adapterCpType =
                ArrayAdapter.createFromResource(com.example.capstonemainproject.SearchActivity.this, R.array.custom_array_cpType, android.R.layout.simple_spinner_item);

        adapterCpType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerCpType.setAdapter(adapterCpType);
        spinnerCpType.setFocusable(true);
        spinnerCpType.setSelection(conditionCpType);
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
        ArrayAdapter<CharSequence> adapterChargerType =
                ArrayAdapter.createFromResource(com.example.capstonemainproject.SearchActivity.this, R.array.custom_array_chargerType, android.R.layout.simple_spinner_item);

        adapterChargerType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerChargerType.setAdapter(adapterChargerType);
        spinnerChargerType.setFocusable(true);
        spinnerChargerType.setSelection(conditionChargerType);
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
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map2);
        mapFragment.getMapAsync(this);
    }

    private void showSearchResults() {
        eTextSearch.setText(search);

        if (getIntent().hasExtra("ErrorCode") || searchResults == null || searchResults.size() == 0) {
            String searchErrorMsg = "";
            int errorCode = getIntent().getIntExtra("ErrorCode", SEARCH_SERVICE_ERROR_NOT_FOUND);

            if (errorCode == SEARCH_SERVICE_ERROR_NOT_FOUND) {
                searchErrorMsg = "검색 결과가 없습니다.";

            } else if (errorCode == SEARCH_SERVICE_ERROR_ADDRESS_EXCEEDED) {
                searchErrorMsg = "주소를 상세히 입력해 주세요.";
            }

            listViewCharger.setVisibility(View.GONE);
            textSearchError.setVisibility(View.VISIBLE);

            textSearchError.setText(searchErrorMsg);

        } else {
            textSearchError.setVisibility(View.GONE);
            listViewCharger.setVisibility(View.VISIBLE);

            listViewCharger.setAdapter(new CustomChargerList(this, searchResults));
        }
    }

    // 위치 서비스 설정 확인
    private boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(com.example.capstonemainproject.SearchActivity.this);

        alertDialogBuilder
                .setTitle("위치 서비스 비활성화")
                .setMessage("본 애플리케이션 이용을 위해 위치 서비스가 필요합니다.")
                .setCancelable(true)
                .setPositiveButton("설정", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_LOCALE_SETTINGS);

                    startActivityResultForLocationServiceSetting.launch(intent);
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.cancel())
                .create()
                .show();
    }

    // 권한 확인
    private void checkRuntimePermissions() {
        int fineLocationPermission
                = ContextCompat.checkSelfPermission(com.example.capstonemainproject.SearchActivity.this, requiredPermissions[0]);

        int coarseLocationPermission
                = ContextCompat.checkSelfPermission(com.example.capstonemainproject.SearchActivity.this, requiredPermissions[1]);

        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED
                && coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {

            return;
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(com.example.capstonemainproject.SearchActivity.this, requiredPermissions[0])
                || ActivityCompat.shouldShowRequestPermissionRationale(com.example.capstonemainproject.SearchActivity.this, requiredPermissions[1])) {

            String permissionSettingMsg = "이 애플리케이션을 실행하려면 위치 접근 권한이 필요합니다.";

            SnackBarManager.showMessage(findViewById(R.id.layout_search), permissionSettingMsg);
        }

        ActivityCompat.requestPermissions(com.example.capstonemainproject.SearchActivity.this, requiredPermissions, PERMISSIONS_REQUEST_CODE);
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

    private void showDialogForMarkerDetails(MarkerTag markerTag) {
        Dialog dialog = new Dialog(com.example.capstonemainproject.SearchActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_marker_details);

        dialog.show();

        TextView markerTitle = dialog.findViewById(R.id.textView_marker_details_title);
        TextView markerAddress = dialog.findViewById(R.id.textView_marker_details_address);
        ListView chargers = dialog.findViewById(R.id.listView_marker_details);
        ImageView close = dialog.findViewById(R.id.imageView_marker_details_close);

        markerTitle.setText(markerTag.getStation().getStationName());
        markerAddress.setText(markerTag.getStation().getStationAddress());
        chargers.setAdapter(new CustomMarkerChargerList(this, dialog, markerTag.getChargerList()));

        close.setOnClickListener(v -> dialog.dismiss());
    }

    private void showDialogForChargerDetails(Charger charger) {
        Dialog dialog = new Dialog(com.example.capstonemainproject.SearchActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_charger_details);

        dialog.show();

        TextView stationName = dialog.findViewById(R.id.textView_charger_details_title);
        TextView stationAddress = dialog.findViewById(R.id.textView_charger_details_address);
        TextView chargerName = dialog.findViewById(R.id.textView_charger_details_name);
        TextView chargerMode = dialog.findViewById(R.id.textView_charger_details_mode);
        TextView chargerState = dialog.findViewById(R.id.textView_charger_details_state);
        TextView updatedAt = dialog.findViewById(R.id.textView_charger_details_updatedAt);
        ImageView close = dialog.findViewById(R.id.imageView_charger_details_close);
        Button btnReservation = dialog.findViewById(R.id.btn_charger_details_reservation);
        Button btnMore = dialog.findViewById(R.id.btn_charger_details_more);

        stationName.setText(charger.getStation().getStationName());
        stationAddress.setText(charger.getStation().getStationAddress());
        chargerName.setText(charger.getChargerName());
        chargerMode.setText(charger.stringValueOfMode());
        chargerState.setText(charger.stringValueOfState());
        updatedAt.setText(charger.getStateUpdatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        close.setOnClickListener(v -> dialog.dismiss());

        // 예약하기
        btnReservation.setOnClickListener(v -> {
            dialog.dismiss();

            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.SearchActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(com.example.capstonemainproject.SearchActivity.this, com.example.capstonemainproject.ReservationActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
            intent.putExtra("ChargerId", charger.getId());

            startActivity(intent);
        });

        // 더보기
        btnMore.setOnClickListener(v -> {
            dialog.dismiss();

            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.SearchActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(com.example.capstonemainproject.SearchActivity.this, ChargerActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
            intent.putExtra("ChargerId", charger.getId());
            intent.putExtra("Record", true);

            startActivity(intent);
        });
    }

    @Getter
    private class MarkerTag {

        private final Station station;
        private final List<Charger> chargerList;

        public MarkerTag(Station station, List<Charger> chargerList) {
            this.station = station;
            this.chargerList = chargerList;
        }
    }

    private class CustomMarkerWindows implements GoogleMap.InfoWindowAdapter {

        private final View currentWindow;
        private final TextView markerTitle, markerAddress;

        public CustomMarkerWindows() {
            this.currentWindow = getLayoutInflater().inflate(R.layout.marker_windows, null);
            this.markerTitle = currentWindow.findViewById(R.id.textView_marker_title);
            this.markerAddress = currentWindow.findViewById(R.id.textView_marker_address);
        }

        @Override
        public View getInfoWindow(Marker marker) {
            MarkerTag markerTag = (MarkerTag) marker.getTag();
            Station station = markerTag.getStation();

            markerTitle.setText(station.getStationName());
            markerAddress.setText(station.getStationAddress());

            return currentWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }

    private class CustomMarkerChargerList extends ArrayAdapter<Charger> {

        private final Activity context;
        private final Dialog dialog;
        private final List<Charger> chargerList;

        public CustomMarkerChargerList(Activity context, Dialog dialog, List<Charger> chargerList) {
            super(context, R.layout.listview_marker, chargerList);
            this.context = context;
            this.dialog = dialog;
            this.chargerList = chargerList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listview_marker, null, true);

            TextView chargerName = rowView.findViewById(R.id.listView_marker_charger_name);
            TextView chargerMode = rowView.findViewById(R.id.listView_marker_charger_mode);
            ImageView chargerStateGreen = rowView.findViewById(R.id.listView_marker_charger_state_green);
            ImageView chargerStateYellow = rowView.findViewById(R.id.listView_marker_charger_state_yellow);
            ImageView chargerStateRed = rowView.findViewById(R.id.listView_marker_charger_state_red);

            Charger charger = chargerList.get(position);

            chargerName.setText(charger.getChargerName());
            chargerMode.setText(charger.stringValueOfMode());

            if (charger.getState() == CHARGER_STATE_GREEN) {
                chargerStateYellow.setVisibility(View.GONE);
                chargerStateRed.setVisibility(View.GONE);
                chargerStateGreen.setVisibility(View.VISIBLE);

            } else if (charger.getState() == CHARGER_STATE_YELLOW) {
                chargerStateGreen.setVisibility(View.GONE);
                chargerStateRed.setVisibility(View.GONE);
                chargerStateYellow.setVisibility(View.VISIBLE);

            } else {
                chargerStateGreen.setVisibility(View.GONE);
                chargerStateYellow.setVisibility(View.GONE);
                chargerStateRed.setVisibility(View.VISIBLE);
            }

            rowView.setOnClickListener(v -> {
                dialog.dismiss();

                showDialogForChargerDetails(charger);
            });

            return rowView;
        }
    }

    private class CustomChargerList extends ArrayAdapter<Charger> {

        private final Activity context;
        private final List<Charger> searchChargerList;

        public CustomChargerList(Activity context, List<Charger> searchChargerList) {
            super(context, R.layout.listview_charger, searchChargerList);
            this.context = context;
            this.searchChargerList = searchChargerList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listview_charger, null, true);

            TextView stationName = rowView.findViewById(R.id.listView_station_name);
            TextView stationAddress = rowView.findViewById(R.id.listView_station_address);
            TextView chargerName = rowView.findViewById(R.id.listView_charger_name);
            TextView chargerMode = rowView.findViewById(R.id.listView_charger_mode);
            ImageView chargerStateGreen = rowView.findViewById(R.id.listView_charger_state_green);
            ImageView chargerStateYellow = rowView.findViewById(R.id.listView_charger_state_yellow);
            ImageView chargerStateRed = rowView.findViewById(R.id.listView_charger_state_red);

            Charger charger = searchChargerList.get(position);

            stationName.setText(charger.getStation().getStationName());
            stationAddress.setText(charger.getStation().getStationAddress());
            chargerName.setText(charger.getChargerName());
            chargerMode.setText(charger.stringValueOfMode());

            if (charger.getState() == CHARGER_STATE_GREEN) {
                chargerStateYellow.setVisibility(View.GONE);
                chargerStateRed.setVisibility(View.GONE);
                chargerStateGreen.setVisibility(View.VISIBLE);

            } else if (charger.getState() == CHARGER_STATE_YELLOW) {
                chargerStateGreen.setVisibility(View.GONE);
                chargerStateRed.setVisibility(View.GONE);
                chargerStateYellow.setVisibility(View.VISIBLE);

            } else {
                chargerStateGreen.setVisibility(View.GONE);
                chargerStateYellow.setVisibility(View.GONE);
                chargerStateRed.setVisibility(View.VISIBLE);
            }

            rowView.setOnClickListener(v -> showDialogForChargerDetails(charger));

            return rowView;
        }
    }
}
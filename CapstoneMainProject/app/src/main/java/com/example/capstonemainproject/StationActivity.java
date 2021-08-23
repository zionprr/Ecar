package com.example.capstonemainproject;

import android.app.Activity;
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
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.capstonemainproject.domain.Charger;
import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.SingleResultResponse;
import com.example.capstonemainproject.dto.response.custom.search.StationInfoDto;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.StationService;
import com.example.capstonemainproject.service.UserMainService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RequiresApi(api = Build.VERSION_CODES.O)
public class StationActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int CHARGER_STATE_GREEN = 1;
    private static final int CHARGER_STATE_YELLOW = 2;
    private static final long STATION_SERVICE_GET_INFO = -17;
    private static final long STATION_SERVICE_GET_INFO_RECORD = -18;
    private static final long USER_MAIN_SERVICE_REGISTER_BOOKMARK = -23;
    private static final long USER_MAIN_SERVICE_DELETE_BOOKMARK = -24;

    private Toolbar toolbarStation;

    private TextView textStationName, textStationAddress;
    private CheckBox checkBoxBookmarked;
    private ListView listViewStationChargers;

    private GoogleMap map;
    private SupportMapFragment mapFragment;

    private StationService stationService;
    private UserMainService userMainService;

    private StationInfoDto stationInfoDto;
    private boolean isRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);

        // 인텐트 정보 저장
        saveIntentValues();

        // 화면 설정
        toolbarStation = findViewById(R.id.toolbar_station);
        textStationName = findViewById(R.id.textView_station_name);
        textStationAddress = findViewById(R.id.textView_station_address);
        checkBoxBookmarked = findViewById(R.id.checkbox_station_bookmarked);
        listViewStationChargers = findViewById(R.id.listView_station_chargers);

        // 상단바
        settingActionBar();

        // 구글 맵
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map3);
        mapFragment.getMapAsync(this);

        // 화면 동작 : 즐겨찾기
        checkBoxBookmarked.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.StationActivity.this, "LOGIN_ACCESS_TOKEN");

            userMainService = new UserMainService(loginAccessToken);

            if (checkBoxBookmarked.isChecked()) {       // 즐겨찾기 등록
                userMainService.execute(USER_MAIN_SERVICE_REGISTER_BOOKMARK, stationInfoDto.getStationId());

            } else {                                    // 즐겨찾기 해제
                userMainService.execute(USER_MAIN_SERVICE_DELETE_BOOKMARK, stationInfoDto.getStationId());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStationInfo();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        if (stationInfoDto != null) {
            LatLng latLng = new LatLng(stationInfoDto.getLatitude(), stationInfoDto.getLongitude());

            map.addMarker(new MarkerOptions().position(latLng).title(stationInfoDto.getStationName()));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
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
            finish();

            return true;

        } else if (item.getItemId() == R.id.action_home) {
            finish();
            startActivity(new Intent(com.example.capstonemainproject.StationActivity.this, com.example.capstonemainproject.MainActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

    }

    private void saveIntentValues() {
        Intent currentIntent = getIntent();

        if (currentIntent.hasExtra("LOGIN_ACCESS_TOKEN")) {
            String loginAccessToken = currentIntent.getStringExtra("LOGIN_ACCESS_TOKEN");

            PreferenceManager.setString(com.example.capstonemainproject.StationActivity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
        }

        if (currentIntent.hasExtra("StationId")) {
            long chargerId = currentIntent.getLongExtra("StationId", -1);

            PreferenceManager.setLong(com.example.capstonemainproject.StationActivity.this, "STATION_ID", chargerId);
        }

        isRecord = currentIntent.getBooleanExtra("Record", false);
    }

    private void settingActionBar() {
        setSupportActionBar(toolbarStation);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_left_solid);
    }

    private void loadStationInfo() {
        String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.StationActivity.this, "LOGIN_ACCESS_TOKEN");
        long stationId = PreferenceManager.getLong(com.example.capstonemainproject.StationActivity.this, "STATION_ID");

        stationService = new StationService(loginAccessToken);

        try {
            CommonResponse commonResponse;

            if (!isRecord) {
                commonResponse = stationService.execute(STATION_SERVICE_GET_INFO, stationId).get();

            } else {
                commonResponse = stationService.execute(STATION_SERVICE_GET_INFO_RECORD, stationId).get();
            }

            if (commonResponse.isSuccess()) {
                SingleResultResponse<StationInfoDto> singleResultResponse = (SingleResultResponse<StationInfoDto>) commonResponse;

                stationInfoDto = singleResultResponse.getData();

                textStationName.setText(stationInfoDto.getStationName());
                textStationAddress.setText(stationInfoDto.getStationAddress());
                checkBoxBookmarked.setChecked(stationInfoDto.isBookmarked());
                listViewStationChargers.setAdapter(new CustomChargerList(this, new ArrayList<>(stationInfoDto.getChargers())));

            } else {
                String loadStationInfoFailedMsg = "충전소 정보를 불러올 수 없습니다.";

                SnackBarManager.showMessage(findViewById(R.id.layout_station), loadStationInfoFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("Station", "Loading station info failed.");
        }
    }

    private void showDialogForChargerDetails(Charger charger) {
        Dialog dialog = new Dialog(com.example.capstonemainproject.StationActivity.this);
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

            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.StationActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(com.example.capstonemainproject.StationActivity.this, com.example.capstonemainproject.ReservationActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
            intent.putExtra("ChargerId", charger.getId());

            startActivity(intent);
        });

        // 더보기
        btnMore.setOnClickListener(v -> {
            dialog.dismiss();

            String loginAccessToken = PreferenceManager.getString(com.example.capstonemainproject.StationActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(com.example.capstonemainproject.StationActivity.this, com.example.capstonemainproject.ChargerActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);
            intent.putExtra("ChargerId", charger.getId());
            intent.putExtra("Record", isRecord);

            startActivity(intent);
        });
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
package com.example.capstonemainproject;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import noman.googleplaces.Place;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, PlacesListener {

    private EditText eTextSearch;
    private ImageView iViewSearch, iViewSpeaker;

    private Spinner spinner_searchType, spinner_customSearch_chargeSpeed, spinner_customSearch_chargeType;

    private GoogleMap mMap;
    LocationManager manager;
    List<Marker> previous_marker = null;
    private Marker currentMarker = null;


    private ActivityResultLauncher<Intent> startActivityResult =
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // 화면 설정
        eTextSearch = findViewById(R.id.editText_search);
        iViewSearch = findViewById(R.id.ImageView_search);
        iViewSpeaker = findViewById(R.id.ImageView_TTS_speaker);

        spinner_searchType = findViewById(R.id.spinner_searchType);
        spinner_customSearch_chargeSpeed = findViewById(R.id.spinner_customSearch_chargeSpeed);
        spinner_customSearch_chargeType = findViewById(R.id.spinner_customSearch_chargeType);


        // GPS 요청 설정
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // GPS 프래그먼트
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);
        mapFragment.getMapAsync(this);


        // 음성 인식(STT)
        iViewSpeaker.setOnClickListener(v ->
                new Thread(() -> {
                    try {
                        getVoice();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start()
        );


        // 검색 타입 설정(지역/충전소명)
        ArrayAdapter<CharSequence> adapter_searchType = ArrayAdapter.createFromResource(this,
                R.array.array_searchType, android.R.layout.simple_spinner_item);
        adapter_searchType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_searchType.setAdapter(adapter_searchType);  // 어탭터에 연결

        spinner_searchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        // 맞춤 전기차 충전소 검색 (지역/충전소명)
        // 충전 스피드 (급속/완속)
        ArrayAdapter<CharSequence> adapter_customSearch_speed = ArrayAdapter.createFromResource(this,
                R.array.array_customSearch_speed, android.R.layout.simple_spinner_item);
        adapter_customSearch_speed.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_customSearch_chargeSpeed.setAdapter(adapter_customSearch_speed);  // 어탭터에 연결

        spinner_customSearch_chargeSpeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        //충전 타입(DC콤보/AC3상)
        ArrayAdapter<CharSequence> adapter_customSearch_type = ArrayAdapter.createFromResource(this,
                R.array.array_customSearch_type, android.R.layout.simple_spinner_item);
        adapter_customSearch_type.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_customSearch_chargeType.setAdapter(adapter_customSearch_type);  // 어탭터에 연결

        spinner_customSearch_chargeType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });



        // 전기차 충전소 검색 (지역/충전소명)
        iViewSearch.setOnClickListener(v -> {

        });

        //
    }

    @Override
    public void onBackPressed() {
    }

    // stt 음성인식 변환
    private void getVoice() {
        Intent intent = new Intent();
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        intent.setAction(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        startActivityResult.launch(intent);
    }


    // GPS 현재위치 구현
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //위치권한 체크
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //리스너 설정
        manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, mlocationListener);
        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocationListener);
    }


    LocationListener mlocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();


            //권한체크
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            MarkerOptions markerOptions = new MarkerOptions();

            markerOptions.position(currentLatLng);
            markerOptions.title("충전소명");
            markerOptions.snippet("충전소 위치");
            markerOptions.draggable(true);


            //현재위치 파란 원 표시
            mMap.setMyLocationEnabled(true);

            //현재위치 마커 표시
            mMap.addMarker(markerOptions);

            //현재위치 줌
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    public void onPlacesFailure(PlacesException e) {

    }

    @Override
    public void onPlacesStart() {

    }

    @Override
    public void onPlacesSuccess(List<Place> places) {

    }

    @Override
    public void onPlacesFinished() {

    }


    // GPS화면 클릭 리스너
    GoogleMap.OnInfoWindowClickListener infoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            //String markerId = marker.getId();
            //Toast.makeText(MainActivity.this, "정보창 클릭 Marker ID : ", Toast.LENGTH_SHORT).show();
            MyDialog();

        }
    };

    // 해당 충전소정보 팝업창(GPS마커 정보창 클릭시)
    private void MyDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog alert;

        builder.setTitle("부산광역시")
                .setMessage("충전소 타입: 급속\n충전소 상태 코드: 고장/점검\n충전 방식: DC차데모+DC콤보+AC3상\n충전기 상태 갱신 시각: 2021-06-08-22:38:03")
                .setPositiveButton("예약하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Toast.makeText(MainActivity.this, "예약하기", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, ReservationActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("길찾기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "길찾기", Toast.LENGTH_SHORT).show();
                    }
                });
        alert = builder.create();
        alert.show();
    }

}
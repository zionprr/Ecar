package com.example.capstonemainproject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
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
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.example.capstonemainproject.domain.Car;
import com.example.capstonemainproject.dto.response.common.CommonResponse;
import com.example.capstonemainproject.dto.response.common.ListResultResponse;
import com.example.capstonemainproject.infra.app.PreferenceManager;
import com.example.capstonemainproject.infra.app.SnackBarManager;
import com.example.capstonemainproject.service.CarService;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class CarActivity extends AppCompatActivity {

    private static final int CAR_REGISTRATION_ACTIVITY_RESULT_OK = 100;
    private static final int CAR_SERVICE_GET_USER_CARS = -5;
    private static final int CAR_SERVICE_DELETE_USER_CAR = -7;

    private Toolbar toolbarCar;

    private ListView listViewCar;
    private TextView textCarNotFound;
    private ImageView iViewNewCar;

    private CarService carService;

    private final ActivityResultLauncher<Intent> startActivityResultForCar =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == CAR_REGISTRATION_ACTIVITY_RESULT_OK) {
                            finish();
                            startActivity(new Intent(CarActivity.this, CarActivity.class));
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car);

        // ????????? ?????? ??????
        saveLoginToken();

        // ?????? ??????
        toolbarCar = findViewById(R.id.toolbar_car);
        listViewCar = findViewById(R.id.listView_car);
        textCarNotFound = findViewById(R.id.textView_car_notFound);
        iViewNewCar = findViewById(R.id.imageView_new_car);

        // ????????? ?????? ??? ?????????
        settingActionBar();
        settingScroll();

        // ?????? ??????
        iViewNewCar.setOnClickListener(v -> {
            String loginAccessToken = PreferenceManager.getString(CarActivity.this, "LOGIN_ACCESS_TOKEN");

            Intent intent = new Intent(CarActivity.this, CarRegistrationActivity.class);
            intent.putExtra("LOGIN_ACCESS_TOKEN", loginAccessToken);

            startActivityResultForCar.launch(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserCarList();
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
            startActivity(new Intent(CarActivity.this, MainActivity.class));

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

            PreferenceManager.setString(CarActivity.this, "LOGIN_ACCESS_TOKEN", loginAccessToken);
        }
    }

    private void settingActionBar() {
        setSupportActionBar(toolbarCar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("");
        actionBar.setSubtitle("");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_left_solid);
    }

    private void settingScroll() {
        NestedScrollView scrollView = findViewById(R.id.scrollView_car);

        listViewCar.setOnTouchListener((v, event) -> {
            scrollView.requestDisallowInterceptTouchEvent(true);

            return false;
        });
    }

    private void loadUserCarList() {
        String loginAccessToken = PreferenceManager.getString(CarActivity.this, "LOGIN_ACCESS_TOKEN");

        carService = new CarService(loginAccessToken);

        try {
            CommonResponse commonResponse = carService.execute(CAR_SERVICE_GET_USER_CARS).get();

            if (commonResponse.isSuccess()) {
                ListResultResponse<Car> listResultResponse = (ListResultResponse<Car>) commonResponse;
                List<Car> userCarList = listResultResponse.getDataList();

                if (userCarList.size() != 0) {
                    textCarNotFound.setVisibility(View.GONE);
                    listViewCar.setVisibility(View.VISIBLE);

                    listViewCar.setAdapter(new CustomCarList(this, userCarList));

                } else {
                    listViewCar.setVisibility(View.GONE);
                    textCarNotFound.setVisibility(View.VISIBLE);
                }

            } else {
                String loadCarListFailedMsg = "?????? ????????? ????????? ??? ????????????.";

                SnackBarManager.showMessage(findViewById(R.id.scrollView_car), loadCarListFailedMsg);
            }

        } catch (ExecutionException | InterruptedException e) {
            Log.w("Car", "Loading user car list failed.");
        }
    }

    private void showDialogForCarDetails(Car car) {
        Dialog dialog = new Dialog(CarActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_car_details);

        dialog.show();

        // ????????? ??????????????? ??????
        TextView carModel = dialog.findViewById(R.id.textView_car_model);
        TextView carModelYear = dialog.findViewById(R.id.textView_car_model_year);
        TextView carType = dialog.findViewById(R.id.textView_car_type);
        TextView carNumber = dialog.findViewById(R.id.textView_car_number);

        carModel.setText(car.getCarModel());
        carModelYear.setText(car.getCarModelYear());
        carType.setText(car.getCarType());
        carNumber.setText(car.getCarNumber());

        Button btnOk = dialog.findViewById(R.id.btn_car_ok);
        Button btnDelete = dialog.findViewById(R.id.btn_car_delete);

        btnOk.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> showDialogForCarDelete(car.getId()));
    }

    private void showDialogForCarDelete(long carId) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CarActivity.this);

        alertDialogBuilder
                .setTitle("?????? ??????")
                .setMessage("????????? ????????? ?????????????????????????\n?????? ????????? ?????? ??? ????????? ?????? ???????????????.")
                .setCancelable(true)
                .setPositiveButton("??????", (dialog, which) -> {
                    String loginAccessToken = PreferenceManager.getString(CarActivity.this, "LOGIN_ACCESS_TOKEN");

                    carService = new CarService(loginAccessToken, carId);
                    carService.execute(CAR_SERVICE_DELETE_USER_CAR);

                    finish();
                    startActivity(new Intent(CarActivity.this, CarActivity.class));
                })
                .setNegativeButton("??????", (dialog, which) -> dialog.cancel())
                .create()
                .show();
    }

    private class CustomCarList extends ArrayAdapter<Car> {

        private final Activity context;
        private final List<Car> userCarList;

        public CustomCarList(Activity context, List<Car> userCarList) {
            super(context, R.layout.listview_car, userCarList);
            this.context = context;
            this.userCarList = userCarList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.listview_car, null, true);

            TextView carNumber = rowView.findViewById(R.id.listView_car_number);
            TextView carModel = rowView.findViewById(R.id.listView_car_model);
            ImageView carDetails = rowView.findViewById(R.id.imageView_car_details);

            Car car = userCarList.get(position);

            carNumber.setText(car.getCarNumber());
            carModel.setText(car.getCarModel());
            carDetails.setOnClickListener(v -> showDialogForCarDetails(car));

            return rowView;
        }
    }
}
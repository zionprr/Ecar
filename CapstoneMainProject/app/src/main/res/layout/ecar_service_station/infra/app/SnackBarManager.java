package com.example.ecar_service_station.infra.app;

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class SnackBarManager {

    public static void showMessage(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }
}

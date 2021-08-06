package com.example.capstonemainproject.infra.app;

import android.graphics.Color;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class SnackBarManager {

    public static void showMessage(View view, String message) {
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show();
    }

    public static void showIndefiniteMessage(View view, String message, String action) {
        Snackbar.make(view, message, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(Color.parseColor("#FF0000"))
                .setAction(action, v -> {})
                .show();
    }
}

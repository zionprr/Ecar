package com.example.capstonemainproject.infra.network;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

public class CustomFcmService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        Log.d("FCM", "Refreshed token: " + token);
    }
}

package com.example.capstonemainproject.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.capstonemainproject.R;

public class ReservationStatement2Fragment extends Fragment {

    private Context currentContext;
    private View currentView;

    private String loginAccessToken;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        currentContext = context;

        if (getArguments() != null) {
            loginAccessToken = getArguments().getString("LOGIN_ACCESS_TOKEN");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        currentView = inflater.inflate(R.layout.fragment_reservation_statement2, container, false);


        return currentView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}

package com.example.capstonemainproject.infra.app;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    public static final String PREFERENCES_NAME = "activity-preferences";

    private static final String DEFAULT_VALUE_STRING = "";
    private static final int DEFAULT_VALUE_INT = -1;

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public static void setString(Context context, String key, String value) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(key, value);
        editor.apply();
    }

    public static void setInt(Context context, String key, int value) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(key, value);
        editor.apply();
    }

    public static String getString(Context context, String key) {
        SharedPreferences preferences = getPreferences(context);

        return preferences.getString(key, DEFAULT_VALUE_STRING);
    }

    public static int getInt(Context context, String key) {
        SharedPreferences preferences = getPreferences(context);

        return preferences.getInt(key, DEFAULT_VALUE_INT);
    }
}

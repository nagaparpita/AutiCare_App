package com.example.auticare;

import android.content.Context;
import android.content.SharedPreferences;

public class DataManager {

    private static final String[] PREF_FILES = {
        "AutiCareRewardData",
        "RoutineData",
        "RoutineDailyLogs",
        "MedicineStatus",
        "MedicineHistory",
        "CommunicationLogs",
        "EmotionLogs",
        "AutiCareData",
        "UserPrefs"
    };

    public static void refreshAllData(Context context) {
        for (String fileName : PREF_FILES) {
            SharedPreferences prefs = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
        }
        
        // Also clear any Firebase references if needed, 
        // but for local state "Refresh" usually means local storage.
    }
}

package com.example.auticare;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RoutineActivity extends AppCompatActivity {

    private CheckBox task1, task2, task3, task4, task5, task6, task7;
    private View card1, card2, card3, card4, card5, card6, card7;
    private Button saveBtn;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine);

        // Checkboxes
        task1 = findViewById(R.id.task1);
        task2 = findViewById(R.id.task2);
        task3 = findViewById(R.id.task3);
        task4 = findViewById(R.id.task4);
        task5 = findViewById(R.id.task5);
        task6 = findViewById(R.id.task6);
        task7 = findViewById(R.id.task7);

        // Cards
        card1 = findViewById(R.id.card1);
        card2 = findViewById(R.id.card2);
        card3 = findViewById(R.id.card3);
        card4 = findViewById(R.id.card4);
        card5 = findViewById(R.id.card5);
        card6 = findViewById(R.id.card6);
        card7 = findViewById(R.id.card7);

        saveBtn = findViewById(R.id.saveBtn);

        prefs = getSharedPreferences("RoutineData", MODE_PRIVATE);

        ImageView backBtn = findViewById(R.id.backBtn);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        checkDailyReset();
        loadSavedData();
        setupCardClicks();

        saveBtn.setOnClickListener(v -> saveData());
    }

    private void checkDailyReset() {
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastResetDate = prefs.getString("lastResetDate", "");

        if (!currentDate.equals(lastResetDate)) {
            // It's a new day! Reset all routine tasks.
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("task1", false);
            editor.putBoolean("task2", false);
            editor.putBoolean("task3", false);
            editor.putBoolean("task4", false);
            editor.putBoolean("task5", false);
            editor.putBoolean("task6", false);
            editor.putBoolean("task7", false);
            editor.putString("lastResetDate", currentDate);
            editor.apply();
        }
    }

    private void setupCardClicks() {
        if (card1 != null) card1.setOnClickListener(v -> toggleTask(task1));
        if (card2 != null) card2.setOnClickListener(v -> toggleTask(task2));
        if (card3 != null) card3.setOnClickListener(v -> toggleTask(task3));
        if (card4 != null) card4.setOnClickListener(v -> toggleTask(task4));
        if (card5 != null) card5.setOnClickListener(v -> toggleTask(task5));
        if (card6 != null) card6.setOnClickListener(v -> toggleTask(task6));
        if (card7 != null) card7.setOnClickListener(v -> toggleTask(task7));
    }

    private void toggleTask(CheckBox cb) {
        cb.setChecked(!cb.isChecked());
        if (cb.isChecked()) {
            // New Reward System Integration
            RewardManager.addStar(this, RewardManager.GAME_ROUTINE);
            RewardManager.incrementActivityCount(this, RewardManager.COUNTER_ROUTINE);
        }
    }

    private void saveData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("task1", task1.isChecked());
        editor.putBoolean("task2", task2.isChecked());
        editor.putBoolean("task3", task3.isChecked());
        editor.putBoolean("task4", task4.isChecked());
        editor.putBoolean("task5", task5.isChecked());
        editor.putBoolean("task6", task6.isChecked());
        editor.putBoolean("task7", task7.isChecked());
        editor.apply();

        // Sync with Routine Status (Shared with Parent/Helper)
        syncWithRoutineStatus();

        Toast.makeText(this, "Routine Saved", Toast.LENGTH_SHORT).show();
    }

    private void syncWithRoutineStatus() {
        SharedPreferences dailyPrefs = getSharedPreferences("RoutineDailyLogs", MODE_PRIVATE);
        SharedPreferences.Editor editor = dailyPrefs.edit();
        
        String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String timeStr = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        
        // Mapping tasks to routine names
        updateStatus(editor, dateKey, "Brush teeth", task1.isChecked(), timeStr);
        updateStatus(editor, dateKey, "Take bath", task2.isChecked(), timeStr);
        updateStatus(editor, dateKey, "Eat breakfast", task3.isChecked(), timeStr);
        updateStatus(editor, dateKey, "Wash hands", task4.isChecked(), timeStr);
        updateStatus(editor, dateKey, "Throw trash", task5.isChecked(), timeStr);
        updateStatus(editor, dateKey, "Play time", task6.isChecked(), timeStr);
        updateStatus(editor, dateKey, "Sleep", task7.isChecked(), timeStr);
        
        editor.apply();
    }

    private void updateStatus(SharedPreferences.Editor editor, String date, String name, boolean completed, String time) {
        String key = date + "_" + name;
        if (completed) {
            // Status | Time | MarkedBy | Note
            editor.putString(key, "Completed|" + time + "|Child|");
        } else {
            // If it was completed before and now unchecked, revert to Pending
            editor.putString(key, "Pending|||");
        }
    }

    private void loadSavedData() {
        task1.setChecked(prefs.getBoolean("task1", false));
        task2.setChecked(prefs.getBoolean("task2", false));
        task3.setChecked(prefs.getBoolean("task3", false));
        task4.setChecked(prefs.getBoolean("task4", false));
        task5.setChecked(prefs.getBoolean("task5", false));
        task6.setChecked(prefs.getBoolean("task6", false));
        task7.setChecked(prefs.getBoolean("task7", false));
    }
}

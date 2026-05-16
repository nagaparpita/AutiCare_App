package com.example.auticare;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProgressReportActivity extends AppCompatActivity {

    private Spinner dateSelector;
    private TextView reportDateText, routineSummaryText, commSummaryText, emotionSummaryText, medSummaryText, snapshotText;
    private RecyclerView historyRecyclerView;
    private HistoryAdapter historyAdapter;
    private List<DailySummary> historyList;
    private String currentSelectedDate; // yyyy-MM-dd

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress_report);

        dateSelector = findViewById(R.id.dateSelector);
        reportDateText = findViewById(R.id.reportDateText);
        routineSummaryText = findViewById(R.id.routineSummaryText);
        commSummaryText = findViewById(R.id.commSummaryText);
        emotionSummaryText = findViewById(R.id.emotionSummaryText);
        medSummaryText = findViewById(R.id.medSummaryText);
        snapshotText = findViewById(R.id.snapshotText);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);

        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setNestedScrollingEnabled(false);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        setupDateSelector();
    }

    private void setupDateSelector() {
        String[] options = {"Today", "Previous Day", "Tomorrow", "Select Date"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSelector.setAdapter(adapter);

        dateSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Calendar cal = Calendar.getInstance();
                if (position == 0) { // Today
                    updateReportDate(cal.getTime());
                } else if (position == 1) { // Previous Day
                    cal.add(Calendar.DAY_OF_YEAR, -1);
                    updateReportDate(cal.getTime());
                } else if (position == 2) { // Tomorrow
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                    updateReportDate(cal.getTime());
                } else if (position == 3) { // Select Date
                    showDatePicker();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Init with Today
        updateReportDate(new Date());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            cal.set(year, month, dayOfMonth);
            updateReportDate(cal.getTime());
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateReportDate(Date date) {
        SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat sdfKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        currentSelectedDate = sdfKey.format(date);
        
        String displayPrefix = "";
        String today = sdfKey.format(new Date());
        
        Calendar prevCal = Calendar.getInstance();
        prevCal.add(Calendar.DAY_OF_YEAR, -1);
        String yesterday = sdfKey.format(prevCal.getTime());
        
        Calendar tomCal = Calendar.getInstance();
        tomCal.add(Calendar.DAY_OF_YEAR, 1);
        String tomorrow = sdfKey.format(tomCal.getTime());

        if (currentSelectedDate.equals(today)) displayPrefix = "Today - ";
        else if (currentSelectedDate.equals(yesterday)) displayPrefix = "Previous Day - ";
        else if (currentSelectedDate.equals(tomorrow)) displayPrefix = "Tomorrow - ";
        
        reportDateText.setText(displayPrefix + sdfDisplay.format(date));
        
        generateReportForDate(currentSelectedDate);
    }

    private void generateReportForDate(String dateKey) {
        calculateRoutineSummary(dateKey);
        calculateCommSummary(dateKey);
        calculateEmotionSummary(dateKey);
        calculateMedSummary(dateKey);
        updateSnapshot(dateKey);
        loadHistoryList(30); 
    }

    private void calculateRoutineSummary(String dateKey) {
        SharedPreferences prefs = getSharedPreferences("RoutineDailyLogs", MODE_PRIVATE);
        int completed = 0, total = 0;
        String[] routines = {"Brush teeth", "Take bath", "Eat breakfast", "Wash hands", "Play time", "Sleep"};

        for (String r : routines) {
            total++;
            String val = prefs.getString(dateKey + "_" + r, "");
            if (val.startsWith("Completed")) completed++;
        }
        routineSummaryText.setText("✔ " + completed + " / " + total + " completed");
    }

    private void calculateCommSummary(String dateKey) {
        SharedPreferences prefs = getSharedPreferences("CommunicationLogs", MODE_PRIVATE);
        int success = 0, total = 0;
        Map<String, ?> all = prefs.getAll();

        for (Map.Entry<String, ?> entry : all.entrySet()) {
            if (entry.getKey().startsWith("log_")) {
                try {
                    long timestamp = Long.parseLong(entry.getKey().substring(4));
                    String logDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(timestamp));
                    if (logDate.equals(dateKey)) {
                        total++;
                        if (entry.getValue().toString().contains("Successful")) success++;
                    }
                } catch (Exception e) {}
            }
        }
        commSummaryText.setText("Attempts: " + total + "\nSuccessful: " + success);
    }

    private void calculateEmotionSummary(String dateKey) {
        SharedPreferences prefs = getSharedPreferences("EmotionLogs", MODE_PRIVATE);
        Map<String, Integer> counts = new HashMap<>();
        Map<String, ?> all = prefs.getAll();

        for (Map.Entry<String, ?> entry : all.entrySet()) {
            if (entry.getKey().startsWith("log_")) {
                try {
                    long timestamp = Long.parseLong(entry.getKey().substring(4));
                    String logDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(timestamp));
                    if (logDate.equals(dateKey)) {
                        String[] parts = entry.getValue().toString().split("\\|");
                        if (parts.length >= 3) {
                            String emotion = parts[2].trim();
                            counts.put(emotion, counts.getOrDefault(emotion, 0) + 1);
                        }
                    }
                } catch (Exception e) {}
            }
        }

        String mostObserved = "None";
        int max = -1;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            if (e.getValue() > max) { max = e.getValue(); mostObserved = e.getKey(); }
        }
        emotionSummaryText.setText("Most observed: " + mostObserved);
    }

    private void calculateMedSummary(String dateKey) {
        SharedPreferences historyPrefs = getSharedPreferences("MedicineHistory", MODE_PRIVATE);
        int given = 0;

        for (Map.Entry<String, ?> entry : historyPrefs.getAll().entrySet()) {
            if (entry.getKey().startsWith("history_")) {
                try {
                    long timestamp = Long.parseLong(entry.getKey().substring(8));
                    String logDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(timestamp));
                    if (logDate.equals(dateKey)) given++;
                } catch (Exception e) {}
            }
        }
        medSummaryText.setText("✔ " + given + " administered");
    }

    private void updateSnapshot(String dateKey) {
        // Simple logic based on current date relative to today
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        if (dateKey.equals(today)) {
            snapshotText.setText("➡ Good routine participation\n➡ Communication was active\n➡ Emotional state mostly calm");
        } else {
            snapshotText.setText("➡ Day summary available\n➡ Review details above for " + dateKey);
        }
    }

    private void loadHistoryList(int days) {
        historyList = new ArrayList<>();
        SharedPreferences rPrefs = getSharedPreferences("RoutineDailyLogs", MODE_PRIVATE);
        String[] routines = {"Brush teeth", "Take bath", "Eat breakfast", "Wash hands", "Play time", "Sleep"};

        for (int i = 0; i < days; i++) {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -i);
            String dateKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
            String dateDisplay = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(cal.getTime());

            int completed = 0;
            boolean hasData = false;
            for (String r : routines) {
                String val = rPrefs.getString(dateKey + "_" + r, "");
                if (!val.isEmpty()) hasData = true;
                if (val.startsWith("Completed")) completed++;
            }

            if (hasData) {
                historyList.add(new DailySummary(dateDisplay, "Routine: " + completed + "/6 completed"));
            }
        }
        historyAdapter = new HistoryAdapter(historyList);
        historyRecyclerView.setAdapter(historyAdapter);
    }

    static class DailySummary {
        String date, summary;
        DailySummary(String d, String s) { date = d; summary = s; }
    }

    class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<DailySummary> items;
        HistoryAdapter(List<DailySummary> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_summary, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DailySummary s = items.get(position);
            holder.date.setText(s.date);
            holder.summary.setText(s.summary);
        }

        @Override
        public int getItemCount() { return items.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView date, summary;
            ViewHolder(View v) {
                super(v);
                date = v.findViewById(R.id.historyDateText);
                summary = v.findViewById(R.id.historySummaryText);
            }
        }
    }
}

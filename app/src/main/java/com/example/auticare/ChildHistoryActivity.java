package com.example.auticare;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChildHistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private HistoryAdapter adapter;
    private List<HistoryItem> historyList;
    private Spinner rangeSpinner;
    private ImageButton datePickerBtn;
    private Calendar selectedCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_history);

        rangeSpinner = findViewById(R.id.rangeSpinner);
        datePickerBtn = findViewById(R.id.datePickerBtn);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        setupRangeSelector();
        
        datePickerBtn.setOnClickListener(v -> showDatePicker());
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedCalendar.set(Calendar.YEAR, year);
                    selectedCalendar.set(Calendar.MONTH, month);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    
                    // Reset spinner selection when a specific date is chosen
                    rangeSpinner.setSelection(AdapterView.INVALID_POSITION, false);
                    loadHistoryForSpecificDate(selectedCalendar.getTime());
                },
                selectedCalendar.get(Calendar.YEAR),
                selectedCalendar.get(Calendar.MONTH),
                selectedCalendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void setupRangeSelector() {
        String[] options = {"Today", "Last 7 Days", "Last 30 Days"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        rangeSpinner.setAdapter(spinnerAdapter);

        rangeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int days = 1;
                if (position == 1) days = 7;
                else if (position == 2) days = 30;
                loadCombinedHistory(days);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Init
        loadCombinedHistory(1);
    }

    private void loadHistoryForSpecificDate(Date date) {
        historyList = new ArrayList<>();
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();
        
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        long endOfDay = cal.getTimeInMillis();

        fetchHistoryInRange(startOfDay, endOfDay);
        updateUI();
    }

    private void loadCombinedHistory(int days) {
        historyList = new ArrayList<>();
        long limitMillis = System.currentTimeMillis() - (days * 24L * 3600L * 1000L);
        fetchHistoryInRange(limitMillis, System.currentTimeMillis());
        updateUI();
    }

    private void fetchHistoryInRange(long start, long end) {
        fetchEmotionHistory(start, end);
        fetchCommunicationHistory(start, end);
        fetchRoutineHistory(start, end);
        fetchMedicineHistory(start, end);
    }

    private void updateUI() {
        // Sort by timestamp latest first
        Collections.sort(historyList, (o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
        adapter = new HistoryAdapter(historyList);
        historyRecyclerView.setAdapter(adapter);
    }

    private void fetchEmotionHistory(long start, long end) {
        SharedPreferences prefs = getSharedPreferences("EmotionLogs", MODE_PRIVATE);
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            if (entry.getKey().startsWith("log_")) {
                try {
                    long ts = Long.parseLong(entry.getKey().substring(4));
                    if (ts >= start && ts <= end) {
                        String[] parts = entry.getValue().toString().split("\\|");
                        if (parts.length >= 5) {
                            historyList.add(new HistoryItem(
                                    parts[0].trim(), parts[1].trim(), "Emotion",
                                    "🙂 Emotion – " + parts[2].trim(),
                                    "", "Observed by: " + parts[4].trim(), ts));
                        }
                    }
                } catch (Exception e) {}
            }
        }
    }

    private void fetchCommunicationHistory(long start, long end) {
        SharedPreferences prefs = getSharedPreferences("CommunicationLogs", MODE_PRIVATE);
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            if (entry.getKey().startsWith("log_")) {
                try {
                    long ts = Long.parseLong(entry.getKey().substring(4));
                    if (ts >= start && ts <= end) {
                        String[] parts = entry.getValue().toString().split("\\|");
                        if (parts.length >= 6) {
                            historyList.add(new HistoryItem(
                                    parts[0].trim(), parts[1].trim(), "Communication",
                                    "💬 Communication – " + parts[3].trim(),
                                    parts[4].trim(), "Observed by: " + parts[5].trim(), ts));
                        }
                    }
                } catch (Exception e) {}
            }
        }
    }

    private void fetchRoutineHistory(long start, long end) {
        SharedPreferences prefs = getSharedPreferences("RoutineDailyLogs", MODE_PRIVATE);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            String key = entry.getKey();
            if (key.contains("_")) {
                try {
                    String dateStr = key.substring(0, key.indexOf("_"));
                    String routineName = key.substring(key.indexOf("_") + 1);
                    String[] parts = entry.getValue().toString().split("\\|");
                    if (parts.length >= 3) {
                        String timeStr = parts[1].trim();
                        if (timeStr.isEmpty()) continue;
                        
                        Date date = sdf.parse(dateStr + " " + timeStr);
                        long ts = date.getTime();
                        
                        if (ts >= start && ts <= end) {
                            String displayDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date);
                            historyList.add(new HistoryItem(
                                    displayDate, timeStr, "Routine",
                                    "🪥 Routine – " + routineName + " – " + parts[0].trim(),
                                    "", "Marked by: " + parts[2].trim(), ts));
                        }
                    }
                } catch (Exception e) {}
            }
        }
    }

    private void fetchMedicineHistory(long start, long end) {
        SharedPreferences prefs = getSharedPreferences("MedicineHistory", MODE_PRIVATE);
        for (Map.Entry<String, ?> entry : prefs.getAll().entrySet()) {
            if (entry.getKey().startsWith("history_")) {
                try {
                    long ts = Long.parseLong(entry.getKey().substring(8));
                    if (ts >= start && ts <= end) {
                        String val = entry.getValue().toString();
                        String[] parts = val.split(" - ");
                        if (parts.length >= 4) {
                            historyList.add(new HistoryItem(
                                    parts[0].trim(), parts[1].trim(), "Medicine",
                                    "💊 Medicine – " + parts[2].trim() + " – Given",
                                    "", parts[3].trim(), ts));
                        }
                    }
                } catch (Exception e) {}
            }
        }
    }

    private static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<HistoryItem> items;

        public HistoryAdapter(List<HistoryItem> items) { this.items = items; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            HistoryItem item = items.get(position);
            holder.dateText.setText(item.getDate());
            holder.timeText.setText(item.getTime());
            holder.descText.setText(item.getDescription());
            holder.observerText.setText(item.getPerformedBy());
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView dateText, timeText, descText, observerText;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                dateText = itemView.findViewById(R.id.historyDate);
                timeText = itemView.findViewById(R.id.historyTime);
                descText = itemView.findViewById(R.id.historyDescription);
                observerText = itemView.findViewById(R.id.historyObservedBy);
            }
        }
    }
}

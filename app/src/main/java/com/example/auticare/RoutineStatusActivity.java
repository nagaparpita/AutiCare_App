package com.example.auticare;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RoutineStatusActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RoutineLogAdapter adapter;
    private Spinner dateSelector;
    private TextView selectedDateText;
    private List<RoutineDailyLog> dailyLogs;
    private String currentSelectedDate; // YYYY-MM-DD format
    
    private final String[] masterRoutines = {
            "Brush teeth", "Take bath", "Eat breakfast", "Wash hands", "Play time", "Sleep"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routine_status);

        recyclerView = findViewById(R.id.routineRecyclerView);
        dateSelector = findViewById(R.id.dateSelector);
        selectedDateText = findViewById(R.id.selectedDateText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        setupDateSelector();
    }

    private void setupDateSelector() {
        String[] options = {"Today", "Tomorrow", "Select Date"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, options);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSelector.setAdapter(spinnerAdapter);

        dateSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Calendar cal = Calendar.getInstance();
                if (position == 0) { // Today
                    updateDate(cal.getTime());
                } else if (position == 1) { // Tomorrow
                    cal.add(Calendar.DAY_OF_YEAR, 1);
                    updateDate(cal.getTime());
                } else if (position == 2) { // Select Date
                    showDatePicker();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Initialize with Today
        updateDate(new Date());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            cal.set(year, month, dayOfMonth);
            updateDate(cal.getTime());
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateDate(Date date) {
        SimpleDateFormat sdfDisplay = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        SimpleDateFormat sdfKey = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        
        currentSelectedDate = sdfKey.format(date);
        
        String displayPrefix = "";
        String today = sdfKey.format(new Date());
        Calendar tomCal = Calendar.getInstance();
        tomCal.add(Calendar.DAY_OF_YEAR, 1);
        String tomorrow = sdfKey.format(tomCal.getTime());
        
        if (currentSelectedDate.equals(today)) displayPrefix = "Today - ";
        else if (currentSelectedDate.equals(tomorrow)) displayPrefix = "Tomorrow - ";
        
        selectedDateText.setText(displayPrefix + sdfDisplay.format(date));
        loadRoutineLogs();
    }

    private void loadRoutineLogs() {
        dailyLogs = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("RoutineDailyLogs", MODE_PRIVATE);
        
        for (String routineName : masterRoutines) {
            String key = currentSelectedDate + "_" + routineName;
            String data = prefs.getString(key, null);
            
            if (data == null) {
                // Pre-create Pending entry
                dailyLogs.add(new RoutineDailyLog(routineName, "Pending", currentSelectedDate, "", "", ""));
            } else {
                // Format: Status | Time | MarkedBy | Note
                String[] parts = data.split("\\|", -1);
                if (parts.length >= 4) {
                    dailyLogs.add(new RoutineDailyLog(routineName, parts[0], currentSelectedDate, parts[1], parts[2], parts[3]));
                } else {
                    dailyLogs.add(new RoutineDailyLog(routineName, "Pending", currentSelectedDate, "", "", ""));
                }
            }
        }
        
        adapter = new RoutineLogAdapter(dailyLogs);
        recyclerView.setAdapter(adapter);
    }

    private void saveRoutineLog(RoutineDailyLog log) {
        SharedPreferences prefs = getSharedPreferences("RoutineDailyLogs", MODE_PRIVATE);
        String key = log.date + "_" + log.routineName;
        String data = log.status + "|" + log.time + "|" + log.markedBy + "|" + log.note;
        prefs.edit().putString(key, data).apply();
    }

    static class RoutineDailyLog {
        String routineName, status, date, time, markedBy, note;

        public RoutineDailyLog(String routineName, String status, String date, String time, String markedBy, String note) {
            this.routineName = routineName;
            this.status = status;
            this.date = date;
            this.time = time;
            this.markedBy = markedBy;
            this.note = note;
        }
    }

    class RoutineLogAdapter extends RecyclerView.Adapter<RoutineLogAdapter.ViewHolder> {
        private List<RoutineDailyLog> logs;

        public RoutineLogAdapter(List<RoutineDailyLog> logs) { this.logs = logs; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_routine_log, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RoutineDailyLog log = logs.get(position);
            holder.nameText.setText(log.routineName);
            
            if (log.status.equals("Completed")) {
                holder.statusIndicator.setText("✔");
                holder.timeText.setVisibility(View.VISIBLE);
                holder.timeText.setText(log.time);
                holder.detailsLayout.setVisibility(View.VISIBLE);
                holder.markedByText.setText("Marked by: " + log.markedBy);
                holder.noteText.setText(log.note.isEmpty() ? "" : "Note: " + log.note);
                holder.actionButtons.setVisibility(View.GONE);
            } else if (log.status.equals("Skipped")) {
                holder.statusIndicator.setText("❌");
                holder.timeText.setVisibility(View.GONE);
                holder.detailsLayout.setVisibility(View.VISIBLE);
                holder.markedByText.setText("Marked by: " + log.markedBy);
                holder.noteText.setText(log.note.isEmpty() ? "" : "Note: " + log.note);
                holder.actionButtons.setVisibility(View.GONE);
            } else {
                holder.statusIndicator.setText("⏳");
                holder.timeText.setVisibility(View.GONE);
                holder.detailsLayout.setVisibility(View.GONE);
                holder.actionButtons.setVisibility(View.VISIBLE);
            }

            holder.btnComplete.setOnClickListener(v -> markStatus(log, position, "Completed"));
            holder.btnSkip.setOnClickListener(v -> markStatus(log, position, "Skipped"));
        }

        private void markStatus(RoutineDailyLog log, int position, String status) {
            log.status = status;
            log.time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
            
            SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            log.markedBy = userPrefs.getString("user_role", "User");
            log.note = ""; // For now simple, could add a dialog for note
            
            saveRoutineLog(log);
            notifyItemChanged(position);
            Toast.makeText(RoutineStatusActivity.this, log.routineName + " marked as " + status, Toast.LENGTH_SHORT).show();
        }

        @Override
        public int getItemCount() { return logs.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView statusIndicator, nameText, timeText, markedByText, noteText;
            View detailsLayout, actionButtons;
            Button btnSkip, btnComplete;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                statusIndicator = itemView.findViewById(R.id.statusIndicator);
                nameText = itemView.findViewById(R.id.routineNameText);
                timeText = itemView.findViewById(R.id.timeText);
                markedByText = itemView.findViewById(R.id.markedByText);
                noteText = itemView.findViewById(R.id.noteText);
                detailsLayout = itemView.findViewById(R.id.detailsLayout);
                actionButtons = itemView.findViewById(R.id.actionButtons);
                btnSkip = itemView.findViewById(R.id.btnSkip);
                btnComplete = itemView.findViewById(R.id.btnComplete);
            }
        }
    }
}

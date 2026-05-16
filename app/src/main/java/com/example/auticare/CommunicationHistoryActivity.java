package com.example.auticare;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class CommunicationHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CommAdapter adapter;
    private List<CommLog> commLogs;
    
    private TextView tvLatestComm, tvLatestTime, tvTodayDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_history);

        // Initialize Summary Header
        tvLatestComm = findViewById(R.id.tvLatestComm);
        tvLatestTime = findViewById(R.id.tvLatestTime);
        tvTodayDate = findViewById(R.id.tvTodayDate);

        recyclerView = findViewById(R.id.commRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        updateLiveSummary();
        loadHistory();
    }

    private void updateLiveSummary() {
        SharedPreferences commPrefs = getSharedPreferences("CommunicationLogs", MODE_PRIVATE);
        String latestLog = commPrefs.getString("latest_log", "");

        String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        if (tvTodayDate != null) tvTodayDate.setText("Today - " + currentDate);

        if (!latestLog.isEmpty()) {
            String[] parts = latestLog.split("\\|");
            if (parts.length >= 4) {
                String logDate = parts[0].trim();
                String logTime = parts[1].trim();
                String logMessage = parts[3].trim();

                if (logDate.equals(currentDate)) {
                    if (tvLatestComm != null) tvLatestComm.setText("Last Communication: " + logMessage);
                    if (tvLatestTime != null) tvLatestTime.setText("Time: " + logTime);
                } else {
                    resetSummary();
                }
            }
        } else {
            resetSummary();
        }
    }

    private void resetSummary() {
        if (tvLatestComm != null) tvLatestComm.setText("Last Communication: None");
        if (tvLatestTime != null) tvLatestTime.setText("Time: --");
    }

    private void loadHistory() {
        commLogs = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("CommunicationLogs", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();

        // Use a TreeMap to sort logs by timestamp (keys are log_timestamp)
        TreeMap<Long, String> sortedLogs = new TreeMap<>(Collections.reverseOrder());

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith("log_")) {
                try {
                    long timestamp = Long.parseLong(entry.getKey().substring(4));
                    sortedLogs.put(timestamp, entry.getValue().toString());
                } catch (Exception e) {
                    // Ignore malformed keys
                }
            }
        }

        for (String logValue : sortedLogs.values()) {
            // Format: Date | Time | Type | Message | Status | ObservedBy
            String[] parts = logValue.split("\\|");
            if (parts.length >= 6) {
                commLogs.add(new CommLog(
                    parts[2].trim(), // Type
                    parts[3].trim(), // Sentence
                    "Request",       // Purpose
                    parts[4].trim(), // Status
                    parts[0].trim(), // Date
                    parts[1].trim(), // Time
                    parts[5].trim(), // ObservedBy
                    "Observation",   // Context (default)
                    "Auto-captured"  // Note (default)
                ));
            }
        }

        adapter = new CommAdapter(commLogs);
        recyclerView.setAdapter(adapter);
    }

    // Data model for CommunicationDailyLog
    static class CommLog {
        String type, sentence, purpose, status, date, time, observer, context, note;

        public CommLog(String type, String sentence, String purpose, String status, String date, String time, String observer, String context, String note) {
            this.type = type;
            this.sentence = sentence;
            this.purpose = purpose;
            this.status = status;
            this.date = date;
            this.time = time;
            this.observer = observer;
            this.context = context;
            this.note = note;
        }
    }

    class CommAdapter extends RecyclerView.Adapter<CommAdapter.ViewHolder> {
        private List<CommLog> logs;

        public CommAdapter(List<CommLog> logs) { this.logs = logs; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_communication_log, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            CommLog log = logs.get(position);
            holder.type.setText(log.type);
            holder.dateTime.setText(log.date + " - " + log.time);
            holder.message.setText("“" + log.sentence + "”");
            holder.status.setText("Status: " + log.status);
            holder.observer.setText("By: " + log.observer);
            holder.context.setText("Context: " + log.context);
            holder.note.setText("Note: " + log.note);

            if (log.status.equalsIgnoreCase("Successful")) {
                holder.status.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            } else if (log.status.equalsIgnoreCase("Partial")) {
                holder.status.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            } else {
                holder.status.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        }

        @Override
        public int getItemCount() { return logs.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView type, dateTime, message, status, observer, context, note;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                type = itemView.findViewById(R.id.commTypeText);
                dateTime = itemView.findViewById(R.id.dateTimeText);
                message = itemView.findViewById(R.id.messageText);
                status = itemView.findViewById(R.id.statusText);
                observer = itemView.findViewById(R.id.observerText);
                context = itemView.findViewById(R.id.contextText);
                note = itemView.findViewById(R.id.noteText);
            }
        }
    }
}

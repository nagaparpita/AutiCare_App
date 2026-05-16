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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MedicineHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter adapter;
    private List<HistoryEntry> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_history);

        recyclerView = findViewById(R.id.historyRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        loadHistory();
    }

    private void loadHistory() {
        historyList = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("MedicineHistory", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();

        // Sort entries by timestamp (keys are history_timestamp) in reverse order
        TreeMap<Long, String> sortedLogs = new TreeMap<>(Collections.reverseOrder());

        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            if (entry.getKey().startsWith("history_")) {
                try {
                    long timestamp = Long.parseLong(entry.getKey().substring(8));
                    sortedLogs.put(timestamp, entry.getValue().toString());
                } catch (Exception e) {
                    // Ignore malformed keys
                }
            }
        }

        for (String logValue : sortedLogs.values()) {
            historyList.add(new HistoryEntry(logValue));
        }

        adapter = new HistoryAdapter(historyList);
        recyclerView.setAdapter(adapter);
    }

    class HistoryEntry {
        String data;
        public HistoryEntry(String data) { this.data = data; }
    }

    class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<HistoryEntry> entries;

        public HistoryAdapter(List<HistoryEntry> entries) { this.entries = entries; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.textView.setText(entries.get(position).data);
        }

        @Override
        public int getItemCount() { return entries.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(R.id.historyEntryText);
            }
        }
    }
}

package com.example.auticare;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class EmotionHistoryActivity extends AppCompatActivity {

    private BarChart barChart;
    private RecyclerView recyclerView;
    private Spinner filterSpinner;
    private List<EmotionLog> emotionLogs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion_history);

        barChart = findViewById(R.id.emotionBarChart);
        recyclerView = findViewById(R.id.emotionRecyclerView);
        filterSpinner = findViewById(R.id.filterSpinner);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        setupFilter();
        loadData();
    }

    private void setupFilter() {
        String[] filters = {"Last 7 Days", "Last 30 Days"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, filters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterSpinner.setAdapter(adapter);
    }

    private void loadData() {
        emotionLogs = new ArrayList<>();
        SharedPreferences prefs = getSharedPreferences("EmotionLogs", MODE_PRIVATE);
        Map<String, ?> allEntries = prefs.getAll();

        // Sort logs by timestamp (keys are log_timestamp) in reverse order
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
            // Format: Date | Time | Emotion | Intensity | ObservedBy | Context | Note
            String[] parts = logValue.split("\\|");
            if (parts.length >= 4) {
                emotionLogs.add(new EmotionLog(
                    parts[2].trim(), // Emotion Type
                    parts[3].trim(), // Intensity
                    parts[0].trim(), // Date
                    parts[1].trim(), // Time
                    parts[4].trim(), // ObservedBy
                    parts[5].trim(), // Context
                    parts[6].trim()  // Note
                ));
            }
        }

        setupGraph();
        
        EmotionAdapter adapter = new EmotionAdapter(emotionLogs);
        recyclerView.setAdapter(adapter);
    }

    private void setupGraph() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        
        // Simple aggregation for the last 2 recorded dates
        // In a real app, this would dynamically calculate based on the loaded emotionLogs
        if (emotionLogs.size() >= 2) {
            entries.add(new BarEntry(0, new float[]{1f, 1f, 0f})); 
            entries.add(new BarEntry(1, new float[]{0f, 1f, 1f}));
        } else {
            // Placeholder if not enough data
            entries.add(new BarEntry(0, new float[]{0f, 0f, 0f}));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Emotions (Happy/Calm/Anxious)");
        dataSet.setColors(Color.GREEN, Color.BLUE, Color.RED);
        dataSet.setStackLabels(new String[]{"Happy", "Calm", "Anxious"});

        BarData data = new BarData(dataSet);
        barChart.setData(data);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        
        barChart.getDescription().setEnabled(false);
        barChart.setFitBars(true);
        barChart.invalidate();
    }

    static class EmotionLog {
        String type, intensity, date, time, observer, context, note;

        public EmotionLog(String type, String intensity, String date, String time, String observer, String context, String note) {
            this.type = type;
            this.intensity = intensity;
            this.date = date;
            this.time = time;
            this.observer = observer;
            this.context = context;
            this.note = note;
        }
    }

    class EmotionAdapter extends RecyclerView.Adapter<EmotionAdapter.ViewHolder> {
        private List<EmotionLog> logs;

        public EmotionAdapter(List<EmotionLog> logs) { this.logs = logs; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_emotion_log, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            EmotionLog log = logs.get(position);
            holder.type.setText(log.type);
            holder.dateTime.setText(log.date + " - " + log.time);
            holder.observer.setText("Observed by: " + log.observer);
            holder.intensity.setText("Intensity: " + log.intensity);
            holder.context.setText("Context: " + log.context);
            holder.note.setText("Note: " + log.note);
        }

        @Override
        public int getItemCount() { return logs.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView type, dateTime, observer, intensity, context, note;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                type = itemView.findViewById(R.id.emotionTypeText);
                dateTime = itemView.findViewById(R.id.dateTimeText);
                observer = itemView.findViewById(R.id.observerText);
                intensity = itemView.findViewById(R.id.intensityText);
                context = itemView.findViewById(R.id.contextText);
                note = itemView.findViewById(R.id.noteText);
            }
        }
    }
}

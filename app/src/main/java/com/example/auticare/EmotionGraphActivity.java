package com.example.auticare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;

public class EmotionGraphActivity extends AppCompatActivity {

    BarChart barChart;
    Button viewHistoryBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion_graph);

        barChart = findViewById(R.id.barChart);
        viewHistoryBtn = findViewById(R.id.viewHistoryBtn);

        loadEmotionData();

        viewHistoryBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, EmotionHistoryActivity.class));
        });
    }

    private void loadEmotionData() {

        SharedPreferences prefs = getSharedPreferences("AutiCareData", MODE_PRIVATE);
        String emotions = prefs.getString("emotions", "");

        int happy = count(emotions, "Happy");
        int sad = count(emotions, "Sad");
        int angry = count(emotions, "Angry");
        int calm = count(emotions, "Calm");

        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, happy));
        entries.add(new BarEntry(1, sad));
        entries.add(new BarEntry(2, angry));
        entries.add(new BarEntry(3, calm));

        BarDataSet dataSet = new BarDataSet(entries, "Emotion Count");
        dataSet.setColors(new int[]{
                ContextCompat.getColor(this, android.R.color.holo_green_light),
                ContextCompat.getColor(this, android.R.color.holo_blue_light),
                ContextCompat.getColor(this, android.R.color.holo_red_light),
                ContextCompat.getColor(this, android.R.color.holo_orange_light)
        });

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        XAxis xAxis = barChart.getXAxis();
        final String[] emotionLabels = {"Happy", "Sad", "Angry", "Calm"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(emotionLabels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setLabelCount(emotionLabels.length);

        barChart.getDescription().setEnabled(false);
        barChart.invalidate();
    }

    private int count(String text, String word) {
        int count = 0;
        if (text != null && !text.isEmpty()) {
            String[] arr = text.split("\n");
            for (String s : arr) {
                if (s.trim().equalsIgnoreCase(word)) {
                    count++;
                }
            }
        }
        return count;
    }
}

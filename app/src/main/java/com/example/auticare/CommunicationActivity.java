package com.example.auticare;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommunicationActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private List<CommunicationItem> communicationItems;
    private SharedPreferences prefs;
    
    private TextView lastActionText, timestampText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);

        tts = new TextToSpeech(this, this);
        // Using "CommunicationLogs" for structured history
        prefs = getSharedPreferences("CommunicationLogs", MODE_PRIVATE);

        // Initialize UI components
        lastActionText = findViewById(R.id.lastActionText);
        timestampText = findViewById(R.id.timestampText);
        
        ImageView backBtn = findViewById(R.id.backBtn);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        communicationItems = new ArrayList<>();
        communicationItems.add(new CommunicationItem("Water", R.drawable.comm_water_illus));
        communicationItems.add(new CommunicationItem("Food", R.drawable.comm_food_illus));
        communicationItems.add(new CommunicationItem("Play", R.drawable.comm_play_illus));
        communicationItems.add(new CommunicationItem("Sleep", R.drawable.comm_sleep_illus));
        communicationItems.add(new CommunicationItem("Washroom", R.drawable.comm_washroom_illus));

        GridView gridView = findViewById(R.id.communication_grid);
        CommunicationAdapter adapter = new CommunicationAdapter(this, communicationItems);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener((parent, view, position, id) -> {
            CommunicationItem item = communicationItems.get(position);
            String actionName = item.getName();
            String sentence = getSpeechText(actionName);

            // 1. Update UI Display with current action and exact timestamp
            updateDisplay(actionName);
            
            // 2. Save structured log for History and Dashboards
            saveToHistory(actionName, sentence);
            
            // 3. Audio feedback
            speak(sentence);

            // Reward System Integration
            RewardManager.addStar(this, RewardManager.GAME_LEARN_COMM);
            RewardManager.incrementActivityCount(this, "First Step"); // Badge trigger
        });
    }

    private void updateDisplay(String actionName) {
        if (lastActionText != null && timestampText != null) {
            lastActionText.setText("Last Action: " + actionName);
            
            String currentTime = new SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(new Date());
            timestampText.setText("Date & Time: " + currentTime);
            
            // Visual feedback
            lastActionText.animate().scaleX(1.1f).scaleY(1.1f).setDuration(100).withEndAction(() -> 
                lastActionText.animate().scaleX(1.0f).scaleY(1.0f).setDuration(100).start()
            ).start();
        }
    }

    private void saveToHistory(String buttonType, String sentence) {
        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        
        // Format: Date | Time | Type | Message | Status | ObservedBy
        String logEntry = date + " | " + time + " | Picture | " + sentence + " | Successful | Child";
        
        SharedPreferences.Editor editor = prefs.edit();
        long timestamp = System.currentTimeMillis();
        editor.putString("log_" + timestamp, logEntry);
        
        // Also save as "latest" for Dashboard summary
        editor.putString("latest_log", logEntry);
        editor.apply();
        
        // Maintain compatibility with existing history if needed
        getSharedPreferences("AutiCareData", MODE_PRIVATE).edit()
            .putString("commands", getSharedPreferences("AutiCareData", MODE_PRIVATE).getString("commands", "") + sentence + "\n")
            .apply();
    }

    private String getSpeechText(String itemName) {
        switch (itemName) {
            case "Water": return "I'd like some water please";
            case "Food": return "I'm hungry, may I have some food";
            case "Play": return "I would like to play now";
            case "Sleep": return "I'm feeling sleepy and want to rest";
            case "Washroom": return "I need to go to the washroom";
            default: return "I want " + itemName;
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
        }
    }

    private void speak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}

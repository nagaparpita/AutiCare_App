package com.example.auticare;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EmotionActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextToSpeech tts;
    private Button btnHappy, btnSad, btnAngry, btnCalm, btnAnxious, btnCustomEmotion, btnDeleteEmotion;
    private EditText customEmotionText;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion);

        btnHappy = findViewById(R.id.happyBtn);
        btnSad = findViewById(R.id.sadBtn);
        btnAngry = findViewById(R.id.angryBtn);
        btnCalm = findViewById(R.id.calmBtn);
        btnAnxious = findViewById(R.id.anxiousBtn);
        customEmotionText = findViewById(R.id.customEmotionText);
        btnCustomEmotion = findViewById(R.id.customEmotionBtn);
        btnDeleteEmotion = findViewById(R.id.deleteEmotionBtn);

        prefs = getSharedPreferences("EmotionLogs", MODE_PRIVATE);
        tts = new TextToSpeech(this, this);

        ImageView backBtn = findViewById(R.id.backBtn);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        btnHappy.setOnClickListener(v -> handleEmotion("Happy"));
        btnSad.setOnClickListener(v -> handleEmotion("Sad"));
        btnAngry.setOnClickListener(v -> handleEmotion("Angry"));
        btnCalm.setOnClickListener(v -> handleEmotion("Calm"));
        btnAnxious.setOnClickListener(v -> handleEmotion("Anxious"));

        btnCustomEmotion.setOnClickListener(v -> {
            String text = customEmotionText.getText().toString().trim();
            if (!text.isEmpty()) {
                handleEmotion(text);
                customEmotionText.setText("");
            }
        });

        btnDeleteEmotion.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleEmotion(String emotion) {
        saveEmotion(emotion);
        speak(getEmotionSpeech(emotion));
        HistoryHelper.logActivity("Selected Emotion: " + emotion);
        
        // Reward System Integration
        RewardManager.addStar(this, RewardManager.GAME_EMOTIONS);
        RewardManager.incrementActivityCount(this, RewardManager.COUNTER_EMOTION);

        Toast.makeText(this, emotion + " Saved", Toast.LENGTH_SHORT).show();
    }

    private String getEmotionSpeech(String emotion) {
        switch (emotion) {
            case "Happy": return "I am happy and joyful!";
            case "Sad": return "I'm feeling a little bit down.";
            case "Angry": return "I'm feeling quite angry right now.";
            case "Calm": return "I am feeling very calm and peaceful.";
            case "Anxious": return "I'm feeling a little bit anxious.";
            default: return "I'm feeling " + emotion;
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
        }
    }

    private void saveEmotion(String emotion) {
        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
        
        // Structured Log: Date | Time | Emotion | Intensity | ObservedBy | Context | Note
        String logEntry = date + " | " + time + " | " + emotion + " | Medium | Child | Auto-log | No notes";
        
        SharedPreferences.Editor editor = prefs.edit();
        long timestamp = System.currentTimeMillis();
        editor.putString("log_" + timestamp, logEntry);
        editor.apply();
        
        // For compatibility with Graph
        SharedPreferences graphPrefs = getSharedPreferences("AutiCareData", MODE_PRIVATE);
        String current = graphPrefs.getString("emotions", "");
        graphPrefs.edit().putString("emotions", current + emotion + "\n").apply();
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

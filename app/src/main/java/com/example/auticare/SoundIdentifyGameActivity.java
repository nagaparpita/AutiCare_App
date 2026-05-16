package com.example.auticare;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class SoundIdentifyGameActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private TextView tvProgress, tvFeedback;
    private CardView btnPlaySound;
    private CardView[] optionCards;
    private ImageView[] optionImages;
    private Button btnNext;
    private MediaPlayer mediaPlayer;
    private TextToSpeech tts;

    private List<SoundItem> soundList;
    private SoundItem currentSound;
    private int currentQuestionIndex = 0;
    private int totalQuestions = 10;
    private int score = 0;
    private int level = 1;
    private int replayCount = 0;
    private long startTime;

    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_game);

        userId = FirebaseAuth.getInstance().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        tts = new TextToSpeech(this, this);

        tvProgress = findViewById(R.id.tvProgress);
        tvFeedback = findViewById(R.id.tvFeedback);
        btnPlaySound = findViewById(R.id.btnPlaySound);
        btnNext = findViewById(R.id.btnNext);

        optionCards = new CardView[4];
        optionCards[0] = findViewById(R.id.cardOption1);
        optionCards[1] = findViewById(R.id.cardOption2);
        optionCards[2] = findViewById(R.id.cardOption3);
        optionCards[3] = findViewById(R.id.cardOption4);

        optionImages = new ImageView[4];
        optionImages[0] = findViewById(R.id.ivOption1);
        optionImages[1] = findViewById(R.id.ivOption2);
        optionImages[2] = findViewById(R.id.ivOption3);
        optionImages[3] = findViewById(R.id.ivOption4);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        initSoundList();
        loadNextQuestion();

        btnPlaySound.setOnClickListener(v -> playCurrentSound());

        btnNext.setOnClickListener(v -> {
            if (currentQuestionIndex < totalQuestions) {
                loadNextQuestion();
            } else {
                showResultDialog();
            }
        });
    }

    private void initSoundList() {
        soundList = new ArrayList<>();
        soundList.add(new SoundItem("Dog", getRawResId("dog_bark"), R.drawable.img_dog));
        soundList.add(new SoundItem("Cat", getRawResId("cat_meow"), R.drawable.img_cat));
        soundList.add(new SoundItem("Bell", getRawResId("bell_ring"), R.drawable.img_bell));
        soundList.add(new SoundItem("Rain", getRawResId("rain"), R.drawable.img_rain));
        soundList.add(new SoundItem("Car", getRawResId("car_horn"), R.drawable.img_car));
        soundList.add(new SoundItem("Door", getRawResId("door_knock"), R.drawable.img_door));
    }

    private int getRawResId(String name) {
        int resId = getResources().getIdentifier(name, "raw", getPackageName());
        return resId != 0 ? resId : 0;
    }

    private void loadNextQuestion() {
        startTime = System.currentTimeMillis();
        currentQuestionIndex++;
        replayCount = 0;
        tvProgress.setText(currentQuestionIndex + " / " + totalQuestions);
        tvFeedback.setText("");
        btnNext.setVisibility(View.INVISIBLE);

        if (currentQuestionIndex <= 3) level = 1;
        else if (currentQuestionIndex <= 7) level = 2;
        else level = 3;

        currentSound = soundList.get(new Random().nextInt(soundList.size()));
        
        // Ask the question first
        speak("Listen carefully. What made that sound?");
        
        // Play the actual sound after a short delay to let TTS finish
        new Handler(Looper.getMainLooper()).postDelayed(this::playCurrentSound, 2500);

        int numOptions = (level == 1) ? 2 : (level == 2 ? 3 : 4);
        List<SoundItem> options = new ArrayList<>();
        options.add(currentSound);

        List<SoundItem> others = new ArrayList<>(soundList);
        others.remove(currentSound);
        Collections.shuffle(others);
        for (int i = 0; i < numOptions - 1; i++) {
            options.add(others.get(i));
        }
        Collections.shuffle(options);

        for (int i = 0; i < 4; i++) {
            if (i < numOptions) {
                optionCards[i].setVisibility(View.VISIBLE);
                optionImages[i].setImageResource(options.get(i).imageResId);
                optionCards[i].setEnabled(true);
                final SoundItem selected = options.get(i);
                optionCards[i].setOnClickListener(v -> checkAnswer(selected));
            } else {
                optionCards[i].setVisibility(View.GONE);
            }
        }
    }

    private void playCurrentSound() {
        if (currentSound.soundResId == 0) {
            Toast.makeText(this, "Sound file missing: " + currentSound.name, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        replayCount++;
        mediaPlayer = MediaPlayer.create(this, currentSound.soundResId);
        mediaPlayer.start();
    }

    private void checkAnswer(SoundItem selected) {
        boolean isCorrect = selected.name.equals(currentSound.name);
        long timeTaken = (System.currentTimeMillis() - startTime) / 1000;

        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }

        for (CardView card : optionCards) card.setEnabled(false);

        if (isCorrect) {
            score++;
            tvFeedback.setText("Well done! ⭐");
            tvFeedback.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            speak("Great job! That is correct.");
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (currentQuestionIndex < totalQuestions) {
                    loadNextQuestion();
                } else {
                    showResultDialog();
                }
            }, 2000);
        } else {
            tvFeedback.setText("Try again!");
            tvFeedback.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            speak("Not quite. Listen again.");
            btnNext.setVisibility(View.VISIBLE);
        }

        saveGameData(selected.name, currentSound.name, isCorrect, timeTaken);
    }

    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            tts.setLanguage(Locale.US);
        }
    }

    private void saveGameData(String selected, String correct, boolean isCorrect, long timeTaken) {
        if (userId == null) return;
        String key = mDatabase.child("game_reports").child(userId).child("sound_identify").push().getKey();
        Map<String, Object> data = new HashMap<>();
        data.put("soundId", correct);
        data.put("selectedImageId", selected);
        data.put("correctImageId", correct);
        data.put("isCorrect", isCorrect);
        data.put("replayCount", replayCount);
        data.put("timeTaken", timeTaken);
        data.put("date", System.currentTimeMillis());
        data.put("level", level);
        if (key != null) mDatabase.child("game_reports").child(userId).child("sound_identify").child(key).setValue(data);
    }

    private void showResultDialog() {
        RewardManager.addStars(this, score);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_game_result, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();

        ((TextView) dialogView.findViewById(R.id.tvFinalScore)).setText("You earned " + score + " stars!");
        dialogView.findViewById(R.id.btnRestart).setOnClickListener(v -> {
            score = 0;
            currentQuestionIndex = 0;
            loadNextQuestion();
            dialog.dismiss();
        });
        dialogView.findViewById(R.id.tvGoBack).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }

    private static class SoundItem {
        String name;
        int soundResId;
        int imageResId;

        SoundItem(String name, int soundResId, int imageResId) {
            this.name = name;
            this.soundResId = soundResId;
            this.imageResId = imageResId;
        }
    }
}

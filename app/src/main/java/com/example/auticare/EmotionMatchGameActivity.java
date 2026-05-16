package com.example.auticare;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

public class EmotionMatchGameActivity extends AppCompatActivity {

    private ImageView ivEmotion;
    private TextView tvProgress, tvFeedback, tvScore;
    private Button[] optionButtons;
    private Button btnNext;
    private TextToSpeech tts;

    private List<Emotion> emotionList;
    private Emotion currentEmotion;
    private int currentQuestionIndex = 0;
    private int totalQuestions = 10;
    private int score = 0;
    private int level = 1;
    private long startTime;

    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emotion_game);

        userId = FirebaseAuth.getInstance().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        ivEmotion = findViewById(R.id.ivEmotion);
        tvProgress = findViewById(R.id.tvProgress);
        tvFeedback = findViewById(R.id.tvFeedback);
        tvScore = findViewById(R.id.tvScore);
        btnNext = findViewById(R.id.btnNext);

        optionButtons = new Button[4];
        optionButtons[0] = findViewById(R.id.option1);
        optionButtons[1] = findViewById(R.id.option2);
        optionButtons[2] = findViewById(R.id.option3);
        optionButtons[3] = findViewById(R.id.option4);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        initEmotionList();
        initTTS();
        
        level = 1; 

        loadNextQuestion();

        btnNext.setOnClickListener(v -> {
            if (currentQuestionIndex < totalQuestions) {
                loadNextQuestion();
            } else {
                showResultDialog();
            }
        });
    }

    private void initEmotionList() {
        emotionList = new ArrayList<>();
        emotionList.add(new Emotion("Happy", R.drawable.emotion_happy));
        emotionList.add(new Emotion("Sad", R.drawable.emotion_sad));
        emotionList.add(new Emotion("Angry", R.drawable.emotion_angry));
        emotionList.add(new Emotion("Surprised", R.drawable.emotion_surprised));
        emotionList.add(new Emotion("Scared", R.drawable.emotion_scared));
        emotionList.add(new Emotion("Calm", R.drawable.emotion_calm));
    }

    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    private void loadNextQuestion() {
        startTime = System.currentTimeMillis();
        currentQuestionIndex++;
        tvProgress.setText(currentQuestionIndex + " / " + totalQuestions);
        tvFeedback.setText("");
        btnNext.setVisibility(View.INVISIBLE);

        Random random = new Random();
        level = random.nextInt(3) + 1;

        currentEmotion = emotionList.get(random.nextInt(emotionList.size()));
        ivEmotion.setImageResource(currentEmotion.drawableResId);

        int numOptions = getNumOptionsForLevel();
        List<String> options = new ArrayList<>();
        options.add(currentEmotion.name);

        List<Emotion> otherEmotions = new ArrayList<>(emotionList);
        otherEmotions.remove(currentEmotion);
        Collections.shuffle(otherEmotions);

        for (int i = 0; i < numOptions - 1; i++) {
            options.add(otherEmotions.get(i).name);
        }
        Collections.shuffle(options);

        for (int i = 0; i < 4; i++) {
            if (i < numOptions) {
                optionButtons[i].setVisibility(View.VISIBLE);
                optionButtons[i].setText(options.get(i));
                optionButtons[i].setEnabled(true);
                optionButtons[i].setOnClickListener(v -> checkAnswer(((Button) v).getText().toString()));
            } else {
                optionButtons[i].setVisibility(View.GONE);
            }
        }
    }

    private int getNumOptionsForLevel() {
        if (level == 1) return 2;
        if (level == 2) return 3;
        return 4;
    }

    private void checkAnswer(String selectedAnswer) {
        boolean isCorrect = selectedAnswer.equals(currentEmotion.name);
        long timeTaken = System.currentTimeMillis() - startTime;

        speak(selectedAnswer);

        for (Button btn : optionButtons) {
            btn.setEnabled(false);
        }

        if (isCorrect) {
            score++;
            tvScore.setText("Score: " + score);
            tvFeedback.setText("Well done! ⭐");
            tvFeedback.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (currentQuestionIndex < totalQuestions) {
                    loadNextQuestion();
                } else {
                    showResultDialog();
                }
            }, 1500);
        } else {
            tvFeedback.setText("Try again!");
            tvFeedback.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            btnNext.setVisibility(View.VISIBLE);
        }

        saveGameData(selectedAnswer, currentEmotion.name, isCorrect, timeTaken);
    }

    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "EmotionUtterance");
        }
    }

    private void saveGameData(String selected, String correct, boolean isCorrect, long timeTaken) {
        if (userId == null) return;

        String key = mDatabase.child("game_reports").child(userId).child("emotion_match").push().getKey();
        Map<String, Object> data = new HashMap<>();
        data.put("emotionId", correct);
        data.put("selectedAnswer", selected);
        data.put("correctAnswer", correct);
        data.put("isCorrect", isCorrect);
        data.put("timeTaken", timeTaken);
        data.put("date", System.currentTimeMillis());
        data.put("level", level);

        if (key != null) {
            mDatabase.child("game_reports").child(userId).child("emotion_match").child(key).setValue(data);
        }
    }

    private void showResultDialog() {
        // Track stars for this specific game
        RewardManager.addGameStars(this, RewardManager.KEY_EMOTION, score);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_game_result, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        TextView tvFinalScore = dialogView.findViewById(R.id.tvFinalScore);
        tvFinalScore.setText("You earned " + score + " stars!");

        dialogView.findViewById(R.id.btnRestart).setOnClickListener(v -> {
            resetGame();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.tvGoBack).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        
        dialog.show();
    }

    private void resetGame() {
        score = 0;
        currentQuestionIndex = 0;
        tvScore.setText("Score: 0");
        loadNextQuestion();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private static class Emotion {
        String name;
        int drawableResId;

        Emotion(String name, int drawableResId) {
            this.name = name;
            this.drawableResId = drawableResId;
        }
    }
}

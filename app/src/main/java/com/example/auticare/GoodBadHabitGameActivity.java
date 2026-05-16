package com.example.auticare;

import android.app.AlertDialog;
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

public class GoodBadHabitGameActivity extends AppCompatActivity {

    private ImageView ivHabit;
    private TextView tvProgress, tvFeedback;
    private Button btnGood, btnBad, btnNext;
    private TextToSpeech tts;

    private List<Habit> habitList;
    private Habit currentHabit;
    private int currentQuestionIndex = 0;
    private int totalQuestions = 5;
    private int score = 0;
    private long startTime;

    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_game);

        userId = FirebaseAuth.getInstance().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        ivHabit = findViewById(R.id.ivHabit);
        tvProgress = findViewById(R.id.tvProgress);
        tvFeedback = findViewById(R.id.tvFeedback);
        btnGood = findViewById(R.id.btnGoodHabit);
        btnBad = findViewById(R.id.btnBadHabit);
        btnNext = findViewById(R.id.btnNext);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        initHabitList();
        initTTS();
        loadNextQuestion();

        btnGood.setOnClickListener(v -> checkAnswer(true));
        btnBad.setOnClickListener(v -> checkAnswer(false));

        btnNext.setOnClickListener(v -> {
            if (currentQuestionIndex < totalQuestions) {
                loadNextQuestion();
            } else {
                showResultDialog();
            }
        });
    }

    private void initHabitList() {
        habitList = new ArrayList<>();
        
        // Good Habits
        habitList.add(new Habit(1, getResId("habit_knock_door"), true, "Knocking on Door", "Knocking before entering is a good habit."));
        habitList.add(new Habit(2, getResId("habit_brush_teeth"), true, "Brushing Teeth", "Brushing teeth keeps them clean. It is a good habit."));
        habitList.add(new Habit(3, getResId("habit_wash_hands"), true, "Washing Hands", "Washing hands keeps germs away. It is a good habit."));
        habitList.add(new Habit(4, getResId("habit_share_toys"), true, "Sharing Toys", "Sharing toys with friends is a good habit."));
        
        // Bad Habits
        habitList.add(new Habit(5, getResId("habit_throw_trash"), false, "Littering", "Throwing trash on the floor is a bad habit."));
        habitList.add(new Habit(6, getResId("habit_hit_others"), false, "Hitting Others", "Hitting others hurts them. It is a bad habit."));
        habitList.add(new Habit(7, getResId("habit_break_toys"), false, "Breaking Toys", "Breaking your toys is a bad habit."));

        Collections.shuffle(habitList);
    }

    private int getResId(String name) {
        int resId = getResources().getIdentifier(name, "drawable", getPackageName());
        // Return a placeholder if the specific image isn't found yet
        return resId != 0 ? resId : R.drawable.ic_smiley_face;
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
        btnGood.setEnabled(true);
        btnBad.setEnabled(true);

        currentHabit = habitList.get(currentQuestionIndex % habitList.size());
        ivHabit.setImageResource(currentHabit.imageResId);
        
        speak(currentHabit.voiceText + " Is it good or bad?");
    }

    private void checkAnswer(boolean selectedGood) {
        boolean isCorrect = (selectedGood == currentHabit.isGood);
        long timeTaken = (System.currentTimeMillis() - startTime) / 1000;

        btnGood.setEnabled(false);
        btnBad.setEnabled(false);

        if (isCorrect) {
            score++;
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

        saveGameData(selectedGood, isCorrect, timeTaken);
    }

    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "HabitUtterance");
        }
    }

    private void saveGameData(boolean selectedGood, boolean isCorrect, long timeTaken) {
        if (userId == null) return;
        String key = mDatabase.child("game_reports").child(userId).child("habit_sort").push().getKey();
        Map<String, Object> data = new HashMap<>();
        data.put("habitTitle", currentHabit.title);
        data.put("selectedType", selectedGood ? "Good" : "Bad");
        data.put("isCorrect", isCorrect);
        data.put("timeTaken", timeTaken);
        data.put("date", System.currentTimeMillis());
        if (key != null) mDatabase.child("game_reports").child(userId).child("habit_sort").child(key).setValue(data);
    }

    private void showResultDialog() {
        RewardManager.addStars(this, score * 3);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_game_result, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();

        ((TextView) dialogView.findViewById(R.id.tvResultTitle)).setText("Awesome Job!");
        ((TextView) dialogView.findViewById(R.id.tvFinalScore)).setText("You learned " + score + " habits and earned " + (score * 3) + " stars!");
        
        dialogView.findViewById(R.id.btnRestart).setOnClickListener(v -> {
            score = 0;
            currentQuestionIndex = 0;
            Collections.shuffle(habitList);
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
        super.onDestroy();
    }
}

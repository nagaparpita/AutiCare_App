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

public class ObjectFinderGameActivity extends AppCompatActivity {

    private TextView tvFindText, tvProgress, tvFeedback;
    private ImageView ivTargetIcon;
    private CardView[] objectCards;
    private ImageView[] objectImages;
    private Button btnNext;
    private TextToSpeech tts;

    private List<ObjectItem> allObjects;
    private ObjectItem targetObject;
    private int currentQuestionIndex = 0;
    private int totalQuestions = 10;
    private int score = 0;
    private long startTime;

    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_object_finder);

        userId = FirebaseAuth.getInstance().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        tvFindText = findViewById(R.id.tvFindText);
        tvProgress = findViewById(R.id.tvProgress);
        tvFeedback = findViewById(R.id.tvFeedback);
        ivTargetIcon = findViewById(R.id.ivTargetIcon);
        btnNext = findViewById(R.id.btnNext);

        // Updated to match 4 cards in XML
        objectCards = new CardView[4];
        objectCards[0] = findViewById(R.id.card1);
        objectCards[1] = findViewById(R.id.card2);
        objectCards[2] = findViewById(R.id.card3);
        objectCards[3] = findViewById(R.id.card4);

        objectImages = new ImageView[4];
        objectImages[0] = findViewById(R.id.iv1);
        objectImages[1] = findViewById(R.id.iv2);
        objectImages[2] = findViewById(R.id.iv3);
        objectImages[3] = findViewById(R.id.iv4);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        initObjectList();
        initTTS();
        loadNextQuestion();

        btnNext.setOnClickListener(v -> {
            if (currentQuestionIndex < totalQuestions) {
                loadNextQuestion();
            } else {
                showResultDialog();
            }
        });
    }

    private void initObjectList() {
        allObjects = new ArrayList<>();
        allObjects.add(new ObjectItem("Dog", R.drawable.img_dog));
        allObjects.add(new ObjectItem("Cat", R.drawable.img_cat));
        allObjects.add(new ObjectItem("Car", R.drawable.img_car));
        allObjects.add(new ObjectItem("Bell", R.drawable.img_bell));
        allObjects.add(new ObjectItem("Door", R.drawable.img_door));
        allObjects.add(new ObjectItem("Rain", R.drawable.img_rain));
        allObjects.add(new ObjectItem("Pencil", R.drawable.img_pencil));
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

        // Pick a random target
        targetObject = allObjects.get(new Random().nextInt(allObjects.size()));
        tvFindText.setText("Find the " + targetObject.name);
        ivTargetIcon.setImageResource(targetObject.imageResId);
        ivTargetIcon.setVisibility(View.VISIBLE);

        // Prepare 4 options including the target
        List<ObjectItem> options = new ArrayList<>();
        options.add(targetObject);

        List<ObjectItem> others = new ArrayList<>(allObjects);
        others.remove(targetObject);
        Collections.shuffle(others);
        
        // Add 3 other unique objects
        for (int i = 0; i < 3; i++) {
            options.add(others.get(i));
        }
        
        // Shuffle the options so target is in a random position
        Collections.shuffle(options);

        // Update UI
        for (int i = 0; i < 4; i++) {
            objectImages[i].setImageResource(options.get(i).imageResId);
            objectImages[i].setVisibility(View.VISIBLE);
            objectCards[i].setVisibility(View.VISIBLE);
            objectCards[i].setEnabled(true);
            final ObjectItem selected = options.get(i);
            objectCards[i].setOnClickListener(v -> checkAnswer(selected));
        }

        speak("Find the " + targetObject.name);
    }

    private void checkAnswer(ObjectItem selected) {
        boolean isCorrect = selected != null && selected.name.equals(targetObject.name);
        long timeTaken = (System.currentTimeMillis() - startTime) / 1000;

        if (isCorrect) {
            score++;
            tvFeedback.setText("Great Job! ⭐");
            tvFeedback.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            speak("Well done! You found the " + targetObject.name);
            
            for (CardView card : objectCards) card.setEnabled(false);
            
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
            speak("Oops! That's not the " + targetObject.name + ". Try again!");
        }

        saveGameData(selected != null ? selected.name : "None", targetObject.name, isCorrect, timeTaken);
    }

    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ObjectUtterance");
        }
    }

    private void saveGameData(String selected, String target, boolean isCorrect, long timeTaken) {
        if (userId == null) return;
        String key = mDatabase.child("game_reports").child(userId).child("object_finder").push().getKey();
        Map<String, Object> data = new HashMap<>();
        data.put("targetObject", target);
        data.put("selectedObject", selected);
        data.put("isCorrect", isCorrect);
        data.put("timeTaken", timeTaken);
        data.put("date", System.currentTimeMillis());
        if (key != null) mDatabase.child("game_reports").child(userId).child("object_finder").child(key).setValue(data);
    }

    private void showResultDialog() {
        RewardManager.addStars(this, score);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_game_result, null);
        builder.setView(dialogView);
        builder.setCancelable(false);
        AlertDialog dialog = builder.create();

        ((TextView) dialogView.findViewById(R.id.tvResultTitle)).setText("Amazing!");
        ((TextView) dialogView.findViewById(R.id.tvFinalScore)).setText("You found " + score + " objects and earned " + score + " stars!");

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
        super.onDestroy();
    }
}

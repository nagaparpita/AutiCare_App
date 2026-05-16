package com.example.auticare;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

public class ColorShapeSortGameActivity extends AppCompatActivity {

    private ImageView ivDraggable;
    private LinearLayout[] targetLayouts;
    private ImageView[] targetImages;
    private TextView[] targetTexts;
    private TextView tvInstruction, tvProgress, tvFeedback, tvScore;
    private Button btnNext;
    private TextToSpeech tts;

    private enum GameMode { COLOR, SHAPE }
    private GameMode currentMode;
    private int currentLevel = 1;
    private int currentQuestionIndex = 0;
    private int totalQuestions = 5;
    private int score = 0;
    private long startTime;

    private String[] colors = {"Red", "Blue", "Green", "Yellow"};
    private int[] colorValues = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};
    private String[] shapes = {"Circle", "Square", "Triangle", "Star"};
    private int[] shapeDrawables = {R.drawable.shape_circle, R.drawable.shape_square, R.drawable.shape_triangle, R.drawable.ic_star};

    private String targetProperty; // The color or shape we are looking for
    private int currentObjectColor;
    private int currentObjectShapeRes;
    private String currentObjectName;

    private DatabaseReference mDatabase;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_shape_game);

        userId = FirebaseAuth.getInstance().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        ivDraggable = findViewById(R.id.ivDraggable);
        tvInstruction = findViewById(R.id.tvInstruction);
        tvProgress = findViewById(R.id.tvProgress);
        tvFeedback = findViewById(R.id.tvFeedback);
        tvScore = findViewById(R.id.tvScore);
        btnNext = findViewById(R.id.btnNext);

        targetLayouts = new LinearLayout[4];
        targetLayouts[0] = findViewById(R.id.target1);
        targetLayouts[1] = findViewById(R.id.target2);
        targetLayouts[2] = findViewById(R.id.target3);
        targetLayouts[3] = findViewById(R.id.target4);

        targetImages = new ImageView[4];
        targetImages[0] = findViewById(R.id.ivTarget1);
        targetImages[1] = findViewById(R.id.ivTarget2);
        targetImages[2] = findViewById(R.id.ivTarget3);
        targetImages[3] = findViewById(R.id.ivTarget4);

        targetTexts = new TextView[4];
        targetTexts[0] = findViewById(R.id.tvTarget1);
        targetTexts[1] = findViewById(R.id.tvTarget2);
        targetTexts[2] = findViewById(R.id.tvTarget3);
        targetTexts[3] = findViewById(R.id.tvTarget4);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        initTTS();
        setupDragAndDrop();
        
        // Start with random mode
        currentMode = new Random().nextBoolean() ? GameMode.COLOR : GameMode.SHAPE;
        loadNextQuestion();

        btnNext.setOnClickListener(v -> {
            if (currentQuestionIndex < totalQuestions) {
                loadNextQuestion();
            } else {
                showResultDialog();
            }
        });
    }

    private void initTTS() {
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });
    }

    private void setupDragAndDrop() {
        ivDraggable.setOnLongClickListener(v -> {
            ClipData.Item item = new ClipData.Item(currentObjectName);
            ClipData dragData = new ClipData(currentObjectName, new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN}, item);
            View.DragShadowBuilder myShadow = new View.DragShadowBuilder(ivDraggable);
            v.startDragAndDrop(dragData, myShadow, null, 0);
            return true;
        });

        for (int i = 0; i < 4; i++) {
            final int index = i;
            targetLayouts[i].setOnDragListener((v, event) -> {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        return event.getClipDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN);
                    case DragEvent.ACTION_DRAG_ENTERED:
                        v.setBackground(ContextCompat.getDrawable(this, R.drawable.role_card_background));
                        return true;
                    case DragEvent.ACTION_DRAG_EXITED:
                        v.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_background));
                        return true;
                    case DragEvent.ACTION_DROP:
                        checkAnswer(targetTexts[index].getText().toString());
                        return true;
                    case DragEvent.ACTION_DRAG_ENDED:
                        v.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_text_background));
                        return true;
                }
                return false;
            });
        }
    }

    private void loadNextQuestion() {
        startTime = System.currentTimeMillis();
        currentQuestionIndex++;
        tvProgress.setText(currentQuestionIndex + " / " + totalQuestions);
        tvFeedback.setText("");
        btnNext.setVisibility(View.INVISIBLE);
        ivDraggable.setVisibility(View.VISIBLE);

        // Determine Level (1 to 3)
        if (currentQuestionIndex <= 2) currentLevel = 1;
        else if (currentQuestionIndex <= 4) currentLevel = 2;
        else currentLevel = 3;

        int numOptions = currentLevel + 1;
        
        // Alternate modes or stay consistent? Let's stay consistent for a few then switch
        if (currentQuestionIndex % 3 == 0) {
            currentMode = (currentMode == GameMode.COLOR) ? GameMode.SHAPE : GameMode.COLOR;
        }

        Random random = new Random();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < 4; i++) indices.add(i);
        Collections.shuffle(indices);

        if (currentMode == GameMode.COLOR) {
            tvInstruction.setText("Sort by Color!");
            int correctIdx = indices.get(0);
            targetProperty = colors[correctIdx];
            
            // Randomly choose a shape for the colored object
            int randomShapeIdx = random.nextInt(4);
            currentObjectShapeRes = shapeDrawables[randomShapeIdx];
            currentObjectColor = colorValues[correctIdx];
            currentObjectName = colors[correctIdx] + " " + shapes[randomShapeIdx];

            ivDraggable.setImageResource(currentObjectShapeRes);
            ivDraggable.setColorFilter(currentObjectColor);

            for (int i = 0; i < 4; i++) {
                if (i < numOptions) {
                    targetLayouts[i].setVisibility(View.VISIBLE);
                    targetTexts[i].setText(colors[indices.get(i)]);
                    targetImages[i].setImageResource(R.drawable.ic_puzzle);
                    targetImages[i].setColorFilter(colorValues[indices.get(i)]);
                } else {
                    targetLayouts[i].setVisibility(View.GONE);
                }
            }
        } else {
            tvInstruction.setText("Sort by Shape!");
            int correctIdx = indices.get(0);
            targetProperty = shapes[correctIdx];

            // Randomly choose a color for the shape object
            int randomColorIdx = random.nextInt(4);
            currentObjectColor = colorValues[randomColorIdx];
            currentObjectShapeRes = shapeDrawables[correctIdx];
            currentObjectName = colors[randomColorIdx] + " " + shapes[correctIdx];

            ivDraggable.setImageResource(currentObjectShapeRes);
            ivDraggable.setColorFilter(currentObjectColor);

            for (int i = 0; i < 4; i++) {
                if (i < numOptions) {
                    targetLayouts[i].setVisibility(View.VISIBLE);
                    targetTexts[i].setText(shapes[indices.get(i)]);
                    targetImages[i].setImageResource(shapeDrawables[indices.get(i)]);
                    targetImages[i].setColorFilter(Color.GRAY); // Neutral color for shape targets
                } else {
                    targetLayouts[i].setVisibility(View.GONE);
                }
            }
        }
        
        speak(currentObjectName);
    }

    private void checkAnswer(String droppedOn) {
        boolean isCorrect = droppedOn.equals(targetProperty);
        long timeTaken = System.currentTimeMillis() - startTime;

        if (isCorrect) {
            score++;
            tvScore.setText("Score: " + score);
            tvFeedback.setText("Good job! ⭐");
            tvFeedback.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            ivDraggable.setVisibility(View.INVISIBLE);
            speak("Correct! " + droppedOn);

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
            speak("Oops, try again");
        }

        saveGameData(droppedOn, targetProperty, isCorrect, timeTaken);
    }

    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "GameUtterance");
        }
    }

    private void saveGameData(String selected, String correct, boolean isCorrect, long timeTaken) {
        if (userId == null) return;

        String key = mDatabase.child("game_reports").child(userId).child("color_shape_sort").push().getKey();
        Map<String, Object> data = new HashMap<>();
        data.put("mode", currentMode.name());
        data.put("selectedCategory", selected);
        data.put("correctCategory", correct);
        data.put("isCorrect", isCorrect);
        data.put("timeTaken", timeTaken);
        data.put("date", System.currentTimeMillis());
        data.put("level", currentLevel);

        if (key != null) {
            mDatabase.child("game_reports").child(userId).child("color_shape_sort").child(key).setValue(data);
        }
    }

    private void showResultDialog() {
        RewardManager.addStars(this, score * 2); // Double stars for sorting game

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_game_result, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();

        TextView tvFinalScore = dialogView.findViewById(R.id.tvFinalScore);
        tvFinalScore.setText("You earned " + (score * 2) + " stars!");

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
}

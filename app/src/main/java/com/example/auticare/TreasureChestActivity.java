package com.example.auticare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class TreasureChestActivity extends AppCompatActivity {

    private ImageView profileBtnTop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_treasure_chest);

        profileBtnTop = findViewById(R.id.profileBtnTop);
        if (profileBtnTop != null) {
            profileBtnTop.setOnClickListener(v -> {
                startActivity(new Intent(this, ChildProfileActivity.class));
            });
        }

        ImageView backBtn = findViewById(R.id.backBtn);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        // 1. Emotion Match Game
        findViewById(R.id.gameEmotionMatch).setOnClickListener(v -> {
            Intent intent = new Intent(TreasureChestActivity.this, EmotionMatchGameActivity.class);
            startActivity(intent);
        });

        // 2. Color & Shape Sort Game
        findViewById(R.id.gameColorShapeSort).setOnClickListener(v -> {
            Intent intent = new Intent(TreasureChestActivity.this, ColorShapeSortGameActivity.class);
            startActivity(intent);
        });

        // 3. Sound Identify Game
        findViewById(R.id.gameSoundIdentify).setOnClickListener(v -> {
            Intent intent = new Intent(TreasureChestActivity.this, SoundIdentifyGameActivity.class);
            startActivity(intent);
        });

        // 4. Picture Memory Flip Game
        findViewById(R.id.gameMemoryFlip).setOnClickListener(v -> {
            Intent intent = new Intent(TreasureChestActivity.this, MemoryMatchGameActivity.class);
            startActivity(intent);
        });

        // 5. Good Habit - Bad Habit Game
        findViewById(R.id.gameHabitSort).setOnClickListener(v -> {
            Intent intent = new Intent(TreasureChestActivity.this, GoodBadHabitGameActivity.class);
            startActivity(intent);
        });

        // 6. Object Finder Game
        findViewById(R.id.gameObjectFinder).setOnClickListener(v -> {
            Intent intent = new Intent(TreasureChestActivity.this, ObjectFinderGameActivity.class);
            startActivity(intent);
        });
    }
}

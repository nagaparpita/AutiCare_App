package com.example.auticare;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class RewardActivity extends AppCompatActivity {

    private TextView starCountText, tvChestStatus, tvSpecialPrizeTitle, tvAppTitle;
    private TextView progressLearnComm, progressEmotions, progressRoutine, progressFocus;
    private ImageView ivTreasureChest, ivSpecialReward;
    private CardView cvSpecialReward;
    
    // Stickers
    private ImageView stickerRainbow, stickerDog, stickerCar, stickerStar, stickerCat, stickerApple;
    
    // Badges
    private ImageView badgeFirstStep, badgeEmotionMaster, badgeHabitStar, badgeFocusChampion;
    
    private TextToSpeech tts;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reward);

        userId = FirebaseAuth.getInstance().getUid();

        // Initialize UI
        tvAppTitle = findViewById(R.id.app_title);
        starCountText = findViewById(R.id.star_count);
        tvChestStatus = findViewById(R.id.tvChestStatus);
        ivTreasureChest = findViewById(R.id.ivTreasureChest);
        
        // Game Progress Texts
        progressLearnComm = findViewById(R.id.progressEmotion);
        progressEmotions = findViewById(R.id.progressColorShape);
        progressRoutine = findViewById(R.id.progressSound);
        progressFocus = findViewById(R.id.progressMemory);
        
        // Hide unused legacy progress views
        if (findViewById(R.id.progressHabit) != null) findViewById(R.id.progressHabit).setVisibility(View.GONE);
        if (findViewById(R.id.progressObject) != null) findViewById(R.id.progressObject).setVisibility(View.GONE);

        // Special Reward
        tvSpecialPrizeTitle = findViewById(R.id.tvSpecialPrizeTitle);
        cvSpecialReward = findViewById(R.id.cvSpecialReward);
        ivSpecialReward = findViewById(R.id.ivSpecialReward);
        
        // Stickers
        stickerRainbow = findViewById(R.id.stickerRainbow);
        stickerDog = findViewById(R.id.stickerPuppy);
        stickerCar = findViewById(R.id.stickerCar);
        stickerStar = findViewById(R.id.stickerStar);
        stickerCat = findViewById(R.id.stickerCat);
        stickerApple = findViewById(R.id.stickerApple);
        
        // Badges
        badgeFirstStep = findViewById(R.id.badgeFirstStep);
        badgeEmotionMaster = findViewById(R.id.badgeEmotionMaster);
        badgeHabitStar = findViewById(R.id.badgeHabitStar);
        badgeFocusChampion = findViewById(R.id.badgeFocusChampion);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());

        // Hidden Reset Feature: Long click on "AutiCare" title to lock everything
        if (tvAppTitle != null) {
            tvAppTitle.setOnLongClickListener(v -> {
                showResetDialog();
                return true;
            });
        }

        // Setup TTS
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        updateUI();

        if (ivTreasureChest != null) {
            ivTreasureChest.setOnClickListener(v -> handleChestClick());
        }

        if (cvSpecialReward != null) {
            cvSpecialReward.setOnClickListener(v -> handleDoorClick());
        }
    }

    private void showResetDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Reset Reward Progress?")
            .setMessage("This will lock all stickers and badges and reset your stars to zero. Are you sure?")
            .setPositiveButton("Reset Everything", (dialog, which) -> {
                DataManager.refreshAllData(this);
                updateUI();
                Toast.makeText(this, "Progress Reset!", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void handleDoorClick() {
        if (RewardManager.isDoorActive(this)) {
            speak("Knock knock! You found a hidden prize. Great job buddy!");
            
            cvSpecialReward.animate().rotationBy(10).setDuration(100).withEndAction(() -> 
                cvSpecialReward.animate().rotationBy(-20).setDuration(100).withEndAction(() -> 
                    cvSpecialReward.animate().rotation(0).setDuration(100).start()
                ).start()
            ).start();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Knock Knock!");
            builder.setMessage("You found a hidden prize.\nGreat job buddy!\n\n✨ Special reward found ✨");
            builder.setPositiveButton("Yay!", (dialog, which) -> {
                RewardManager.consumeDoorReward(this);
                NotificationHelper.logNotification(userId, "Parent", userId, "Reward Milestone", "Special hidden reward found! 🎉", "Reward");
                updateUI();
            });
            builder.show();
        } else {
            speak("Unlock a new sticker to open this door!");
            Toast.makeText(this, "Unlock a sticker first!", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleChestClick() {
        int available = RewardManager.getAvailableChests(this);
        if (available > 0) {
            openChestWithAnimation();
        } else {
            speak("Keep playing to earn more stars for the chest!");
            Toast.makeText(this, "Earn 3 stars to open a chest!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        // Update Star Count UI
        int totalStars = getSharedPreferences("AutiCareRewardData", MODE_PRIVATE).getInt("total_stars_earned", 0);
        if (starCountText != null) {
            starCountText.setText(totalStars + (totalStars == 1 ? " Star" : " Stars"));
        }

        // 1. Update Game Progress
        updateProgressText(progressLearnComm, "Learn & Comm", RewardManager.GAME_LEARN_COMM);
        updateProgressText(progressEmotions, "Emotions", RewardManager.GAME_EMOTIONS);
        updateProgressText(progressRoutine, "Daily Routine", RewardManager.GAME_ROUTINE);
        updateProgressText(progressFocus, "Focus", RewardManager.GAME_FOCUS);

        // 2. Stickers
        updateSticker(stickerRainbow, "Rainbow");
        updateSticker(stickerDog, "Dog");
        updateSticker(stickerCar, "Car");
        updateSticker(stickerStar, "Star");
        updateSticker(stickerCat, "Cat");
        updateSticker(stickerApple, "Apple");

        // 3. Special Door
        if (RewardManager.isDoorActive(this)) {
            unlockItem(ivSpecialReward);
            if (cvSpecialReward != null) cvSpecialReward.setCardBackgroundColor(Color.parseColor("#FFFFFF"));
        } else {
            lockItem(ivSpecialReward);
            if (cvSpecialReward != null) cvSpecialReward.setCardBackgroundColor(Color.parseColor("#F5F5F5"));
        }

        // 4. Badges
        updateBadge(badgeFirstStep, "First Step");
        updateBadge(badgeEmotionMaster, "Emotion Master");
        updateBadge(badgeHabitStar, "Good Habits Star");
        updateBadge(badgeFocusChampion, "Focus Champion");

        // 5. Chest Status
        int available = RewardManager.getAvailableChests(this);
        if (tvChestStatus != null) {
            if (available > 0) {
                tvChestStatus.setText(available + " Chest(s) Ready!");
                tvChestStatus.setTextColor(Color.parseColor("#4CAF50"));
            } else {
                tvChestStatus.setText("Earn 3 stars to open!");
                tvChestStatus.setTextColor(Color.GRAY);
            }
        }
    }

    private void updateProgressText(TextView tv, String label, String gameId) {
        if (tv == null) return;
        int stars = RewardManager.getStarsForGame(this, gameId);
        StringBuilder sb = new StringBuilder(label + " – ");
        for (int i = 0; i < 5; i++) {
            sb.append(i < stars ? "⭐" : "☆");
        }
        tv.setText(sb.toString());
    }

    private void updateSticker(ImageView iv, String name) {
        if (iv == null) return;
        if (RewardManager.isStickerUnlocked(this, name)) {
            unlockItem(iv);
        } else {
            lockItem(iv);
        }
    }

    private void updateBadge(ImageView iv, String name) {
        if (iv == null) return;
        if (RewardManager.isBadgeUnlocked(this, name)) {
            unlockItem(iv);
        } else {
            lockItem(iv);
        }
    }

    private void unlockItem(ImageView iv) {
        if (iv == null) return;
        iv.clearColorFilter();
        iv.setAlpha(1.0f);
    }

    private void lockItem(ImageView iv) {
        if (iv == null) return;
        ColorMatrix matrix = new ColorMatrix();
        matrix.setSaturation(0);
        iv.setColorFilter(new ColorMatrixColorFilter(matrix));
        iv.setAlpha(0.3f);
    }

    private void openChestWithAnimation() {
        speak("Yay!");
        if (ivTreasureChest != null) {
            ivTreasureChest.animate().rotation(360).setDuration(1000).withEndAction(() -> {
                ivTreasureChest.setRotation(0);
                RewardManager.openChest(this);
                NotificationHelper.logNotification(userId, "Parent", userId, "Reward Milestone", "Treasure chest opened! 🎁", "Reward");
                new AlertDialog.Builder(this)
                    .setTitle("WOW!!")
                    .setMessage("You found the magic gift inside the chest!\nKeep playing to find more!\n\n🎉 Well done buddy!\nYou are the superstar!")
                    .setPositiveButton("Awesome!", (dialog, which) -> updateUI())
                    .show();
                speak("Well done buddy! You are the superstar!");
            }).start();
        }
    }

    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "RewardUtterance");
        }
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

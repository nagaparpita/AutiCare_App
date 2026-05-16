package com.example.auticare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;

public class ChildDashboardActivity extends AppCompatActivity {

    CardView communicationCard, emotionCard, routineCard, treasureChestCard, rewardsRoomCard;
    Button logoutBtn;
    ImageView profileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_dashboard);

        // Initialize UI components
        communicationCard = findViewById(R.id.communicationCard);
        emotionCard = findViewById(R.id.emotionCard);
        routineCard = findViewById(R.id.routineCard);
        treasureChestCard = findViewById(R.id.treasureChestCard);
        rewardsRoomCard = findViewById(R.id.rewardsRoomCard);
        logoutBtn = findViewById(R.id.logoutBtn);
        profileBtn = findViewById(R.id.profileBtn);

        // Card Click Listeners
        if (communicationCard != null) {
            communicationCard.setOnClickListener(v ->
                    startActivity(new Intent(this, CommunicationActivity.class)));
        }

        if (emotionCard != null) {
            emotionCard.setOnClickListener(v ->
                    startActivity(new Intent(this, EmotionActivity.class)));
        }

        if (routineCard != null) {
            routineCard.setOnClickListener(v ->
                    startActivity(new Intent(this, RoutineActivity.class)));
        }

        if (treasureChestCard != null) {
            treasureChestCard.setOnClickListener(v ->
                    startActivity(new Intent(this, TreasureChestActivity.class)));
        }

        if (rewardsRoomCard != null) {
            rewardsRoomCard.setOnClickListener(v ->
                    startActivity(new Intent(this, RewardActivity.class)));
        }

        // Action Listeners
        if (logoutBtn != null) {
            logoutBtn.setOnClickListener(v -> {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }

        if (profileBtn != null) {
            profileBtn.setOnClickListener(v -> {
                startActivity(new Intent(this, ChildProfileActivity.class));
            });
        }
    }
}

package com.example.auticare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class HelperDashboardActivity extends AppCompatActivity {

    View emotionCard, communicationCard, routineCard, progressCard, medicineCard, rewardsCard, childHistoryCard;
    Button logoutBtn;
    TextView starText;
    ImageView profileBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_dashboard);

        // Initialize UI components
        emotionCard = findViewById(R.id.emotionCard);
        communicationCard = findViewById(R.id.communicationCard);
        routineCard = findViewById(R.id.routineCard);
        progressCard = findViewById(R.id.progressCard);
        medicineCard = findViewById(R.id.medicineCard);
        rewardsCard = findViewById(R.id.rewardsCard);
        childHistoryCard = findViewById(R.id.childHistoryCard);
        
        logoutBtn = findViewById(R.id.logoutBtn);
        starText = findViewById(R.id.starText);
        profileBtn = findViewById(R.id.profileBtn);

        updateStars();

        // Card Click Listeners
        if (emotionCard != null) {
            emotionCard.setOnClickListener(v ->
                    startActivity(new Intent(this, EmotionGraphActivity.class)));
        }

        if (communicationCard != null) {
            communicationCard.setOnClickListener(v ->
                    startActivity(new Intent(this, CommunicationHistoryActivity.class)));
        }

        if (routineCard != null) {
            routineCard.setOnClickListener(v ->
                    startActivity(new Intent(this, RoutineStatusActivity.class)));
        }

        if (progressCard != null) {
            progressCard.setOnClickListener(v ->
                    startActivity(new Intent(this, ProgressReportActivity.class)));
        }

        if (medicineCard != null) {
            medicineCard.setOnClickListener(v ->
                    startActivity(new Intent(this, MedicineManagementActivity.class)));
        }

        if (childHistoryCard != null) {
            childHistoryCard.setOnClickListener(v ->
                    startActivity(new Intent(this, ChildHistoryActivity.class)));
        }

        if (rewardsCard != null) {
            rewardsCard.setOnClickListener(v ->
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
                startActivity(new Intent(this, HelperProfileActivity.class));
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStars();
    }

    private void updateStars() {
        if (starText != null) {
            starText.setText("Total Stars: 0");
        }
    }
}

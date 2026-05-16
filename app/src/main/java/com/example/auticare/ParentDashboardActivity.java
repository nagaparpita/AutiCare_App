package com.example.auticare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ParentDashboardActivity extends AppCompatActivity {

    View cardEmotion, cardMedicine, cardProgress, cardCommunication, cardRoutine, cardChildHistory, cardRewards;
    Button logoutBtn;
    ImageView profileBtn;
    
    TextView tvLatestComm, tvLatestTime, tvTodayDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_dashboard);

        // Map UI components
        cardEmotion = findViewById(R.id.emotionCard);
        cardCommunication = findViewById(R.id.communicationCard);
        cardRoutine = findViewById(R.id.routineCard);
        cardProgress = findViewById(R.id.progressCard);
        cardMedicine = findViewById(R.id.medicineCard);
        cardChildHistory = findViewById(R.id.childHistoryCard);
        cardRewards = findViewById(R.id.rewardsCard);
        
        logoutBtn = findViewById(R.id.logoutBtn);
        profileBtn = findViewById(R.id.profileBtn);
        
        tvLatestComm = findViewById(R.id.tvLatestComm);
        tvLatestTime = findViewById(R.id.tvLatestTime);
        tvTodayDate = findViewById(R.id.tvTodayDate);

        updateLiveSummary();

        // Click listeners for cards
        if (cardEmotion != null) {
            cardEmotion.setOnClickListener(v ->
                    startActivity(new Intent(this, EmotionGraphActivity.class)));
        }

        if (cardCommunication != null) {
            cardCommunication.setOnClickListener(v ->
                    startActivity(new Intent(this, CommunicationHistoryActivity.class)));
        }

        if (cardRoutine != null) {
            cardRoutine.setOnClickListener(v ->
                    startActivity(new Intent(this, RoutineStatusActivity.class)));
        }

        if (cardProgress != null) {
            cardProgress.setOnClickListener(v ->
                    startActivity(new Intent(this, ProgressReportActivity.class)));
        }

        if (cardMedicine != null) {
            cardMedicine.setOnClickListener(v ->
                    startActivity(new Intent(this, MedicineManagementActivity.class)));
        }

        if (cardChildHistory != null) {
            cardChildHistory.setOnClickListener(v ->
                    startActivity(new Intent(this, ChildHistoryActivity.class)));
        }

        if (cardRewards != null) {
            cardRewards.setOnClickListener(v ->
                    startActivity(new Intent(this, RewardActivity.class)));
        }

        // Logout click listener
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
                startActivity(new Intent(this, ParentProfileActivity.class));
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateLiveSummary();
    }

    private void updateLiveSummary() {
        SharedPreferences commPrefs = getSharedPreferences("CommunicationLogs", MODE_PRIVATE);
        String latestLog = commPrefs.getString("latest_log", "");

        String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        if (tvTodayDate != null) tvTodayDate.setText("Today - " + currentDate);

        if (!latestLog.isEmpty()) {
            // Log format: Date | Time | Type | Message | Status | ObservedBy
            String[] parts = latestLog.split("\\|");
            if (parts.length >= 4) {
                String logDate = parts[0].trim();
                String logTime = parts[1].trim();
                String logMessage = parts[3].trim();

                // Only show if the log is from today
                if (logDate.equals(currentDate)) {
                    if (tvLatestComm != null) tvLatestComm.setText("Last Communication: " + logMessage);
                    if (tvLatestTime != null) tvLatestTime.setText("Time: " + logTime);
                } else {
                    resetSummary();
                }
            }
        } else {
            resetSummary();
        }
    }

    private void resetSummary() {
        if (tvLatestComm != null) tvLatestComm.setText("Last Communication: None");
        if (tvLatestTime != null) tvLatestTime.setText("Time: --");
    }
}

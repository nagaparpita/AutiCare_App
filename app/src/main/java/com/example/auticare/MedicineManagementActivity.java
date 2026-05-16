package com.example.auticare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicineManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MedicineAdapter adapter;
    private List<MedicineLog> medicineLogs;
    private TextView todayDateText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine_management);

        todayDateText = findViewById(R.id.todayDateText);
        recyclerView = findViewById(R.id.medicineRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
        findViewById(R.id.historyBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, MedicineHistoryActivity.class));
        });

        String currentDate = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        todayDateText.setText("Today - " + currentDate);

        loadTodaySchedule();
    }

    private void loadTodaySchedule() {
        medicineLogs = new ArrayList<>();
        // These represent today's tasks
        medicineLogs.add(new MedicineLog("Melatonin", "Sleep support", "5 ml", "09:00 PM", "Once daily", getMedicineStatus("Melatonin")));
        medicineLogs.add(new MedicineLog("Multivitamin", "General health", "1 tab", "08:00 AM", "Once daily", getMedicineStatus("Multivitamin")));

        adapter = new MedicineAdapter(medicineLogs);
        recyclerView.setAdapter(adapter);
    }

    private String getMedicineStatus(String medicineName) {
        SharedPreferences prefs = getSharedPreferences("MedicineStatus", MODE_PRIVATE);
        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return prefs.getString(todayDate + "_" + medicineName, "Pending");
    }

    private void saveMedicineStatus(String medicineName, String status) {
        SharedPreferences prefs = getSharedPreferences("MedicineStatus", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String todayDate = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        editor.putString(todayDate + "_" + medicineName, status);
        editor.apply();
    }

    private void saveToHistory(MedicineLog log) {
        SharedPreferences prefs = getSharedPreferences("MedicineHistory", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        
        String timestamp = new SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.getDefault()).format(new Date());
        
        // Get user role for "Given by" information
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String role = userPrefs.getString("user_role", "User");
        
        String historyEntry = timestamp + " - " + log.name + " - Given by " + role;
        
        // Using timestamp as key to ensure uniqueness for each event
        editor.putString("history_" + System.currentTimeMillis(), historyEntry);
        editor.apply();
    }

    // Data model for Daily Log
    class MedicineLog {
        String name, purpose, dose, time, frequency, status;

        public MedicineLog(String name, String purpose, String dose, String time, String frequency, String status) {
            this.name = name;
            this.purpose = purpose;
            this.dose = dose;
            this.time = time;
            this.frequency = frequency;
            this.status = status;
        }
    }

    // Adapter for RecyclerView
    class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.ViewHolder> {
        private List<MedicineLog> logs;

        public MedicineAdapter(List<MedicineLog> logs) {
            this.logs = logs;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_medicine_log, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            MedicineLog log = logs.get(position);
            holder.nameText.setText(log.name);
            holder.detailsText.setText(log.dose + " - " + log.time + " (" + log.purpose + ")");
            holder.statusText.setText("Status: " + log.status);

            if (log.status.equals("Given")) {
                holder.statusText.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                holder.markBtn.setVisibility(View.GONE);
            } else {
                holder.statusText.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                holder.markBtn.setVisibility(View.VISIBLE);
            }

            holder.markBtn.setOnClickListener(v -> {
                log.status = "Given";
                saveMedicineStatus(log.name, "Given");
                saveToHistory(log); // Save to persistent history
                notifyItemChanged(position);
                Toast.makeText(MedicineManagementActivity.this, log.name + " marked as Given", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView nameText, detailsText, statusText;
            View markBtn;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                nameText = itemView.findViewById(R.id.medicineNameText);
                detailsText = itemView.findViewById(R.id.medicineDetailsText);
                statusText = itemView.findViewById(R.id.statusText);
                markBtn = itemView.findViewById(R.id.markGivenBtn);
            }
        }
    }
}

package com.example.auticare;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HistoryHelper {

    public static void logActivity(String action) {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("child_history").child(userId).push();

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        // Using 'a' instead of 'AM' for AM/PM marker in SimpleDateFormat
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        
        Date now = new Date();
        String date = dateFormat.format(now);
        String time = timeFormat.format(now);
        long timestamp = now.getTime();

        // Using the existing 7-argument constructor: 
        // HistoryItem(date, time, type, description, status, performedBy, timestamp)
        HistoryItem item = new HistoryItem(date, time, "Activity", action, "Completed", "User", timestamp);
        ref.setValue(item);
    }
}

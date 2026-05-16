package com.example.auticare;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationHelper {

    private static final String CHANNEL_ID = "AutiCare_Notifications";
    private static final String CHANNEL_NAME = "AutiCare Alerts";

    public static void logNotification(String userId, String userRole, String childId, String title, String message, String type) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("NotificationLog");
        String notificationId = mDatabase.push().getKey();

        String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        String time = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

        NotificationLog log = new NotificationLog(notificationId, userId, userRole, childId, title, message, type, date, time);
        if (notificationId != null) {
            mDatabase.child(notificationId).setValue(log);
        }
    }

    public static void showSystemNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_bell)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // Specialized methods for Child Side (Soft & Positive)
    public static void sendChildReminder(Context context, String userId, String childId, String message, String type) {
        // Log to Firebase
        logNotification(userId, "Child", childId, "Friendly Reminder", message, type);
        // Show soft notification
        showSystemNotification(context, "Friendly Reminder 😊", message);
    }

    public static void sendParentAlert(Context context, String parentId, String childId, String title, String message, String type) {
        logNotification(parentId, "Parent", childId, title, message, type);
        showSystemNotification(context, title, message);
    }

    public static void sendHelperAlert(Context context, String helperId, String childId, String title, String message, String type) {
        logNotification(helperId, "Helper", childId, title, message, type);
        showSystemNotification(context, title, message);
    }
}

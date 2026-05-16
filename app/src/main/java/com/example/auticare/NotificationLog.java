package com.example.auticare;

public class NotificationLog {
    public String notificationId;
    public String userId;
    public String userRole; // Child / Parent / Helper
    public String childId;
    public String title;
    public String message;
    public String type; // Routine / Medicine / Communication / Reward / Emotion
    public String date;
    public String time;
    public boolean isRead;

    public NotificationLog() {
        // Default constructor required for calls to DataSnapshot.getValue(NotificationLog.class)
    }

    public NotificationLog(String notificationId, String userId, String userRole, String childId, String title, String message, String type, String date, String time) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.userRole = userRole;
        this.childId = childId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.date = date;
        this.time = time;
        this.isRead = false;
    }
}

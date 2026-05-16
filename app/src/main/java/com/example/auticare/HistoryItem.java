package com.example.auticare;

public class HistoryItem {
    private String date;
    private String time;
    private String type; // Emotion, Communication, Routine, Medicine
    private String description;
    private String status;
    private String performedBy;
    private long timestamp;

    public HistoryItem() {}

    public HistoryItem(String date, String time, String type, String description, String status, String performedBy, long timestamp) {
        this.date = date;
        this.time = time;
        this.type = type;
        this.description = description;
        this.status = status;
        this.performedBy = performedBy;
        this.timestamp = timestamp;
    }

    public String getDate() { return date; }
    public String getTime() { return time; }
    public String getType() { return type; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getPerformedBy() { return performedBy; }
    public long getTimestamp() { return timestamp; }
}

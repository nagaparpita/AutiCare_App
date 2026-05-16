package com.example.auticare;

public class Habit {
    public int id;
    public int imageResId;
    public boolean isGood;
    public String title;
    public String voiceText;

    public Habit(int id, int imageResId, boolean isGood, String title, String voiceText) {
        this.id = id;
        this.imageResId = imageResId;
        this.isGood = isGood;
        this.title = title;
        this.voiceText = voiceText;
    }
}

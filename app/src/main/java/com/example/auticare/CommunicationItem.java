package com.example.auticare;

public class CommunicationItem {
    private String name;
    private int imageResource;

    public CommunicationItem(String name, int imageResource) {
        this.name = name;
        this.imageResource = imageResource;
    }

    public String getName() {
        return name;
    }

    public int getImageResource() {
        return imageResource;
    }
}
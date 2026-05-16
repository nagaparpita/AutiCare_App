package com.example.auticare;

public class Medicine {

    String name;
    String dosage;
    String time;

    public Medicine(String name, String dosage, String time) {
        this.name = name;
        this.dosage = dosage;
        this.time = time;
    }

    public String getName() { return name; }
    public String getDosage() { return dosage; }
    public String getTime() { return time; }
}
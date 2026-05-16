package com.example.auticare;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Button btnCommunication, btnRoutine, btnEmotion, btnMedicine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCommunication = findViewById(R.id.btnCommunication);
        btnRoutine = findViewById(R.id.btnRoutine);
        btnEmotion = findViewById(R.id.btnEmotion);
        btnMedicine = findViewById(R.id.btnMedicine);

        btnCommunication.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CommunicationActivity.class)));

        btnRoutine.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, RoutineActivity.class)));

        btnEmotion.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, EmotionActivity.class)));

        btnMedicine.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, MedicineActivity.class)));
    }
}
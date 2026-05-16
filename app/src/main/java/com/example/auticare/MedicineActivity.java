package com.example.auticare;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MedicineActivity extends AppCompatActivity {

    Button btnTakeMedicine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medicine);

        btnTakeMedicine = findViewById(R.id.btnTakeMedicine);

        btnTakeMedicine.setOnClickListener(v ->
                Toast.makeText(this, "Medicine Taken", Toast.LENGTH_SHORT).show());
    }
}
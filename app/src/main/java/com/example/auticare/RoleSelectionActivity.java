package com.example.auticare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class RoleSelectionActivity extends AppCompatActivity {

    Button childBtn, parentBtn, helperBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        childBtn = findViewById(R.id.childBtn);
        parentBtn = findViewById(R.id.parentBtn);
        helperBtn = findViewById(R.id.helperBtn);

        childBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ChildDashboardActivity.class)));

        parentBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ParentDashboardActivity.class)));

        helperBtn.setOnClickListener(v ->
                startActivity(new Intent(this, HelperDashboardActivity.class)));
    }
}
package com.example.auticare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class UserSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_selection);

        View btnParent = findViewById(R.id.btnParent);
        View btnChild = findViewById(R.id.btnChild);
        View btnHelper = findViewById(R.id.btnHelper);

        if (btnParent != null) {
            btnParent.setOnClickListener(v -> {
                saveUserRole("Parent");
                startActivity(new Intent(UserSelectionActivity.this, ParentDashboardActivity.class));
            });
        }

        if (btnChild != null) {
            btnChild.setOnClickListener(v -> {
                saveUserRole("Child");
                startActivity(new Intent(UserSelectionActivity.this, ChildDashboardActivity.class));
            });
        }

        if (btnHelper != null) {
            btnHelper.setOnClickListener(v -> {
                saveUserRole("Helper");
                startActivity(new Intent(UserSelectionActivity.this, HelperDashboardActivity.class));
            });
        }
    }

    private void saveUserRole(String role) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        prefs.edit().putString("user_role", role).apply();
    }
}

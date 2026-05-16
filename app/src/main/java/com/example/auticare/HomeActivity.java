package com.example.auticare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class HomeActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        String userId = mAuth.getCurrentUser().getUid();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        databaseReference.child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        if (snapshot.exists()) {

                            String role = snapshot.child("role").getValue(String.class);

                            if (role != null && role.equals("Parent")) {
                                startActivity(new Intent(HomeActivity.this, ParentDashboardActivity.class));
                            } else if (role != null && role.equals("Helper")) {
                                startActivity(new Intent(HomeActivity.this, HelperDashboardActivity.class));
                            } else {
                                Toast.makeText(HomeActivity.this,
                                        "Role not found!", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                            }

                        } else {
                            Toast.makeText(HomeActivity.this,
                                    "User data not found!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                        }

                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(HomeActivity.this,
                                "Database Error: " + error.getMessage(),
                                Toast.LENGTH_LONG).show();
                        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                        finish();
                    }
                });
    }
}
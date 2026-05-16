package com.example.auticare;

import android.content.Intent;
import android.os.Bundle;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    EditText fullName, age, phone, email, password;
    Button registerBtn;
    RadioGroup roleGroup;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fullName = findViewById(R.id.fullName);
        age = findViewById(R.id.age);
        phone = findViewById(R.id.phone);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        registerBtn = findViewById(R.id.registerBtn);
        roleGroup = findViewById(R.id.roleGroup);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        registerBtn.setOnClickListener(v -> {

            final String name = fullName.getText().toString().trim();
            final String userAge = age.getText().toString().trim();
            final String userPhone = phone.getText().toString().trim();
            final String userEmail = email.getText().toString().trim();
            final String userPass = password.getText().toString().trim();

            int selectedRoleId = roleGroup.getCheckedRadioButtonId();

            if(name.isEmpty() || userEmail.isEmpty() || userPass.isEmpty() || selectedRoleId == -1){
                Toast.makeText(this, "Please fill all fields and select role", Toast.LENGTH_SHORT).show();
                return;
            }

            final String role = (selectedRoleId == R.id.parentRole) ? "Parent" : "Helper";

            mAuth.createUserWithEmailAndPassword(userEmail, userPass)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){

                            String userId = mAuth.getCurrentUser().getUid();

                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("name", name);
                            userMap.put("age", userAge);
                            userMap.put("phone", userPhone);
                            userMap.put("email", userEmail);
                            userMap.put("role", role);

                            databaseReference.child(userId).setValue(userMap);

                            Toast.makeText(this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();

                        } else {
                            Toast.makeText(this,
                                    "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });

        });
    }
}
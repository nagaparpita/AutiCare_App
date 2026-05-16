package com.example.auticare;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddChildActivity extends AppCompatActivity {

    EditText etChildName, etAge, etParentName, etBirthDate;
    Button btnSave;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        etChildName = findViewById(R.id.etChildName);
        etAge = findViewById(R.id.etAge);
        etParentName = findViewById(R.id.etParentName);
        etBirthDate = findViewById(R.id.etBirthDate);
        btnSave = findViewById(R.id.btnSave);

        etBirthDate.setOnClickListener(v -> showDatePicker());

        btnSave.setOnClickListener(v -> saveData());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String date = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, (monthOfYear + 1), year1);
                    etBirthDate.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveData() {
        String childName = etChildName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String parentName = etParentName.getText().toString().trim();
        String birthDate = etBirthDate.getText().toString().trim();

        if (TextUtils.isEmpty(childName) || TextUtils.isEmpty(age) || 
            TextUtils.isEmpty(parentName) || TextUtils.isEmpty(birthDate)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = mAuth.getUid();
        if (userId == null) return;

        Map<String, Object> childData = new HashMap<>();
        childData.put("name", childName);
        childData.put("age", age);
        childData.put("parentName", parentName);
        childData.put("birthDate", birthDate);

        mDatabase.child("users").child(userId).child("child_details").setValue(childData)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(AddChildActivity.this, "Child Details Saved Successfully", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(AddChildActivity.this, "Failed to save data: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }
}

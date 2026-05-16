package com.example.auticare;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HelperProfileActivity extends AppCompatActivity {

    private TextView tvHelperName, tvRole;
    private View rowMobile, rowEmail, rowExperience, rowAvailability, rowWorkingTime, rowAssignedChildren;
    private Button btnEditProfile, btnChangePassword;
    private ImageView ivHelperPhoto;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_helper_profile);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        ivHelperPhoto = findViewById(R.id.ivHelperPhoto);
        tvHelperName = findViewById(R.id.tvHelperName);
        tvRole = findViewById(R.id.tvRole);

        rowMobile = findViewById(R.id.rowMobile);
        rowEmail = findViewById(R.id.rowEmail);
        rowExperience = findViewById(R.id.rowExperience);
        rowAvailability = findViewById(R.id.rowAvailability);
        rowWorkingTime = findViewById(R.id.rowWorkingTime);
        rowAssignedChildren = findViewById(R.id.rowAssignedChildren);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        setupLabels();
        loadProfileData();

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }
    }

    private void setupLabels() {
        ((TextView) rowMobile.findViewById(R.id.label)).setText("Mobile Number");
        ((TextView) rowEmail.findViewById(R.id.label)).setText("Email");
        ((TextView) rowExperience.findViewById(R.id.label)).setText("Experience");
        ((TextView) rowAvailability.findViewById(R.id.label)).setText("Availability");
        ((TextView) rowWorkingTime.findViewById(R.id.label)).setText("Working Time");
        ((TextView) rowAssignedChildren.findViewById(R.id.label)).setText("Assigned Child / Children");
    }

    private void loadProfileData() {
        tvHelperName.setText("Neha Sharma");
        tvRole.setText("Special Educator / Therapist");
        ivHelperPhoto.setImageResource(R.drawable.helper_avatar_illus);

        ((TextView) rowMobile.findViewById(R.id.value)).setText("+91 91234 56789");
        ((TextView) rowEmail.findViewById(R.id.value)).setText("neha.sharma@auticare.in");
        ((TextView) rowExperience.findViewById(R.id.value)).setText("4 years");
        ((TextView) rowAvailability.findViewById(R.id.value)).setText("Full Time");
        ((TextView) rowWorkingTime.findViewById(R.id.value)).setText("10:00 AM – 4:00 PM");
        ((TextView) rowAssignedChildren.findViewById(R.id.value)).setText("• Aarav Patil\n• Riya Patil");
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        builder.setView(view);

        EditText etName = view.findViewById(R.id.etName);
        EditText etPhone = view.findViewById(R.id.etPhone);
        TextInputLayout tilExtra = view.findViewById(R.id.tilExtraInfo);
        EditText etExtra = view.findViewById(R.id.etExtraInfo);

        tilExtra.setHint("Professional Role");
        etName.setText(tvHelperName.getText().toString());
        etPhone.setText(((TextView) rowMobile.findViewById(R.id.value)).getText().toString());
        etExtra.setText(tvRole.getText().toString());

        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = etName.getText().toString();
            String phone = etPhone.getText().toString();
            String role = etExtra.getText().toString();

            tvHelperName.setText(name);
            ((TextView) rowMobile.findViewById(R.id.value)).setText(phone);
            tvRole.setText(role);

            Toast.makeText(this, "Helper Profile Updated", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_change_password, null);
        builder.setView(view);

        EditText etNewPass = view.findViewById(R.id.etNewPassword);
        EditText etConfirmPass = view.findViewById(R.id.etConfirmPassword);

        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnUpdate).setOnClickListener(v -> {
            String newPass = etNewPass.getText().toString();
            String confirmPass = etConfirmPass.getText().toString();

            if (newPass.length() < 6) {
                Toast.makeText(this, "Min 6 chars", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords don't match", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                user.updatePassword(newPass).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Success", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}

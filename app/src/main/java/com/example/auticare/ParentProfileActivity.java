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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ParentProfileActivity extends AppCompatActivity {

    private TextView tvParentName, tvRelation;
    private View rowMobile, rowEmail, rowRegDate, rowChildrenLinked;
    private Button btnEditProfile, btnChangePassword;
    private ImageView ivParentPhoto;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parent_profile);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        ivParentPhoto = findViewById(R.id.ivParentPhoto);
        tvParentName = findViewById(R.id.tvParentName);
        tvRelation = findViewById(R.id.tvRelation);

        rowMobile = findViewById(R.id.rowMobile);
        rowEmail = findViewById(R.id.rowEmail);
        rowRegDate = findViewById(R.id.rowRegDate);
        rowChildrenLinked = findViewById(R.id.rowChildrenLinked);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        setupLabels();
        loadProfileData();

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
        btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
    }

    private void setupLabels() {
        ((TextView) rowMobile.findViewById(R.id.label)).setText("Mobile Number");
        ((TextView) rowEmail.findViewById(R.id.label)).setText("Email");
        ((TextView) rowRegDate.findViewById(R.id.label)).setText("Registered Date");
        ((TextView) rowChildrenLinked.findViewById(R.id.label)).setText("Total Children Linked");
    }

    private void loadProfileData() {
        // Example data display
        tvParentName.setText("Anjali Patil");
        tvRelation.setText("Relation with Child: Mother");
        ivParentPhoto.setImageResource(R.drawable.parent_avatar_illus);
        
        ((TextView) rowMobile.findViewById(R.id.value)).setText("+91 98765 43210");
        ((TextView) rowEmail.findViewById(R.id.value)).setText("anjali.patil@gmail.com");
        ((TextView) rowRegDate.findViewById(R.id.value)).setText("12 January 2026");
        ((TextView) rowChildrenLinked.findViewById(R.id.value)).setText("1 Child");
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        builder.setView(view);

        EditText etName = view.findViewById(R.id.etName);
        EditText etPhone = view.findViewById(R.id.etPhone);
        TextInputLayout tilExtra = view.findViewById(R.id.tilExtraInfo);
        EditText etExtra = view.findViewById(R.id.etExtraInfo);
        
        tilExtra.setHint("Relation with Child");
        
        // Pre-fill with current data
        etName.setText(tvParentName.getText().toString());
        etPhone.setText(((TextView) rowMobile.findViewById(R.id.value)).getText().toString());
        etExtra.setText("Mother");

        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = etName.getText().toString();
            String phone = etPhone.getText().toString();
            String relation = etExtra.getText().toString();

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Update UI
            tvParentName.setText(name);
            ((TextView) rowMobile.findViewById(R.id.value)).setText(phone);
            tvRelation.setText("Relation with Child: " + relation);

            // Update Firebase if logged in
            String uid = mAuth.getUid();
            if (uid != null) {
                mDatabase.child("users").child(uid).child("name").setValue(name);
                mDatabase.child("users").child(uid).child("phone").setValue(phone);
            }

            Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser user = mAuth.getCurrentUser();
            if (user != null) {
                user.updatePassword(newPass).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password Changed Successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}

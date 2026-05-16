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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ChildProfileActivity extends AppCompatActivity {

    private TextView tvChildName, tvNickname;
    private View rowBirthDate, rowGender, rowCondition, rowSpecialNeeds, rowAllergies, rowRoutines, rowHelpers, rowMedicines;
    private Button btnEditProfile;
    private ImageView ivChildPhoto;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_profile);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        ivChildPhoto = findViewById(R.id.ivChildPhoto);
        tvChildName = findViewById(R.id.tvChildName);
        tvNickname = findViewById(R.id.tvNickname);

        rowBirthDate = findViewById(R.id.rowBirthDate);
        rowGender = findViewById(R.id.rowGender);
        rowCondition = findViewById(R.id.rowCondition);
        rowSpecialNeeds = findViewById(R.id.rowSpecialNeeds);
        rowAllergies = findViewById(R.id.rowAllergies);
        rowRoutines = findViewById(R.id.rowRoutines);
        rowHelpers = findViewById(R.id.rowHelpers);
        rowMedicines = findViewById(R.id.rowMedicines);

        btnEditProfile = findViewById(R.id.btnEditProfile);

        setupLabels();
        loadProfileData();

        btnEditProfile.setOnClickListener(v -> showEditProfileDialog());
    }

    private void setupLabels() {
        ((TextView) rowBirthDate.findViewById(R.id.label)).setText("Date of Birth / Age");
        ((TextView) rowGender.findViewById(R.id.label)).setText("Gender");
        ((TextView) rowCondition.findViewById(R.id.label)).setText("Diagnosis / Condition");
        ((TextView) rowSpecialNeeds.findViewById(R.id.label)).setText("Special Needs / Notes");
        ((TextView) rowAllergies.findViewById(R.id.label)).setText("Allergies");
        ((TextView) rowRoutines.findViewById(R.id.label)).setText("Assigned Routines");
        ((TextView) rowHelpers.findViewById(R.id.label)).setText("Assigned Helper(s)");
        ((TextView) rowMedicines.findViewById(R.id.label)).setText("Current Medicines");
    }

    private void loadProfileData() {
        tvChildName.setText("Aarav Patil");
        tvNickname.setText("(Aaru)");
        // Updated to use the child avatar illustration
        ivChildPhoto.setImageResource(R.drawable.child_avatar_illus);

        ((TextView) rowBirthDate.findViewById(R.id.value)).setText("15 March 2020 (5 years)");
        ((TextView) rowGender.findViewById(R.id.value)).setText("Male");
        ((TextView) rowCondition.findViewById(R.id.value)).setText("Autism Spectrum Disorder (ASD – Level 1)");
        ((TextView) rowSpecialNeeds.findViewById(R.id.value)).setText("Needs visual instructions and calm environment");
        ((TextView) rowAllergies.findViewById(R.id.value)).setText("Peanut allergy");
        
        String routines = "• Morning hygiene routine\n• Study & learning time\n• Play time routine\n• Sleep routine";
        ((TextView) rowRoutines.findViewById(R.id.value)).setText(routines);
        ((TextView) rowHelpers.findViewById(R.id.value)).setText("Ms. Neha Sharma (Special Educator)");
        String medicines = "• Melatonin (night – as prescribed)\n• Multivitamin syrup";
        ((TextView) rowMedicines.findViewById(R.id.value)).setText(medicines);
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_profile, null);
        builder.setView(view);

        EditText etName = view.findViewById(R.id.etName);
        EditText etNickname = view.findViewById(R.id.etPhone);
        TextInputLayout tilExtra = view.findViewById(R.id.tilExtraInfo);
        EditText etCondition = view.findViewById(R.id.etExtraInfo);

        tilExtra.setVisibility(View.VISIBLE);
        tilExtra.setHint("Diagnosis / Condition");

        // Correctly setting hint for the nickname field which is reusing etPhone's container
        if (view.findViewById(R.id.etPhone).getParent().getParent() instanceof TextInputLayout) {
            ((TextInputLayout) view.findViewById(R.id.etPhone).getParent().getParent()).setHint("Nickname");
        }

        etName.setText(tvChildName.getText().toString());
        etNickname.setText("Aaru");
        etCondition.setText(((TextView) rowCondition.findViewById(R.id.value)).getText().toString());

        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String name = etName.getText().toString();
            String nick = etNickname.getText().toString();
            String cond = etCondition.getText().toString();

            tvChildName.setText(name);
            tvNickname.setText("(" + nick + ")");
            ((TextView) rowCondition.findViewById(R.id.value)).setText(cond);

            Toast.makeText(this, "Child Profile Updated", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }
}

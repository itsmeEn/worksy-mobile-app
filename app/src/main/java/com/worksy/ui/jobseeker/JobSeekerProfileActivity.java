package com.worksy.ui.jobseeker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.worksy.R;

public class JobSeekerProfileActivity extends AppCompatActivity {
    private TextView nameTextView, emailTextView, resumeTextView;
    private Spinner statusSpinner;
    private Button updateStatusButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String userId;
    private String resumeUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_seeker_profile);

        nameTextView = findViewById(R.id.profileNameTextView);
        emailTextView = findViewById(R.id.profileEmailTextView);
        resumeTextView = findViewById(R.id.profileResumeTextView);
        statusSpinner = findViewById(R.id.profileStatusSpinner);
        updateStatusButton = findViewById(R.id.profileUpdateStatusButton);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        userId = currentUser.getUid();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.profile_status_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        fetchProfile();

        updateStatusButton.setOnClickListener(v -> updateStatus());
        resumeTextView.setOnClickListener(v -> {
            if (!TextUtils.isEmpty(resumeUrl)) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(resumeUrl));
                startActivity(browserIntent);
            }
        });
    }

    private void fetchProfile() {
        DocumentReference userRef = firebaseFirestore.collection("users").document(userId);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                String email = documentSnapshot.getString("email");
                String status = documentSnapshot.getString("status");
                resumeUrl = documentSnapshot.getString("resumeUrl");
                nameTextView.setText(name != null ? name : "");
                emailTextView.setText(email != null ? email : "");
                resumeTextView.setText(resumeUrl != null ? resumeUrl : "No resume uploaded");
                if (status != null) {
                    int spinnerPosition = ((ArrayAdapter) statusSpinner.getAdapter()).getPosition(status);
                    if (spinnerPosition >= 0) statusSpinner.setSelection(spinnerPosition);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch profile", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateStatus() {
        String newStatus = statusSpinner.getSelectedItem().toString();
        firebaseFirestore.collection("users").document(userId)
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Status updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show());
    }
}

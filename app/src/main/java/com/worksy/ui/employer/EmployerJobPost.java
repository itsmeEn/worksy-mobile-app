package com.worksy.ui.employer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.worksy.R;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class EmployerJobPost extends AppCompatActivity {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private EditText editTextJobTitle;
    private EditText editJobDescription;
    private EditText editTextSalaryRange;
    private EditText editTextLocation;
    private RadioGroup radioGroupWorkArrangement;
    private RadioGroup radioGroupExperienceLevel;
    FusedLocationProviderClient fusedLocationProviderClient;
    private final static int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employee_post_job);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize your views using findViewById
        editTextJobTitle = findViewById(R.id.editTextJobTitle);
        editJobDescription = findViewById(R.id.editJobDescription);
        editTextSalaryRange = findViewById(R.id.editTextSalaryRange);
        editTextLocation = findViewById(R.id.editTextLocation);
        radioGroupWorkArrangement = findViewById(R.id.radioGroupWorkArrangement);
        radioGroupExperienceLevel = findViewById(R.id.radioGroupExperienceLevel);
        findViewById(R.id.buttonPinLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Logic to get current location
                getCurrentLocation();
            }
        });

        findViewById(R.id.buttonPostNow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show review dialog before posting
                showReviewDialog();
            }
        });

        findViewById(R.id.buttonReset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show reset confirmation dialog
                showResetConfirmationDialog();
            }
        });
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Get the address from the location
                                Geocoder geocoder = new Geocoder(EmployerJobPost.this, Locale.getDefault());
                                try {
                                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                    if (addresses != null && addresses.size() > 0) {
                                        Address address = addresses.get(0);
                                        StringBuilder fullAddress = new StringBuilder();
                                        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                                            fullAddress.append(address.getAddressLine(i)).append("\n");
                                        }
                                        editTextLocation.setText(fullAddress.toString().trim());
                                    } else {
                                        Toast.makeText(EmployerJobPost.this, "Could not find address", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) {
                                    Log.e("Location Error", "Error getting address: " + e.getMessage());
                                    Toast.makeText(EmployerJobPost.this, "Error getting address", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(EmployerJobPost.this, "Could not get last known location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            // Request location permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(this, "Location permission is required to pin your location", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Reset")
                .setMessage("Once reset, all your input will be removed. Are you sure?")
                .setPositiveButton("Yes, Reset", (dialog, which) -> {
                    resetForm();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showReviewDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Review Job Posting")
                .setMessage("Please review your job posting details carefully before posting.")
                .setPositiveButton("Proceed to Post", (dialog, which) -> {
                    // Call the method to actually post the job
                    postJob();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void postJob() {
        // 1. Get the input values from the views
        String jobTitle = editTextJobTitle.getText().toString().trim();
        String description = editJobDescription.getText().toString().trim();
        String salaryRange = editTextSalaryRange.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();

        // 2. Get the selected work arrangement
        String workArrangement = getSelectedWorkArrangement();
        if (workArrangement.isEmpty()) {
            Toast.makeText(this, "Please select a work arrangement", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Get the selected experience level
        String experienceLevel = getSelectedExperienceLevel();
        if (experienceLevel.isEmpty()) {
            Toast.makeText(this, "Please select an experience level", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Basic input validation (you'll likely need more robust validation)
        if (jobTitle.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Job title and description are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5. Get the current user's ID (employer ID)
        String employerId = firebaseAuth.getCurrentUser().getUid();

        // 6. Create a data structure (e.g., a Map or a custom object) to hold the job data
        java.util.Map<String, Object> jobData = new java.util.HashMap<>();
        jobData.put("employerId", employerId);
        jobData.put("jobTitle", jobTitle);
        jobData.put("description", description);
        jobData.put("salaryRange", salaryRange);
        jobData.put("location", location);
        jobData.put("workArrangement", workArrangement);
        jobData.put("experienceLevel", experienceLevel);
        jobData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
        // Add any other relevant fields (e.g., company name, posting date)

        // 7. Add the job data to Firestore
        firebaseFirestore.collection("jobs")
                .add(jobData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Job posted successfully!", Toast.LENGTH_SHORT).show();
                    resetForm(); // Optionally reset the form after successful posting
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to post job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Log the error for debugging: Log.e(TAG, "Error posting job", e);
                });
    }

    private String getSelectedWorkArrangement() {
        int selectedId = radioGroupWorkArrangement.getCheckedRadioButtonId();
        if (selectedId != -1) {
            View radioButton = radioGroupWorkArrangement.findViewById(selectedId);
            return (String) radioButton.getTag();
        }
        return "";
    }

    private String getSelectedExperienceLevel() {
        int selectedId = radioGroupExperienceLevel.getCheckedRadioButtonId();
        if (selectedId != -1) {
            View radioButton = radioGroupExperienceLevel.findViewById(selectedId);
            return (String) radioButton.getTag();
        }
        return "";
    }

    private void resetForm() {
        editTextJobTitle.setText("");
        editJobDescription.setText("");
        editTextSalaryRange.setText("");
        editTextLocation.setText("");
        radioGroupWorkArrangement.clearCheck();
        radioGroupExperienceLevel.clearCheck();
    }
}
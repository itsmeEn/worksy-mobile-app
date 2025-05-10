package com.worksy.ui.employer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
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
    private Spinner spinnerJobCategory; // Added Spinner for Job Category

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback;

    private static final int REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employee_post_job);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Initialize views
        editTextJobTitle = findViewById(R.id.editTextJobTitle);
        editJobDescription = findViewById(R.id.editJobDescription);
        editTextSalaryRange = findViewById(R.id.editTextSalaryRange);
        editTextLocation = findViewById(R.id.editTextLocation);
        radioGroupWorkArrangement = findViewById(R.id.radioGroupWorkArrangement);
        radioGroupExperienceLevel = findViewById(R.id.radioGroupExperienceLevel);
        spinnerJobCategory = findViewById(R.id.spinnerJobCategory); // Initialize Job Category Spinner

        // Populate Job Category Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.job_categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerJobCategory.setAdapter(adapter);

        findViewById(R.id.buttonPinLocation).setOnClickListener(v -> getCurrentLocation());

        findViewById(R.id.buttonPostNow).setOnClickListener(v -> showReviewDialog());

        findViewById(R.id.buttonReset).setOnClickListener(v -> showResetConfirmationDialog());
    }


    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(2000);
            locationRequest.setNumUpdates(1); // Get only one update

            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (locationResult == null) {
                        Toast.makeText(EmployerJobPost.this, "Could not get location result", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Location location = locationResult.getLastLocation();
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(EmployerJobPost.this, Locale.getDefault());
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                Address address = addresses.get(0);
                                StringBuilder fullAddress = new StringBuilder();
                                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                                    fullAddress.append(address.getAddressLine(i)).append("\n");
                                }
                                editTextLocation.setText(fullAddress.toString().trim());
                            } else {
                                Toast.makeText(EmployerJobPost.this, "No address found", Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            Log.e("Location Error", "Geocoder failed: " + e.getMessage());
                            Toast.makeText(EmployerJobPost.this, "Error fetching address", Toast.LENGTH_SHORT).show();
                        }

                        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    }
                }
            };

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationProviderClient != null && locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
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
                .setPositiveButton("Yes, Reset", (dialog, which) -> resetForm())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showReviewDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Review Job Posting")
                .setMessage("Please review your job posting details carefully before posting.")
                .setPositiveButton("Proceed to Post", (dialog, which) -> postJob())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void postJob() {
        String jobTitle = editTextJobTitle.getText().toString().trim();
        String description = editJobDescription.getText().toString().trim();
        String salaryRange = editTextSalaryRange.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();
        String jobCategory = spinnerJobCategory.getSelectedItem().toString(); // Get selected job category

        String workArrangement = getSelectedWorkArrangement();
        if (workArrangement.isEmpty()) {
            Toast.makeText(this, "Please select a work arrangement", Toast.LENGTH_SHORT).show();
            return;
        }

        String experienceLevel = getSelectedExperienceLevel();
        if (experienceLevel.isEmpty()) {
            Toast.makeText(this, "Please select an experience level", Toast.LENGTH_SHORT).show();
            return;
        }

        if (jobTitle.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Job title and description are required", Toast.LENGTH_SHORT).show();
            return;
        }

        String employerId = firebaseAuth.getCurrentUser().getUid();

        java.util.Map<String, Object> jobData = new java.util.HashMap<>();
        jobData.put("employerId", employerId);
        jobData.put("jobTitle", jobTitle);
        jobData.put("description", description);
        jobData.put("salaryRange", salaryRange);
        jobData.put("location", location);
        jobData.put("workArrangement", workArrangement);
        jobData.put("experienceLevel", experienceLevel);
        jobData.put("jobCategory", jobCategory); // Add job category to data
        jobData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        firebaseFirestore.collection("jobs")
                .add(jobData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Job posted successfully!", Toast.LENGTH_SHORT).show();
                    resetForm();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to post job: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String getSelectedWorkArrangement() {
        int selectedId = radioGroupWorkArrangement.getCheckedRadioButtonId();
        if (selectedId != -1) {
            View radioButton = radioGroupWorkArrangement.findViewById(selectedId);
            if (radioButton != null && radioButton.getTag() != null) {
                return radioButton.getTag().toString();
            }
        }
        return "";
    }

    private String getSelectedExperienceLevel() {
        int selectedId = radioGroupExperienceLevel.getCheckedRadioButtonId();
        if (selectedId != -1) {
            View radioButton = radioGroupExperienceLevel.findViewById(selectedId);
            if (radioButton != null && radioButton.getTag() != null) {
                return radioButton.getTag().toString();
            }
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
        spinnerJobCategory.setSelection(0); // Reset to the first item (if it's a default like "Select Category")
    }
}
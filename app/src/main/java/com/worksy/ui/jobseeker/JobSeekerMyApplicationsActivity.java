package com.worksy.ui.jobseeker;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.security.ProviderInstaller;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import com.worksy.R;
import com.worksy.data.model.JobApplication;
import com.worksy.ui.adapter.JobApplicationAdapter;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class JobSeekerMyApplicationsActivity extends AppCompatActivity implements JobApplicationAdapter.OnApplicationClickListener, ProviderInstaller.ProviderInstallListener {

    private static final String TAG = "JobSeekerApps";
    private RecyclerView recyclerViewApplications;
    private JobApplicationAdapter applicationAdapter;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private final List<JobApplication> currentApplications = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.job_seeker_my_applications);

        // Initialize security provider
        ProviderInstaller.installIfNeededAsync(this, this);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        recyclerViewApplications = findViewById(R.id.recyclerViewApplications);
        progressBar = findViewById(R.id.progressBar);
        textViewEmpty = findViewById(R.id.textViewEmpty);

        setupRecyclerView();
        fetchApplications();
    }

    private void setupRecyclerView() {
        applicationAdapter = new JobApplicationAdapter(currentApplications, this);
        recyclerViewApplications.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewApplications.setAdapter(applicationAdapter);
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerViewApplications.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        textViewEmpty.setVisibility(View.GONE);
    }

    private JobApplication.ApplicationStatus getApplicationStatus(String status) {
        if (status == null) return JobApplication.ApplicationStatus.UNKNOWN;
        
        try {
            return JobApplication.ApplicationStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Map string status to enum values
            switch (status.toLowerCase()) {
                case "pending":
                case "reviewing":
                    return JobApplication.ApplicationStatus.REVIEWING;
                case "accepted":
                    return JobApplication.ApplicationStatus.ACCEPTED;
                case "rejected":
                    return JobApplication.ApplicationStatus.REJECTED;
                case "shortlisted":
                    return JobApplication.ApplicationStatus.SHORTLISTED;
                default:
                    return JobApplication.ApplicationStatus.UNKNOWN;
            }
        }
    }

    private void fetchApplications() {
        showLoading(true);
        
        // Get current user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            showLoading(false);
            Toast.makeText(this, "Please log in to view applications", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        String userId = currentUser.getUid();
        Log.d(TAG, "Fetching applications for user ID: " + userId);
        
        // Fetch applications from Firebase
        db.collection("applications")
            .whereEqualTo("jobseekerId", userId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    showLoading(false);
                    textViewEmpty.setVisibility(View.VISIBLE);
                    textViewEmpty.setText("No applications found");
                    currentApplications.clear(); // Clear list even if empty
                    applicationAdapter.notifyDataSetChanged(); // Notify adapter
                    return;
                }

                List<JobApplication> fetchedApplications = new ArrayList<>();
                final int totalApplications = querySnapshot.size();
                final int[] applicationsProcessed = {0};

                for (QueryDocumentSnapshot applicationDoc : querySnapshot) {
                    try {
                        String jobId = applicationDoc.getString("jobId");
                        String status = applicationDoc.getString("status");
                        String applicationDate = applicationDoc.getTimestamp("applicationDate") != null ?
                                applicationDoc.getTimestamp("applicationDate").toDate().toString() : "N/A";

                        if (jobId != null) {
                            // Get job details
                            db.collection("jobs").document(jobId)
                                .get()
                                .addOnSuccessListener(jobDoc -> {
                                    try {
                                        String jobTitle = jobDoc.getString("jobTitle");
                                        String companyName = jobDoc.getString("companyName");
                                        String location = jobDoc.getString("location");

                                        // Create JobApplication with required parameters
                                        JobApplication application = new JobApplication(
                                            applicationDoc.getId(),
                                            jobTitle != null ? jobTitle : "Unknown Position",
                                            companyName != null ? companyName : "Unknown Company",
                                            getApplicationStatus(status),
                                            applicationDate,
                                            location != null ? location : "N/A"
                                        );

                                        fetchedApplications.add(application);

                                    } catch (Exception e) {
                                        Log.e(TAG, "Error processing job document: " + e.getMessage(), e);
                                    } finally {
                                        applicationsProcessed[0]++;
                                        if (applicationsProcessed[0] == totalApplications) {
                                            // All applications processed, update UI
                                            runOnUiThread(() -> {
                                                currentApplications.clear();
                                                currentApplications.addAll(fetchedApplications);
                                                applicationAdapter.notifyDataSetChanged();
                                                showLoading(false);
                                            });
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error getting job details: " + e.getMessage(), e);
                                    applicationsProcessed[0]++;
                                     if (applicationsProcessed[0] == totalApplications) {
                                        // All applications processed, update UI even if some failed
                                        runOnUiThread(() -> {
                                            currentApplications.clear();
                                            currentApplications.addAll(fetchedApplications);
                                            applicationAdapter.notifyDataSetChanged();
                                            showLoading(false);
                                        });
                                    }
                                });
                        } else { // Handle case where jobId is null
                             applicationsProcessed[0]++;
                             if (applicationsProcessed[0] == totalApplications) {
                                // All applications processed, update UI
                                runOnUiThread(() -> {
                                    currentApplications.clear();
                                    currentApplications.addAll(fetchedApplications);
                                    applicationAdapter.notifyDataSetChanged();
                                    showLoading(false);
                                });
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing application document: " + e.getMessage(), e);
                        applicationsProcessed[0]++;
                         if (applicationsProcessed[0] == totalApplications) {
                            // All applications processed, update UI
                            runOnUiThread(() -> {
                                currentApplications.clear();
                                currentApplications.addAll(fetchedApplications);
                                applicationAdapter.notifyDataSetChanged();
                                showLoading(false);
                            });
                        }
                    }
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Failed to fetch applications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error fetching applications: " + e.getMessage());
                 // Ensure UI is updated even on initial fetch failure
                 runOnUiThread(() -> {
                    currentApplications.clear();
                    applicationAdapter.notifyDataSetChanged();
                    textViewEmpty.setVisibility(View.VISIBLE);
                    textViewEmpty.setText("Failed to load applications.");
                 });
            });
    }

    @Override
    public void onProviderInstalled() {
        Log.d(TAG, "Security provider installed successfully");
    }

    @Override
    public void onProviderInstallFailed(int errorCode, android.content.Intent recoveryIntent) {
        GoogleApiAvailability.getInstance().showErrorNotification(this, errorCode);
        Log.e(TAG, "Security provider installation failed with error: " + errorCode);
    }

    @Override
    public void onApplicationClick(JobApplication application) {
        // Handle click on application
        Toast.makeText(this, "Application for " + application.getJobTitle(), Toast.LENGTH_SHORT).show();
    }
}
package com.worksy.ui.jobseeker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.worksy.data.model.Job;
import com.worksy.databinding.ActivityJobSeekerSearchJobBinding;
import com.worksy.ui.adapter.JobAdapter;
import com.worksy.ui.jobseeker.filter.JobFilter;
import com.worksy.ui.jobseeker.filter.JobFilterBottomSheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.worksy.R;
import com.google.firebase.firestore.FieldValue;

public class JobSeekerSearchJobActivity extends AppCompatActivity implements JobFilterBottomSheet.OnFilterAppliedListener {

    private ActivityJobSeekerSearchJobBinding binding;
    private JobAdapter jobAdapter;
    private FirebaseFirestore db;
    private JobFilter currentFilter;
    private Job jobToApply;
    private Uri resumeUriToUpload;
    private ActivityResultLauncher<Intent> pickResumeLauncher;

    private List<Job> allJobs = new ArrayList<>(); // Store all fetched jobs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobSeekerSearchJobBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase and check user authentication
        try {
            db = FirebaseFirestore.getInstance();

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, R.string.please_log_in, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialize RecyclerView and adapters
            setupRecyclerView();
            setupSearchAndFilter();

            // Load jobs with error handling
            loadRecentJobs();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_initializing_activity) + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("JobSeekerSearchJobActivity", "Activity initialization error", e);
            finish();
        }

        pickResumeLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        resumeUriToUpload = result.getData().getData();
                        if (jobToApply != null && resumeUriToUpload != null) {
                            submitApplication(jobToApply, resumeUriToUpload); // Submit application after resume selection
                        } else {
                            Toast.makeText(this, "Error: Job or resume data missing after selection.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupRecyclerView() {
        jobAdapter = new JobAdapter(new JobAdapter.OnJobClickListener() {
            @Override
            public void onApplyClick(Job job) {
                jobToApply = job;
                // Check employment status before allowing application (assuming this logic was intended)
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    Toast.makeText(JobSeekerSearchJobActivity.this, R.string.please_log_in, Toast.LENGTH_SHORT).show();
                    return;
                }

                String currentUserId = currentUser.getUid();
                db.collection("users").document(currentUserId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String employmentStatus = documentSnapshot.getString("employmentStatus");

                                if ("EMPLOYED".equals(employmentStatus)) {
                                    // Check for employer ID, must not apply to same employer (assuming this logic was intended)
                                    String currentEmployerId = documentSnapshot.getString("currentEmployerId");
                                    if (currentEmployerId != null && job != null && job.getEmployerId() != null && currentEmployerId.equals(job.getEmployerId())) {
                                        Toast.makeText(JobSeekerSearchJobActivity.this,
                                                R.string.cannot_apply_to_current_employer,
                                                Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    // Warn user they are currently employed (assuming this logic was intended)
                                    new AlertDialog.Builder(JobSeekerSearchJobActivity.this)
                                            .setTitle(R.string.currently_employed)
                                            .setMessage(R.string.employed_warning)
                                            .setPositiveButton(R.string.yes_continue, (dialog, which) -> {
                                                selectResumeForUpload(); // Allow application
                                            })
                                            .setNegativeButton(R.string.cancel, null)
                                            .show();
                                } else if ("END_OF_CONTRACT".equals(employmentStatus)) {
                                    selectResumeForUpload(); // End of contract, can apply
                                } else {
                                    selectResumeForUpload(); // Available or any other status, can apply directly
                                }
                            } else {
                                // No user profile, should not happen, but allow anyway
                                selectResumeForUpload();
                            }
                        })
                        .addOnFailureListener(e -> {
                            // Error checking status, allow anyway but log error
                            Log.e("JobSeekerSearchJobActivity", "Error checking employment status", e);
                            selectResumeForUpload();
                        });
            }

            @Override
            public void onJobClick(Job job) {
                showJobDescriptionDialog(job);
            }

            @Override
            public void onSaveJobClick(Job job) {
                // TODO: Implement save job logic
            }
        });
        binding.recyclerViewJobs.setAdapter(jobAdapter);
        binding.recyclerViewJobs.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupSearchAndFilter() {
        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                performSearch(s.toString());
            }
        });
        binding.textInputLayoutSearch.setEndIconOnClickListener(v -> showFilterDialog());
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadRecentJobs);
    }

    private void showFilterDialog() {
        JobFilterBottomSheet filterSheet = JobFilterBottomSheet.newInstance();
        filterSheet.setOnFilterAppliedListener(this);
        filterSheet.show(getSupportFragmentManager(), "JobFilter");
    }

    private void loadRecentJobs() {
        binding.swipeRefreshLayout.setRefreshing(true);

        try {
            db.collection("jobs")
                    .orderBy("postedDate", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        allJobs.clear(); // Clear previous data
                        List<Job> loadedJobs = new ArrayList<>();
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            try {
                                Job job = document.toObject(Job.class);
                                if (job != null) {
                                    job.setId(document.getId());

                                    // Validate and set required fields with null checks
                                    String title = document.getString("title");
                                    String employerId = document.getString("employerId");

                                    if (title != null && !title.isEmpty() && employerId != null && !employerId.isEmpty()) {
                                        job.setTitle(title);
                                        job.setEmployerId(employerId);

                                        // Set other fields with null checks
                                        String companyName = document.getString("company");
                                        String location = document.getString("location");
                                        Object salaryMinObj = document.get("salaryMin");
                                        Object salaryMaxObj = document.get("salaryMax");
                                        String workArrangement = document.getString("workArrangement");
                                        String workSetup = document.getString("workSetup");
                                        String experienceLevel = document.getString("experienceLevel");

                                        job.setCompanyName(companyName != null ? companyName : "");
                                        job.setLocation(location != null ? location : "");
                                        job.setSalaryMin(salaryMinObj != null ? ((Number) salaryMinObj).longValue() : 0L);
                                        job.setSalaryMax(salaryMaxObj != null ? ((Number) salaryMaxObj).longValue() : 0L);
                                        job.setWorkArrangement(workArrangement != null ? workArrangement : "");
                                        job.setWorkSetup(workSetup != null ? workSetup : "");
                                        job.setExperienceLevel(experienceLevel != null ? experienceLevel : "");
                                        job.setPostedDate(document.getTimestamp("postedDate"));

                                        loadedJobs.add(job);
                                        Log.d("JobSeekerSearchJobActivity", "Loaded Job: " + (job.getTitle() != null ? job.getTitle() : "N/A") + ", Salary: " + job.getSalaryMin() + " - " + job.getSalaryMax());
                                    } else {
                                        Log.w("JobSeekerSearchJobActivity", "Missing required fields for job: " + document.getId());
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("JobSeekerSearchJobActivity", "Error processing job document: " + document.getId(), e);
                            }
                        }

                        // Sort jobs by postedDate if needed
                        loadedJobs.sort((j1, j2) -> {
                            if (j1.getPostedDate() == null || j2.getPostedDate() == null) {
                                return 0;
                            }
                            return j2.getPostedDate().compareTo(j1.getPostedDate());
                        });

                        allJobs.addAll(loadedJobs); // Store all fetched jobs
                        filterJobs(); // Apply current filter/search after loading

                        binding.swipeRefreshLayout.setRefreshing(false);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, getString(R.string.failed_to_load_jobs) + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("JobSeekerSearchJobActivity", "Error fetching jobs", e);
                        updateEmptyState(true);
                        binding.swipeRefreshLayout.setRefreshing(false);
                    });
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_loading_jobs) + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("JobSeekerSearchJobActivity", "Error in loadRecentJobs", e);
            binding.swipeRefreshLayout.setRefreshing(false);
        }
    }

    private boolean matchesSearchQuery(Job job, String query) {
        if (job == null) return false;

        String queryLower = query.toLowerCase();
        // Check if query matches title, description, company name, location, or employment type with null checks
        return (job.getTitle() != null && job.getTitle().toLowerCase().contains(queryLower)) ||
                (job.getDescription() != null && job.getDescription().toLowerCase().contains(queryLower)) ||
                (job.getCompanyName() != null && job.getCompanyName().toLowerCase().contains(queryLower)) ||
                (job.getLocation() != null && job.getLocation().toLowerCase().contains(queryLower)) ||
                (job.getEmploymentType() != null && job.getEmploymentType().toLowerCase().contains(queryLower));
    }

    private void performSearch(String query) {
        List<Job> filteredJobs = new ArrayList<>();
        String currentQuery = query.toLowerCase();

        // If query is empty and no filter is applied, show all jobs
        if (currentQuery.isEmpty() && currentFilter == null) {
            filteredJobs.addAll(allJobs);
        } else {
            for (Job job : allJobs) {
                // Apply both search query and current filters
                if (matchesSearchQuery(job, currentQuery) && (currentFilter == null || matchesFilter(job, currentFilter))) {
                    filteredJobs.add(job);
                }
            }
        }

        jobAdapter.submitList(filteredJobs);
        updateEmptyState(filteredJobs.isEmpty());
    }

    @Override
    public void onFiltersApplied(JobFilter filter) {
        this.currentFilter = filter;
        // Apply search and filter on the in-memory list
        performSearch(binding.editTextSearch.getText().toString());
    }

    private void filterJobs() {
        String currentQuery = binding.editTextSearch.getText().toString();
        List<Job> filteredJobs = new ArrayList<>();
        for (Job job : allJobs) {
            // Apply both search query and current filters
            if (matchesSearchQuery(job, currentQuery) && matchesFilter(job, currentFilter)) {
                filteredJobs.add(job);
            }
        }
        jobAdapter.submitList(filteredJobs);
        updateEmptyState(filteredJobs.isEmpty());
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    private boolean matchesFilter(Job job, JobFilter filter) {
        if (job == null || filter == null) return true; // If no job or filter, consider it a match
        boolean matches = true;

        // Filter by employment types with null checks
        if (filter.getEmploymentTypes() != null && !filter.getEmploymentTypes().isEmpty()) {
            matches &= job.getWorkArrangement() != null && filter.getEmploymentTypes().contains(job.getWorkArrangement());
        }

        // Filter by experience levels with null checks
        if (filter.getExperienceLevels() != null && !filter.getExperienceLevels().isEmpty()) {
            matches &= job.getExperienceLevel() != null && filter.getExperienceLevels().contains(job.getExperienceLevel());
        }

        // Filter by location (case-insensitive partial match) with null checks
        if (filter.getLocation() != null && !filter.getLocation().isEmpty()) {
            matches &= job.getLocation() != null && job.getLocation().toLowerCase().contains(filter.getLocation().toLowerCase());
        }

        // TODO: Add more filtering criteria as needed (e.g., salary range, job category)

        return matches;
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.recyclerViewJobs.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void showJobDescriptionDialog(Job job) {
        if (job == null) return;
        new AlertDialog.Builder(this)
                .setTitle(job.getTitle() != null ? job.getTitle() : "Job Details") // Add null check for title
                .setMessage(job.getDescription() != null ? job.getDescription() : "No description available.") // Add null check for description
                .setPositiveButton(R.string.close, null)
                .show();
    }

    private void selectResumeForUpload() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        // Corrected MIME types with proper spelling
        String[] mimeTypes = {"application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        pickResumeLauncher.launch(Intent.createChooser(intent, getString(R.string.select_resume)));
    }

    private void submitApplication(Job job, Uri resumeUri) {
        if (job == null || resumeUri == null) {
            Toast.makeText(this, "Error: Job or resume missing.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.please_log_in, Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        // First, upload the resume
        uploadResumeToStorage(userId, resumeUri, new OnResumeUploadListener() {
            @Override
            public void onSuccess(String resumeUrl) {
                // Then, save the application details to Firestore
                saveApplicationToFirestore(job, userId, resumeUrl);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(JobSeekerSearchJobActivity.this, "Resume upload failed: " + errorMessage, Toast.LENGTH_SHORT).show();
                Log.e("JobSeekerSearchJobActivity", "Resume upload failed", new Exception(errorMessage));
            }
        });
    }

    private void uploadResumeToStorage(String userId, Uri resumeUri, OnResumeUploadListener listener) {
        // Consider generating a more robust and unique file name
        String fileName = "resumes/" + userId + "_" + System.currentTimeMillis() + ".pdf";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(fileName);

        storageRef.putFile(resumeUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    if (listener != null) {
                        listener.onSuccess(uri.toString());
                    }
                }))
                .addOnFailureListener(e -> {
                    if (listener != null) {
                        listener.onFailure(e.getMessage());
                    }
                });
    }

    private void saveApplicationToFirestore(Job job, String userId, String resumeUrl) {
        // Create a new application object/map
        Map<String, Object> application = new java.util.HashMap<>();
        application.put("jobId", job.getId());
        application.put("jobseekerId", userId);
        application.put("employerId", job.getEmployerId()); // Assuming Job object has employerId
        application.put("resumeUrl", resumeUrl);
        application.put("status", "Pending"); // Initial status
        application.put("applicationDate", FieldValue.serverTimestamp());

        db.collection("applications")
                .add(application)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(JobSeekerSearchJobActivity.this, "Successfully applied for the job!", Toast.LENGTH_SHORT).show();
                    Log.d("JobSeekerSearchJobActivity", "Application submitted with ID: " + documentReference.getId());

                    // TODO: Add recruiter notification logic here
                    // You would typically use a service like Firebase Cloud Messaging (FCM)
                    // to send a notification to the recruiter about the new application.
                    // This requires implementing FCM in your project and sending a message
                    // to the recruiter's device token or a topic they are subscribed to.

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(JobSeekerSearchJobActivity.this, "Failed to submit application: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("JobSeekerSearchJobActivity", "Error submitting application", e);
                });
    }

    // Helper interface for resume upload callback
    private interface OnResumeUploadListener {
        void onSuccess(String resumeUrl);

        void onFailure(String errorMessage);
    }
}

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
import com.worksy.R;
import com.google.firebase.firestore.FieldValue;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.provider.OpenableColumns; // Import necessary for getFileNameFromUri

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

        try {
            db = FirebaseFirestore.getInstance();

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, R.string.please_log_in, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            setupRecyclerView();
            setupSearchAndFilter();
            loadRecentJobs();

            pickResumeLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            resumeUriToUpload = result.getData().getData();
                            if (resumeUriToUpload != null) {
                                // This callback is now primarily for the Upload Resume button
                                uploadResumeOnly(resumeUriToUpload);
                            } else {
                                Toast.makeText(this, "Error: No resume selected.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Resume selection cancelled or failed.", Toast.LENGTH_SHORT).show();
                        }
                    });

        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_initializing_activity) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("JobSeekerSearchJobActivity", "Activity initialization error", e);
            finish();
        }
    }

    private void setupRecyclerView() {
        jobAdapter = new JobAdapter(new JobAdapter.OnJobClickListener() {
            @Override
            public void onApplyClick(Job job) {
                if (job == null) {
                    Toast.makeText(JobSeekerSearchJobActivity.this, "Error: Cannot apply to a null job.", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    Toast.makeText(JobSeekerSearchJobActivity.this, R.string.please_log_in, Toast.LENGTH_SHORT).show();
                    return;
                }

                String currentUserId = currentUser.getUid();

                // Fetch user's profile to check for resume
                db.collection("users").document(currentUserId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                String employmentStatus = documentSnapshot.getString("employmentStatus");
                                String profileResumeUrl = documentSnapshot.getString("resumeUrl"); // Get profile resume URL

                                if (profileResumeUrl == null || profileResumeUrl.isEmpty()) {
                                    // No resume uploaded to profile
                                    Toast.makeText(JobSeekerSearchJobActivity.this, "Please upload your resume to your profile first using the Upload Resume button.", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                // If currently employed, show warning
                                if ("EMPLOYED".equals(employmentStatus)) {
                                    // Check for employer ID, must not apply to same employer
                                    String currentEmployerId = documentSnapshot.getString("currentEmployerId");
                                    if (currentEmployerId != null && job.getEmployerId() != null && currentEmployerId.equals(job.getEmployerId())) {
                                        // Cannot apply to current employer
                                        Toast.makeText(JobSeekerSearchJobActivity.this,
                                                R.string.cannot_apply_to_current_employer,
                                                Toast.LENGTH_LONG).show();
                                        return;
                                    }

                                    new AlertDialog.Builder(JobSeekerSearchJobActivity.this)
                                            .setTitle(R.string.currently_employed)
                                            .setMessage(R.string.employed_warning)
                                            .setPositiveButton(R.string.yes_continue, (dialog, which) -> {
                                                // Proceed with application using profile resume
                                                submitApplication(job, Uri.parse(profileResumeUrl)); // Use profile resume URL
                                            })
                                            .setNegativeButton(R.string.cancel, null)
                                            .show();
                                } else {
                                    // Not employed or end of contract, apply directly using profile resume
                                    submitApplication(job, Uri.parse(profileResumeUrl)); // Use profile resume URL
                                }
                            } else {
                                // User profile not found, prompt to complete profile and upload resume
                                Toast.makeText(JobSeekerSearchJobActivity.this, "Please complete your profile and upload a resume using the Upload Resume button.", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("JobSeekerSearchJobActivity", "Error checking employment status or profile", e);
                            Toast.makeText(JobSeekerSearchJobActivity.this, "Error checking profile status. Please try again.", Toast.LENGTH_SHORT).show();
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

            @Override
            public void onUploadResumeClick(Job job) {
                // This button should allow the user to upload a resume to their profile
                // It should not initiate a job application
                selectResumeForUpload(); // Use the existing method for general resume upload
            }
        });
        binding.recyclerViewJobs.setAdapter(jobAdapter);
        binding.recyclerViewJobs.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupSearchAndFilter() {
        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
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
        updateEmptyState(false); // Show loading state

        try {
            db.collection("jobs")
                    .orderBy("timestamp", Query.Direction.DESCENDING) // Changed from "postedDate" to "timestamp"
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        Log.d("JobSeekerSearchJobActivity", "Firestore query successful. Documents found: " + querySnapshot.size());
                        allJobs.clear();
                        List<Job> loadedJobs = new ArrayList<>();

                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            try {
                                // Manually extract data and create Job object
                                String id = document.getId();
                                String title = document.getString("jobTitle"); // Use "jobTitle" as in Firestore
                                String companyName = document.getString("company");
                                String location = document.getString("location");
                                String description = document.getString("description");
                                String employmentType = document.getString("employmentType");
                                String rawSalaryRange = document.getString("salaryRange"); // Get the raw string
                                String experienceLevel = document.getString("experienceLevel");
                                String employerId = document.getString("employerId");
                                String jobCategory = document.getString("jobCategory");
                                String workSetup = document.getString("workSetup");
                                String workArrangement = document.getString("workArrangement");
                                com.google.firebase.Timestamp postedDate = document.getTimestamp("timestamp"); // Get timestamp as postedDate

                                // Basic validation for required fields
                                if (id != null && title != null && !title.isEmpty() && employerId != null && !employerId.isEmpty()) {
                                    Job job = new Job();
                                    job.setId(id);
                                    job.setTitle(title);
                                    job.setCompanyName(companyName);
                                    job.setLocation(location);
                                    job.setDescription(description);
                                    job.setEmploymentType(employmentType);
                                    job.setExperienceLevel(experienceLevel);
                                    job.setEmployerId(employerId);
                                    job.setJobCategory(jobCategory);
                                    job.setWorkSetup(workSetup);
                                    job.setWorkArrangement(workArrangement);
                                    job.setPostedDate(postedDate); // Set the timestamp to the Job object

                                    // Handle salary parsing
                                    if (rawSalaryRange != null) {
                                        job.setRawSalaryRange(rawSalaryRange); // This should call parseSalaryRange internally
                                    } else {
                                        // Log a warning if salaryRange is missing
                                        Log.w("JobSeekerSearchJobActivity", "Missing salaryRange for job: " + id);
                                        job.setSalaryMin(0);
                                        job.setSalaryMax(0);
                                        job.setSalaryCurrency("");
                                    }

                                    loadedJobs.add(job);
                                    Log.d("JobSeekerSearchJobActivity", "Loaded Job: " + job.getTitle() + ", Employer ID: " + job.getEmployerId() + ", Salary: " + job.getFormattedSalary());
                                } else {
                                    Log.w("JobSeekerSearchJobActivity", "Skipping job document due to missing required fields: " + document.getId());
                                }
                            } catch (Exception e) {
                                Log.e("JobSeekerSearchJobActivity", "Error processing job document: " + document.getId(), e);
                            }
                        }

                        allJobs.addAll(loadedJobs);
                        // Display all jobs initially
                        jobAdapter.submitList(allJobs);
                        updateEmptyState(allJobs.isEmpty());
                        binding.swipeRefreshLayout.setRefreshing(false);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, getString(R.string.failed_to_load_jobs) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("JobSeekerSearchJobActivity", "Error fetching jobs", e);
                        updateEmptyState(true);
                        binding.swipeRefreshLayout.setRefreshing(false);
                    });
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_loading_jobs) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("JobSeekerSearchJobActivity", "Error in loadRecentJobs", e);
            binding.swipeRefreshLayout.setRefreshing(false);
            updateEmptyState(true);
        }
    }

    private void performSearch(String query) {
        if (allJobs == null) {
            allJobs = new ArrayList<>();
        }

        List<Job> filteredJobs = new ArrayList<>();
        String currentQuery = query != null ? query.toLowerCase().trim() : "";

        // If query is empty, show all jobs
        if (currentQuery.isEmpty()) {
            jobAdapter.submitList(allJobs);
            updateEmptyState(allJobs.isEmpty());
            return;
        }

        // Otherwise filter based on search query
        for (Job job : allJobs) {
            if (job != null && matchesSearchQuery(job, currentQuery) && matchesFilter(job, currentFilter)) {
                filteredJobs.add(job);
            }
        }

        jobAdapter.submitList(filteredJobs);
        updateEmptyState(filteredJobs.isEmpty());
    }

    private boolean matchesSearchQuery(Job job, String query) {
        if (job == null || query == null || query.isEmpty()) return true;

        String queryLower = query.toLowerCase();
        return (job.getTitle() != null && job.getTitle().toLowerCase().contains(queryLower)) ||
                (job.getDescription() != null && job.getDescription().toLowerCase().contains(queryLower)) ||
                (job.getCompanyName() != null && job.getCompanyName().toLowerCase().contains(queryLower)) ||
                (job.getLocation() != null && job.getLocation().toLowerCase().contains(queryLower)) ||
                (job.getEmploymentType() != null && job.getEmploymentType().toLowerCase().contains(queryLower));
    }

    @Override
    public void onFiltersApplied(JobFilter filter) {
        this.currentFilter = filter;
        performSearch(binding.editTextSearch.getText().toString().trim()); // Apply search after filter
    }

    private void filterJobs() {
        String currentQuery = binding.editTextSearch.getText().toString().trim();
        List<Job> filteredJobs = new ArrayList<>();
        for (Job job : allJobs) {
            if (matchesSearchQuery(job, currentQuery) && matchesFilter(job, currentFilter)) {
                filteredJobs.add(job);
            }
        }
        jobAdapter.submitList(filteredJobs);
        updateEmptyState(filteredJobs.isEmpty());
        binding.swipeRefreshLayout.setRefreshing(false);
    }

    private boolean matchesFilter(Job job, JobFilter filter) {
        if (job == null) return false;
        if (filter == null) return true;

        boolean matches = true;

        if (filter.getEmploymentTypes() != null && !filter.getEmploymentTypes().isEmpty()) {
            matches &= job.getWorkArrangement() != null && filter.getEmploymentTypes().contains(job.getWorkArrangement());
        }

        if (filter.getExperienceLevels() != null && !filter.getExperienceLevels().isEmpty()) {
            matches &= job.getExperienceLevel() != null && filter.getExperienceLevels().contains(job.getExperienceLevel());
        }

        if (filter.getLocation() != null && !filter.getLocation().isEmpty()) {
            matches &= job.getLocation() != null && job.getLocation().toLowerCase().contains(filter.getLocation().toLowerCase().trim()); // Trim location
        }

        // TODO: Add more filtering criteria as needed

        return matches;
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.recyclerViewJobs.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void showJobDescriptionDialog(Job job) {
        if (job == null) return;
        new AlertDialog.Builder(this)
                .setTitle(job.getTitle() != null ? job.getTitle() : "Job Details")
                .setMessage(job.getDescription() != null ? job.getDescription() : "No description available.")
                .setPositiveButton(R.string.close, null)
                .show();
    }

    private void uploadResumeOnly(Uri resumeUri) {
        if (resumeUri == null) return;

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, R.string.please_log_in, Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String fileName = "resumes/" + userId + "_profile_resume_" + System.currentTimeMillis() + ".pdf";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child(fileName);

        storageRef.putFile(resumeUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String resumeUrl = uri.toString();
                    // Update user's Firestore profile with resume URL
                    db.collection("users").document(userId)
                            .update("resumeUrl", resumeUrl)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Resume uploaded successfully!", Toast.LENGTH_SHORT).show();
                                // Display the uploaded file name
                                displayUploadedResumeFileName(getFileNameFromUri(resumeUri));
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, "Failed to update profile: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Resume upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void selectResumeForUpload() {
        // This method is called when the "Upload Resume" button is clicked
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        String[] mimeTypes = {"application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        pickResumeLauncher.launch(Intent.createChooser(intent, getString(R.string.select_resume)));
    }

    // New method to get file name from Uri
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
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
        // Use the provided resumeUri directly, as it's already the profile resume URL

        Map<String, Object> application = new java.util.HashMap<>();
        application.put("jobId", job.getId());
        application.put("jobseekerId", userId);
        application.put("employerId", job.getEmployerId());
        application.put("resumeUrl", resumeUri.toString()); // Use the profile resume URL
        application.put("status", "Pending");
        application.put("applicationDate", FieldValue.serverTimestamp());

        db.collection("applications")
                .add(application)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(JobSeekerSearchJobActivity.this, getString(R.string.application_submitted), Toast.LENGTH_SHORT).show();
                    Log.d("JobSeekerSearchJobActivity", "Application submitted with ID: " + documentReference.getId());
                    sendNotificationToEmployer(job.getEmployerId(), job.getTitle()); // Send notification
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(JobSeekerSearchJobActivity.this, "Failed to submit application: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("JobSeekerSearchJobActivity", "Error submitting application", e);
                });
    }

    private void sendNotificationToEmployer(String employerId, String jobTitle) {
        Map<String, Object> notification = new java.util.HashMap<>();
        notification.put("recipientId", employerId);
        notification.put("type", "application");
        notification.put("title", "New Job Application");
        notification.put("body", "A job seeker has applied for your job: " + jobTitle);
        notification.put("timestamp", FieldValue.serverTimestamp());
        notification.put("isRead", false);

        db.collection("notifications")
                .add(notification)
                .addOnSuccessListener(documentReference -> {
                    Log.d("JobSeekerSearchJobActivity", "Notification sent to employer: " + employerId);
                })
                .addOnFailureListener(e -> {
                    Log.e("JobSeekerSearchJobActivity", "Failed to send notification", e);
                });
    }

    // Helper interface for resume upload callback
    private interface OnResumeUploadListener {
        void onSuccess(String resumeUrl);
        void onFailure(String errorMessage);
    }

    // Placeholder method to display the uploaded resume file name
    private void displayUploadedResumeFileName(String fileName) {
        // To display the uploaded resume file name, you need to add a TextView
        // to your activity_job_seeker_search_job.xml layout file.
        // Give it an ID, for example, textViewUploadedResumeFileName.
        // Then, you can uncomment and use the line below:
        // binding.textViewUploadedResumeFileName.setText("Uploaded Resume: " + fileName);
        Log.d("JobSeekerSearchJobActivity", "Uploaded resume file name: " + fileName);
        Toast.makeText(this, "Uploaded Resume: " + fileName, Toast.LENGTH_LONG).show(); // For testing
    }
}


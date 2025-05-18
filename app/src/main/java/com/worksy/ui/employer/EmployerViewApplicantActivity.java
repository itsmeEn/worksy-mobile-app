package com.worksy.ui.employer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.worksy.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;

// Represents an applicant.
class Applicant {
    private String id; // Change to String to match Firestore document ID
    private String name;
    private String applicationDate;
    private String status;
    private String resumeUrl;
    private String jobTitle;
    private String email;

    // Add a Firestore document ID field
    private String firestoreDocumentId;

    public Applicant(String id, String name, String applicationDate, String status, String resumeUrl, String jobTitle, String email) {
        this.id = id; // This might be the jobseekerId or application ID, clarify as needed
        this.name = name;
        this.applicationDate = applicationDate;
        this.status = status;
        this.resumeUrl = resumeUrl;
        this.jobTitle = jobTitle;
        this.email = email;
    }

    // Add getter and setter for firestoreDocumentId
    public String getFirestoreDocumentId() {
        return firestoreDocumentId;
    }

    public void setFirestoreDocumentId(String firestoreDocumentId) {
        this.firestoreDocumentId = firestoreDocumentId;
    }

    public String getId() { // This might be jobseekerId or application ID
        return id;
    }

    public String getName() {
        return name;
    }

    public String getApplicationDate() {
        return applicationDate;
    }

    public String getStatus() {
        return status;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getEmail() {
        return email;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

public class EmployerViewApplicantActivity extends AppCompatActivity {

    private static final String TAG = "EmployerViewApplicant";
    private RecyclerView applicantsRecyclerView;
    private ApplicantAdapter applicantAdapter;
    private List<Applicant> applicantList;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private String[] applicationStatuses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employer_view_applicant);

        try {
            // Initialize Firebase
            db = FirebaseFirestore.getInstance();
            currentUser = FirebaseAuth.getInstance().getCurrentUser();

            if (currentUser == null) {
                Toast.makeText(this, "Please log in to view applicants", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // Initialize UI elements
            initializeViews();
            
            // Get the application statuses from the resources
            applicationStatuses = getResources().getStringArray(R.array.application_statuses);
            if (applicationStatuses == null || applicationStatuses.length == 0) {
                applicationStatuses = new String[]{"Pending", "Under Review", "Accepted", "Rejected"};
            }
            
            // Set up the RecyclerView and Adapter
            applicantList = new ArrayList<>();
            // Pass the context and applicationStatuses to the adapter
            applicantAdapter = new ApplicantAdapter(this, applicantList, applicationStatuses);
            applicantsRecyclerView.setAdapter(applicantAdapter);
            applicantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

            // Load applicants
            loadApplicants();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing activity: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        applicantsRecyclerView = findViewById(R.id.applicants_recycler_view);

        if (applicantsRecyclerView == null) {
            throw new IllegalStateException("Failed to initialize applicantsRecyclerView. Check if the ID is correct in the layout file.");
        }
    }

    private void loadApplicants() {
        try {
            String employerId = currentUser.getUid();
            
            // First, get all jobs posted by this employer
            db.collection("jobs")
                .whereEqualTo("employerId", employerId)
                .get()
                .addOnSuccessListener(jobDocuments -> {
                    try {
                        List<String> jobIds = new ArrayList<>();
                        for (QueryDocumentSnapshot jobDoc : jobDocuments) {
                            jobIds.add(jobDoc.getId());
                        }

                        if (jobIds.isEmpty()) {
                            // Use runOnUiThread to update UI on the main thread
                            runOnUiThread(() -> {
                                Toast.makeText(this, "No jobs posted yet", Toast.LENGTH_SHORT).show();
                                applicantList.clear();
                                applicantAdapter.notifyDataSetChanged();
                            });
                            return;
                        }

                        // Then, get all applications for these jobs
                        db.collection("applications")
                            .whereIn("jobId", jobIds)
                            .get()
                            .addOnSuccessListener(applicationDocuments -> {
                                try {
                                    applicantList.clear();
                                    // No need for applicantId counter as we use Firestore document ID
//                                    int applicantId = 1;

                                    if (applicationDocuments.isEmpty()) {
                                        runOnUiThread(() -> {
                                            Toast.makeText(this, "No applications found.", Toast.LENGTH_SHORT).show();
                                            applicantAdapter.notifyDataSetChanged();
                                        });
                                        return;
                                    }

                                    for (QueryDocumentSnapshot applicationDoc : applicationDocuments) {
                                        String jobseekerId = applicationDoc.getString("jobseekerId");
                                        String jobId = applicationDoc.getString("jobId");
                                        String status = applicationDoc.getString("status");
                                        String resumeUrl = applicationDoc.getString("resumeUrl");
                                        String applicationDate = applicationDoc.getTimestamp("applicationDate") != null ?
                                                applicationDoc.getTimestamp("applicationDate").toDate().toString() : "N/A";

                                        // Store the Firestore document ID of the application
                                        String applicationDocumentId = applicationDoc.getId();

                                        if (jobseekerId != null && jobId != null) {
                                            // Get job seeker details
                                            db.collection("users").document(jobseekerId)
                                                .get()
                                                .addOnSuccessListener(userDoc -> {
                                                    try {
                                                        String name = userDoc.getString("name");
                                                        String email = userDoc.getString("email");

                                                        if (name != null && email != null) {
                                                            // Get job details
                                                            db.collection("jobs").document(jobId)
                                                                .get()
                                                                .addOnSuccessListener(jobDoc -> {
                                                                    try {
                                                                        String jobTitle = jobDoc.getString("jobTitle");
                                                                        
                                                                        // Use applicationDocumentId as the ID
                                                                        Applicant applicant = new Applicant(
                                                                            applicationDocumentId, // Use application document ID
                                                                            name,
                                                                            applicationDate,
                                                                            status != null ? status : "Pending",
                                                                            resumeUrl,
                                                                            jobTitle != null ? jobTitle : "Unknown Position",
                                                                            email
                                                                        );
                                                                        // Set the Firestore document ID separately
                                                                        applicant.setFirestoreDocumentId(applicationDocumentId);
                                                                        
                                                                        runOnUiThread(() -> {
                                                                            applicantList.add(applicant);
                                                                            applicantAdapter.notifyDataSetChanged();
                                                                        });
                                                                    } catch (Exception e) {
                                                                        Log.e(TAG, "Error processing job document: " + e.getMessage(), e);
                                                                    }
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Log.e(TAG, "Error getting job details: " + e.getMessage(), e);
                                                                });
                                                        }
                                                    } catch (Exception e) {
                                                        Log.e(TAG, "Error processing user document: " + e.getMessage(), e);
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e(TAG, "Error getting user details: " + e.getMessage(), e);
                                                });
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "Error processing applications: " + e.getMessage(), e);
                                    runOnUiThread(() -> Toast.makeText(this, "Error loading applications: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error loading applications: " + e.getMessage(), e);
                                runOnUiThread(() -> Toast.makeText(this, "Error loading applications: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            });
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing jobs: " + e.getMessage(), e);
                        runOnUiThread(() -> Toast.makeText(this, "Error processing jobs: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading jobs: " + e.getMessage(), e);
                    runOnUiThread(() -> Toast.makeText(this, "Error loading jobs: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                });
        } catch (Exception e) {
            Log.e(TAG, "Error in loadApplicants: " + e.getMessage(), e);
            runOnUiThread(() -> Toast.makeText(this, "Error loading applicants: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    // Modified to update status for a specific applicant item
    private void handleUpdateStatus(Applicant applicant, String newStatus) {
        if (applicant != null && applicant.getFirestoreDocumentId() != null) {
            db.collection("applications").document(applicant.getFirestoreDocumentId())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    applicant.setStatus(newStatus);
                    // No need to notifyDataSetChanged here, the adapter handles the change
                    // Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Status updated for application: " + applicant.getFirestoreDocumentId());
                    
                    // Add Toast to confirm status update
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update status: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Error updating status for application: " + applicant.getFirestoreDocumentId(), e);
                });
        } else {
            Log.e(TAG, "Cannot update status: Applicant or Firestore Document ID is null");
            Toast.makeText(this, "Error updating status.", Toast.LENGTH_SHORT).show();
        }
    }

    // Modified to schedule interview for a specific applicant item
    private void handleScheduleInterview(Applicant applicant, String interviewDate) {
        if (applicant != null && applicant.getFirestoreDocumentId() != null) {
            if (interviewDate.isEmpty()) {
                Toast.makeText(this, "Please enter an interview date", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentStatus = applicant.getStatus();

            if (currentStatus != null && currentStatus.equals("Accepted")) {
                // Store the interview date in the database here.
                 db.collection("applications").document(applicant.getFirestoreDocumentId())
                     .update("interviewDate", interviewDate, // Assuming 'interviewDate' field in applications collection
                             "status", "Interview Scheduled") // Optionally update status
                     .addOnSuccessListener(aVoid -> {
                         // Update the local applicant object if status was changed
                         applicant.setStatus("Interview Scheduled");
                         // No need to notifyDataSetChanged here
                         Toast.makeText(this, "Interview scheduled for " + interviewDate, Toast.LENGTH_SHORT).show();
                         Log.d(TAG, "Interview scheduled for application: " + applicant.getFirestoreDocumentId());
                     })
                     .addOnFailureListener(e -> {
                         Toast.makeText(this, "Failed to schedule interview: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                         Log.e(TAG, "Error scheduling interview for application: " + applicant.getFirestoreDocumentId(), e);
                     });
            } else {
                Toast.makeText(this, "Cannot schedule interview. Applicant status is not Accepted.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "Cannot schedule interview: Applicant or Firestore Document ID is null");
            Toast.makeText(this, "Error scheduling interview.", Toast.LENGTH_SHORT).show();
        }
    }

    // Adapter for the RecyclerView
    private class ApplicantAdapter extends RecyclerView.Adapter<ApplicantAdapter.ApplicantViewHolder> {

        private Context context;
        private List<Applicant> applicantList;
        private String[] applicationStatuses;

        public ApplicantAdapter(Context context, List<Applicant> applicantList, String[] applicationStatuses) {
            this.context = context;
            this.applicantList = applicantList;
            this.applicationStatuses = applicationStatuses;
        }

        @NonNull
        @Override
        public ApplicantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.applicant_item_layout, parent, false);
            return new ApplicantViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ApplicantViewHolder holder, int position) {
            Applicant applicant = applicantList.get(position);
            holder.applicantNameTextView.setText(getString(R.string.applicant_name, applicant.getName()));
            holder.applicationDateTextView.setText(getString(R.string.application_date, applicant.getApplicationDate()));
            holder.statusTextView.setText(getString(R.string.status, applicant.getStatus()));
            holder.jobTitleTextView.setText(getString(R.string.job_title_applicant, applicant.getJobTitle()));

            // Set up status spinner for this item
            ArrayAdapter<String> statusSpinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, applicationStatuses);
            statusSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            holder.statusSpinner.setAdapter(statusSpinnerAdapter);

            // Set the spinner's selection to the current status of the applicant
            if (applicant.getStatus() != null) {
                int spinnerPosition = statusSpinnerAdapter.getPosition(applicant.getStatus());
                if (spinnerPosition >= 0) {
                    holder.statusSpinner.setSelection(spinnerPosition);
                }
            }

            // Click listener for the View Resume button
            holder.viewResumeButton.setOnClickListener(v -> {
                String resumeUrl = applicant.getResumeUrl();
                if (resumeUrl != null && !resumeUrl.isEmpty()) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(resumeUrl));
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Log.e(TAG, "Error viewing resume: " + e.getMessage(), e);
                        Toast.makeText(context, "Could not open resume.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Resume not available", Toast.LENGTH_SHORT).show();
                }
            });

            // Click listener for the Send Email button
            holder.sendEmailButton.setOnClickListener(v -> {
                String email = applicant.getEmail();
                if (email != null && !email.isEmpty()) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"));
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Regarding your application for " + applicant.getJobTitle());
                        if (intent.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error sending email: " + e.getMessage(), e);
                        Toast.makeText(context, "Could not open email app.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Email address not available", Toast.LENGTH_SHORT).show();
                }
            });

            // Click listener for the Update Status button
            holder.updateStatusButton.setOnClickListener(v -> {
                String newStatus = holder.statusSpinner.getSelectedItem().toString();
                handleUpdateStatus(applicant, newStatus);
            });

            // Click listener for the Schedule Interview button
            holder.scheduleInterviewButton.setOnClickListener(v -> {
                String interviewDate = holder.interviewDateEditText.getText().toString().trim();
                handleScheduleInterview(applicant, interviewDate);
            });
        }

        @Override
        public int getItemCount() {
            return applicantList.size();
        }

        public class ApplicantViewHolder extends RecyclerView.ViewHolder {
            TextView applicantNameTextView;
            TextView applicationDateTextView;
            TextView statusTextView;
            Button viewResumeButton;
            TextView jobTitleTextView;
            Button sendEmailButton;
            Spinner statusSpinner;
            Button updateStatusButton;
            EditText interviewDateEditText;
            Button scheduleInterviewButton;

            public ApplicantViewHolder(@NonNull View itemView) {
                super(itemView);
                applicantNameTextView = itemView.findViewById(R.id.applicant_name_text_view);
                applicationDateTextView = itemView.findViewById(R.id.application_date_text_view);
                statusTextView = itemView.findViewById(R.id.status_text_view);
                viewResumeButton = itemView.findViewById(R.id.view_resume_button);
                jobTitleTextView = itemView.findViewById(R.id.job_title_text_view);
                sendEmailButton = itemView.findViewById(R.id.send_email_button);
                // Initialize new views from applicant_item_layout.xml
                statusSpinner = itemView.findViewById(R.id.status_spinner);
                updateStatusButton = itemView.findViewById(R.id.update_status_button);
                interviewDateEditText = itemView.findViewById(R.id.interview_date_edit_text);
                scheduleInterviewButton = itemView.findViewById(R.id.schedule_interview_button);
            }
        }
    }
}
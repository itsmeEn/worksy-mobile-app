package com.worksy.ui.employer;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

// Represents an applicant.
class Applicant {
    private int id;
    private String name;
    private String applicationDate;
    private String status;
    private String resumeUrl;
    private String jobTitle;
    private String email;

    public Applicant(int id, String name, String applicationDate, String status, String resumeUrl, String jobTitle, String email) {
        this.id = id;
        this.name = name;
        this.applicationDate = applicationDate;
        this.status = status;
        this.resumeUrl = resumeUrl;
        this.jobTitle = jobTitle;
        this.email = email;
    }

    public int getId() {
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

    private RecyclerView applicantsRecyclerView;
    private ApplicantAdapter applicantAdapter;
    private List<Applicant> applicantList;
    private TextView selectedApplicantNameTextView;
    private Spinner statusSpinner;
    private Button updateStatusButton;
    private EditText interviewDateEditText;
    private Button scheduleInterviewButton;

    private int selectedApplicantId = -1;
    private String[] applicationStatuses;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employer_view_applicant);

        // Initialize UI elements
        applicantsRecyclerView = findViewById(R.id.applicants_recycler_view);
        selectedApplicantNameTextView = findViewById(R.id.selected_applicant_name);
        statusSpinner = findViewById(R.id.status_spinner);
        updateStatusButton = findViewById(R.id.update_status_button);
        interviewDateEditText = findViewById(R.id.interview_date_edit_text);
        scheduleInterviewButton = findViewById(R.id.schedule_interview_button);

        // Get the application statuses from the resources
        applicationStatuses = getResources().getStringArray(R.array.application_statuses);
        // Set up the Spinner.
        ArrayAdapter<String> statusSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, applicationStatuses);
        statusSpinner.setAdapter(statusSpinnerAdapter);

        // Initialize the applicant list and adapter
        applicantList = new ArrayList<>();
        applicantAdapter = new ApplicantAdapter(this, applicantList);
        applicantsRecyclerView.setAdapter(applicantAdapter);
        applicantsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Populate the applicant list (for demonstration purposes)
        populateApplicantList();

        // Set click listener for the Update Status button
        updateStatusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleUpdateStatus();
            }
        });

        // Set click listener for the Schedule Interview button
        scheduleInterviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleScheduleInterview();
            }
        });
    }

    private void populateApplicantList() {
        //  Replace this with actual data retrieval from your database or data source.
        //  The status should be "Pending" when the applicant first applies.
        applicantList.add(new Applicant(1, "John Doe", "2024-01-15", "Pending", "https://example.com/john_doe_resume.pdf", "Software Engineer", "john.doe@example.com"));
        applicantList.add(new Applicant(2, "Jane Smith", "2024-01-20", "Pending", "https://example.com/jane_smith_resume.pdf", "Data Scientist", "jane.smith@example.com"));
        applicantList.add(new Applicant(3, "Robert Jones", "2024-01-25", "Pending", "https://example.com/robert_jones_resume.pdf", "Web Developer", "robert.jones@example.com"));
        applicantAdapter.notifyDataSetChanged();
    }

    private void handleUpdateStatus() {
        if (selectedApplicantId != -1) {
            String newStatus = statusSpinner.getSelectedItem().toString();
            for (Applicant applicant : applicantList) {
                if (applicant.getId() == selectedApplicantId) {
                    applicant.setStatus(newStatus);
                    applicantAdapter.notifyDataSetChanged();
                    Toast.makeText(this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                    break;
                }
            }
            selectedApplicantId = -1;
            selectedApplicantNameTextView.setText("Selected Applicant: ");

        } else {
            Toast.makeText(this, "Please select an applicant first", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleScheduleInterview() {
        if (selectedApplicantId != -1) {
            String interviewDate = interviewDateEditText.getText().toString();
            if (interviewDate.isEmpty()) {
                Toast.makeText(this, "Please enter an interview date", Toast.LENGTH_SHORT).show();
                return;
            }
            String currentStatus = "";
            for (Applicant applicant : applicantList) {
                if (applicant.getId() == selectedApplicantId) {
                    currentStatus = applicant.getStatus();
                    break;
                }
            }

            if (currentStatus.equals("Accepted")) {
                for (Applicant applicant : applicantList) {
                    if (applicant.getId() == selectedApplicantId) {
                        // Store the interview date in the database here.
                        Toast.makeText(this, "Interview scheduled for " + interviewDate, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            } else {
                Toast.makeText(this, "Cannot schedule interview. Applicant status is not Accepted.", Toast.LENGTH_SHORT).show();
            }

            interviewDateEditText.getText().clear();
            selectedApplicantId = -1;
            selectedApplicantNameTextView.setText("Selected Applicant: ");

        } else {
            Toast.makeText(this, "Please select an applicant first", Toast.LENGTH_SHORT).show();
        }
    }

    // Adapter for the RecyclerView
    private class ApplicantAdapter extends RecyclerView.Adapter<ApplicantAdapter.ApplicantViewHolder> {

        private Context context;
        private List<Applicant> applicantList;

        public ApplicantAdapter(Context context, List<Applicant> applicantList) {
            this.context = context;
            this.applicantList = applicantList;
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
            holder.applicantNameTextView.setText("Name: " + applicant.getName());
            holder.applicationDateTextView.setText("Application Date: " + applicant.getApplicationDate());
            holder.statusTextView.setText("Status: " + applicant.getStatus());
            holder.jobTitleTextView.setText("Job Title: " + applicant.getJobTitle());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedApplicantId = applicant.getId();
                    selectedApplicantNameTextView.setText("Selected Applicant: " + applicant.getName());
                }
            });

            holder.viewResumeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String resumeUrl = applicant.getResumeUrl();
                    if (resumeUrl != null && !resumeUrl.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(resumeUrl));
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Resume not available", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            holder.sendEmailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = applicant.getEmail();
                    if (email != null && !email.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:"));
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Regarding your application");
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            context.startActivity(intent);
                        } else {
                            Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Email address not available", Toast.LENGTH_SHORT).show();
                    }
                }
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

            public ApplicantViewHolder(@NonNull View itemView) {
                super(itemView);
                applicantNameTextView = itemView.findViewById(R.id.applicant_name_text_view);
                applicationDateTextView = itemView.findViewById(R.id.application_date_text_view);
                statusTextView = itemView.findViewById(R.id.status_text_view);
                viewResumeButton = itemView.findViewById(R.id.view_resume_button);
                jobTitleTextView = itemView.findViewById(R.id.job_title_text_view);
                sendEmailButton = itemView.findViewById(R.id.send_email_button);
            }
        }
    }
}
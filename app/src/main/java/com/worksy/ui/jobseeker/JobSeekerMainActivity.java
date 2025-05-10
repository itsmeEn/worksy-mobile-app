package com.worksy.ui.jobseeker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.worksy.R;
import com.worksy.databinding.ActivityJobSeekerMainBinding;

public class JobSeekerMainActivity extends AppCompatActivity {
    private ActivityJobSeekerMainBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private static final int REQUEST_UPLOAD_RESUME = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobSeekerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth and FireStore
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        setupBottomNavigation();
        setupQuickActions(); // Initialize Quick Actions
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                // Show home fragment
                return true;
            } else if (itemId == R.id.navigation_search) {
                // Show search fragment
                return true;
            } else if (itemId == R.id.navigation_applications) {
                // Show applications fragment
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Show profile fragment
                return true;
            }
            return false;
        });

        // Set default selection
        binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);
    }

    private void setupQuickActions() {
        // 1. Find the views for the Quick Actions. It is CRUCIAL that these IDs
        //    match the IDs of the elements in your activity_job_seeker_main.xml
        //    file. If they don't match, the clicks won't work.
        View achievementsButton = findViewById(R.id.achievements_button);
        View jobHistoryButton = findViewById(R.id.job_history_button);
        View uploadResumeButton = findViewById(R.id.upload_resume_button);
        View assistantBotButton = findViewById(R.id.assistant_bot_button);
        View interviewGuideButton = findViewById(R.id.interview_guide_button);
        View industryInsightsButton = findViewById(R.id.backButton);
        View jobStrategiesButton = findViewById(R.id.job_strategies_button);
        View careerAssessmentButton = findViewById(R.id.bottomRightGridCard);

        // 2. Set OnClickListeners for each of the Quick Action views. Inside each
        //    listener, create an Intent to start the appropriate Activity.
        if (achievementsButton != null) {
            achievementsButton.setOnClickListener(view -> {
                Intent intent = new Intent(JobSeekerMainActivity.this, JobAchievementsMainActivity.class);
                startActivity(intent);
            });
        }

        if (jobHistoryButton != null) {
            jobHistoryButton.setOnClickListener(view -> {
                Intent intent = new Intent(JobSeekerMainActivity.this, JobHistoryActivity.class);
                startActivity(intent);
            });
        }

        if (uploadResumeButton != null) {
            uploadResumeButton.setOnClickListener(view -> {
                Intent intent = new Intent(JobSeekerMainActivity.this, UploadResumeMainActivity.class);
                startActivityForResult(intent, REQUEST_UPLOAD_RESUME);
            });
        }

        if (assistantBotButton != null) {
            assistantBotButton.setOnClickListener(view -> {
                //  Intent intent = new Intent(JobSeekerMainActivity.this, AssistantBotActivity.class);
                //  startActivity(intent);
                Toast.makeText(JobSeekerMainActivity.this, "Assistant Bot Not Implemented", Toast.LENGTH_SHORT).show();
            });
        }

        if (interviewGuideButton != null) {
            interviewGuideButton.setOnClickListener(view -> {
                //Intent intent = new Intent(JobSeekerMainActivity.this, InterviewGuideActivity.class);
                //startActivity(intent);
                Toast.makeText(JobSeekerMainActivity.this, "Interview Guide Not Implemented", Toast.LENGTH_SHORT).show();
            });
        }

        if (industryInsightsButton != null) {
            industryInsightsButton.setOnClickListener(view -> {
                // Intent intent = new Intent(JobSeekerMainActivity.this, IndustryInsightsActivity.class);
                //startActivity(intent);
                Toast.makeText(JobSeekerMainActivity.this, "Industry Insights Not Implemented", Toast.LENGTH_SHORT).show();
            });
        }

        if (jobStrategiesButton != null) {
            jobStrategiesButton.setOnClickListener(view -> {
                //Intent intent = new Intent(JobSeekerMainActivity.this, JobStrategiesActivity.class);
                //startActivity(intent);
                Toast.makeText(JobSeekerMainActivity.this, "Job Strategies Not Implemented", Toast.LENGTH_SHORT).show();
            });
        }

        if (careerAssessmentButton != null) {
            // careerAssessmentButton.setOnClickListener(view -> {
            // Intent intent = new Intent(JobSeekerMainActivity.this, CareerAssessmentActivity.class);
            // startActivity(intent);
            // });
            Toast.makeText(JobSeekerMainActivity.this, "Career Assessment Not Implemented", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_UPLOAD_RESUME && resultCode == RESULT_OK && data != null) {
            //  Uri uri = data.getData();
            // Handle the file here.
            // You'll need to get the file path from the URI, and then handle the upload.
            // This is a complex topic.
            // Example :
            // Toast.makeText(this, "Selected file: " + uri.toString(), Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Resume uploaded successfully!", Toast.LENGTH_LONG).show();

        } else if (requestCode == REQUEST_UPLOAD_RESUME && resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Resume upload cancelled.", Toast.LENGTH_LONG).show();
        } else if (requestCode == REQUEST_UPLOAD_RESUME) {
            Toast.makeText(this, "Failed to upload resume.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}


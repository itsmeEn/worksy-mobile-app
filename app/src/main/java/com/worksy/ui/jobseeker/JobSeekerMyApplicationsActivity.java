package com.worksy.ui.jobseeker;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import com.worksy.R;
import com.worksy.data.model.JobApplication;
import com.worksy.ui.adapter.JobApplicationAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.worksy.R;
import com.worksy.data.model.JobApplication;
import com.worksy.ui.adapter.JobApplicationAdapter;

import java.util.ArrayList;
import java.util.List;
import android.util.Log;

public class JobSeekerMyApplicationsActivity extends AppCompatActivity implements JobApplicationAdapter.OnApplicationClickListener {

    private RecyclerView recyclerViewApplications;
    private JobApplicationAdapter applicationAdapter;
    private ProgressBar progressBar;
    private TextView textViewEmpty;
    private final List<JobApplication> currentApplications = new ArrayList<>(); // To hold fetched data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.job_seeker_my_applications);

        recyclerViewApplications = findViewById(R.id.recyclerViewApplications);
        progressBar = findViewById(R.id.progressBar);
        textViewEmpty = findViewById(R.id.textViewEmpty);

        setupRecyclerView();
        fetchApplications(); // Start fetching data
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

    // --- SIMULATED DATA FETCHING ---
    // Replace this with your actual data fetching logic (ViewModel, API call, Database query)
    private void fetchApplications() {
        showLoading(true);
        
        // Get current user ID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to view applications", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        String userId = currentUser.getUid();
        
        // Fetch applications from Firebase
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("applications")
            .whereEqualTo("jobSeekerId", userId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<JobApplication> fetchedApplications = new ArrayList<>();
                
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    try {
                        JobApplication application = document.toObject(JobApplication.class);
                        if (application != null) {
                            application.setId(document.getId());
                            fetchedApplications.add(application);
                        }
                    } catch (Exception e) {
                        Log.e("FirebaseFetch", "Error converting application: " + e.getMessage());
                    }
                }
                
                // Update the list and notify adapter
                currentApplications.clear();
                currentApplications.addAll(fetchedApplications);
                applicationAdapter.notifyDataSetChanged();
                
                showLoading(false);
                
                if (fetchedApplications.isEmpty()) {
                    textViewEmpty.setVisibility(View.VISIBLE);
                    textViewEmpty.setText("No applications found");
                }
            })
            .addOnFailureListener(e -> {
                showLoading(false);
                Toast.makeText(this, "Failed to fetch applications: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FirebaseFetch", "Error fetching applications: " + e.getMessage());
            });
    }

    @Override
    public void onApplicationClick(JobApplication application) {
        // Handle click on application
        // For now, just show a toast
        // In real implementation, you might want to show more details or allow actions
    }
}
package com.worksy.ui.employer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.worksy.R;
import com.worksy.databinding.ActivityEmployerMainBinding;
import com.worksy.data.model.Job; //Import Job model
import com.worksy.ui.adapter.RecentJobAdapter; // Import adapter
import java.util.ArrayList;
import java.util.List;

public class EmployerMainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private ActivityEmployerMainBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private RecyclerView recentJobsRecyclerView; // Declare RecyclerView
    private RecentJobAdapter recentJobAdapter;
    private List<Job> recentJobList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmployerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recentJobsRecyclerView = binding.recentJobsRecyclerView; //  findViewById
        recentJobsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        recentJobList = new ArrayList<>();
        recentJobAdapter = new RecentJobAdapter(recentJobList);
        recentJobsRecyclerView.setAdapter(recentJobAdapter);

        setupBottomNavigation();
        fetchCompanyName();
        fetchRecentJobs(); // Call method to fetch jobs
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_dashboard) {
                // Don't recreate the current activity
                return true;
            } else if (itemId == R.id.navigation_post_job) {
                Intent intent = new Intent(this, EmployerJobPost.class);
                startActivity(intent);
                finish(); // Finish the current activity
                return true;
            } else if (itemId == R.id.navigation_applicants) {
                Intent intent = new Intent(this, ViewApplicant.class);
                startActivity(intent);
                finish(); // Finish the current activity
                return true;
            } else if (itemId == R.id.navigation_company) {
                Intent intent = new Intent(this, ViewApplicant.class); //since wala pa nagagawang company page as is muna to
                startActivity(intent);
                finish(); // Finish the current activity
                return true;
            }
            return false;
        });

        binding.bottomNavigation.setSelectedItemId(R.id.navigation_dashboard);
    }

    private void fetchCompanyName() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();

        firebaseFirestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String companyName = documentSnapshot.getString("companyName");
                        if (companyName != null && !companyName.isEmpty()) {
                            binding.greetingEmployerView.setText("Hi, " + companyName + "!");
                        } else {
                            binding.greetingEmployerView.setText("Hi!");
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show()
                );
    }

    private void fetchRecentJobs() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        String employerId = currentUser.getUid();

        firebaseFirestore.collection("jobs")
                .whereEqualTo("employerId", employerId)
                .orderBy("timestamp", Query.Direction.DESCENDING) //changed postedDate to timestamp
                .limit(4)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        recentJobList.clear();
                        recentJobAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "No recent job posts.", Toast.LENGTH_SHORT).show();
                        Log.d("FirestoreData", "No jobs found for employerId: " + employerId); //debugging
                        return;
                    }

                    recentJobList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Job job = document.toObject(Job.class);
                        if (job != null) {
                            job.setId(document.getId());
                            recentJobList.add(job);
                            Log.d("FirestoreData", "Job ID: " + job.getId() + ", Title: " + job.getTitle() + ", Employer ID: " + job.getEmployerId() + ", Timestamp: " + document.get("timestamp")); //debugging
                        }
                    }
                    recentJobAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch recent jobs: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("FirestoreError", "Error fetching jobs: " + e.getMessage(), e); // Include the exception
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
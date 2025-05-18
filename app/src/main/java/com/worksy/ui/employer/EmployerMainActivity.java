package com.worksy.ui.employer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import android.widget.LinearLayout;
import android.app.AlertDialog;
import com.worksy.data.model.EmployeeContract;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class EmployerMainActivity extends AppCompatActivity {

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

        LinearLayout rateEmployeeLayout = findViewById(R.id.quick_action_rate_employee); // Use the correct ID
        rateEmployeeLayout.setOnClickListener(v -> {
            fetchCompletedContractsAndShowDialog();
        });
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_dashboard) {
                // Don't recreate the current activity
                return true;
            } else if (itemId == R.id.navigation_post_job) {
                Intent intent = new Intent(this, EmployerJobPost.class);
                intent.putExtra("source", "main");
                startActivity(intent);
                //finish(); // Finish the current activity
                return true;
            } else if (itemId == R.id.navigation_applicants) {
                Intent intent = new Intent(this, EmployerViewApplicantActivity.class);
                intent.putExtra("source", "main");
                startActivity(intent);
                //finish(); // Finish the current activity
                return true;
            } else if (itemId == R.id.navigation_company) {
                Intent intent = new Intent(this, EmployerCompanyProfile.class);
                intent.putExtra("source", "main");
                startActivity(intent);
                //finish(); // Finish the current activity
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
                        Log.d("FireStoreData", "No jobs found for employerId: " + employerId); //debugging
                        return;
                    }

                    recentJobList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Job job = document.toObject(Job.class);
                        if (job != null) {
                            job.setId(document.getId());
                            recentJobList.add(job);
                            Log.d("FireStoreData", "Job ID: " + job.getId() + ", Title: " + job.getTitle() + ", Employer ID: " + job.getEmployerId() + ", Timestamp: " + document.get("timestamp")); //debugging
                        }
                    }
                    recentJobAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch recent jobs: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("FireStoreError", "Error fetching jobs: " + e.getMessage(), e); // Include the exception
                });
    }

    private void fetchCompletedContractsAndShowDialog() {
        String employerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firebaseFirestore.collection("employee_contracts")
            .whereEqualTo("employerId", employerId)
            .whereEqualTo("status", "COMPLETED")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<EmployeeContract> completedContracts = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    EmployeeContract contract = doc.toObject(EmployeeContract.class);
                    completedContracts.add(contract);
                }
                if (completedContracts.isEmpty()) {
                    Toast.makeText(this, "No completed contracts to rate.", Toast.LENGTH_SHORT).show();
                } else {
                    showEmployeeSelectionDialog(completedContracts);
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to fetch completed contracts.", Toast.LENGTH_SHORT).show();
            });
    }

    private void showEmployeeSelectionDialog(List<EmployeeContract> contracts) {
        String[] employeeTitles = new String[contracts.size()];
        for (int i = 0; i < contracts.size(); i++) {
            EmployeeContract c = contracts.get(i);
            // You can customize this string as needed
            employeeTitles[i] = c.getJobTitle() + " (" + c.getWorkArrangement() + ", " + c.getWorkSetup() + ")";
        }
        new AlertDialog.Builder(this)
            .setTitle("Select Employee to Rate")
            .setItems(employeeTitles, (dialog, which) -> {
                EmployeeContract selected = contracts.get(which);
                openRateEmployeeFragment(selected.getEmployeeId(), selected.getJobId());
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void openRateEmployeeFragment(String employeeId, String jobId) {
        findViewById(R.id.companyIdentity).setVisibility(View.GONE);
        findViewById(R.id.recentJobsRecyclerView).setVisibility(View.GONE);
        // Hide other views as needed
        RateEmployeeFragment fragment = new RateEmployeeFragment();
        Bundle args = new Bundle();
        args.putString("employeeId", employeeId);
        args.putString("jobId", jobId);
        fragment.setArguments(args);
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
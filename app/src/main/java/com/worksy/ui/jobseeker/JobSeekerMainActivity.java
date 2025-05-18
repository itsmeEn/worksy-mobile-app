package com.worksy.ui.jobseeker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.worksy.R;
import com.worksy.databinding.ActivityJobSeekerMainBinding;

public class JobSeekerMainActivity extends AppCompatActivity {
    private ActivityJobSeekerMainBinding binding;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobSeekerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth and FireStore
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        setupBottomNavigation();
        //setupQuickActions();
        setupQuickActionClicks();
        fetchUserGreeting();
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                // Already on home screen
                return true;
            } else if (itemId == R.id.navigation_search) {
                Intent intent = new Intent(this, JobSeekerSearchJobActivity.class);
                intent.putExtra("source", "main");
                startActivity(intent);
                //finish(); // Finish the current activity
                return true;
            } else if (itemId == R.id.navigation_applications) {
                Intent intent = new Intent(this, JobSeekerMyApplicationsActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.navigation_profile) {
                Intent intent = new Intent(this, JobSeekerProfileActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });

        // Set default selection
        binding.bottomNavigation.setSelectedItemId(R.id.navigation_home);
    }

    private void setupQuickActionClicks() {
        binding.quickActionAssistantBot.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatBotActivity.class);
            startActivity(intent);
        });

        binding.achievementsButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, JobSeekerAchievements.class);
            startActivity(intent);
        });

        // TODO: Add click listeners for other quick actions if needed
    }

    private void fetchUserGreeting() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        String userId = currentUser.getUid();

        firebaseFirestore.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            binding.greetingTextView.setText("Hi, " + name + "!");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch user data.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadFragment(androidx.fragment.app.Fragment fragment) {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
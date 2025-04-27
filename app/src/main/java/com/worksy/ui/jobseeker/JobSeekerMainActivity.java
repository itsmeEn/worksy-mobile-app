package com.worksy.ui.jobseeker;

import android.os.Bundle;
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
        fetchUserGreeting();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
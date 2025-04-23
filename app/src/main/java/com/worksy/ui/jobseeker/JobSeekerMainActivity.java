package com.worksy.ui.jobseeker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.navigation.NavigationBarView;
import com.worksy.R;
import com.worksy.databinding.ActivityJobSeekerMainBinding;

public class JobSeekerMainActivity extends AppCompatActivity {
    private ActivityJobSeekerMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityJobSeekerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

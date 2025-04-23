package com.worksy.ui.employer;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.navigation.NavigationBarView;
import com.worksy.R;
import com.worksy.databinding.ActivityEmployerMainBinding;

public class EmployerMainActivity extends AppCompatActivity {
    private ActivityEmployerMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmployerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupBottomNavigation();
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_dashboard) {
                // Show dashboard fragment
                return true;
            } else if (itemId == R.id.navigation_post_job) {
                // Show post job fragment
                return true;
            } else if (itemId == R.id.navigation_applicants) {
                // Show applicants fragment
                return true;
            } else if (itemId == R.id.navigation_company) {
                // Show company profile fragment
                return true;
            }
            return false;
        });

        // Set default selection
        binding.bottomNavigation.setSelectedItemId(R.id.navigation_dashboard);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

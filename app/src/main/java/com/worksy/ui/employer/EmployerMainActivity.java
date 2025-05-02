package com.worksy.ui.employer;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.worksy.R;
import com.worksy.databinding.ActivityEmployerMainBinding;

public class EmployerMainActivity extends AppCompatActivity {
    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private ActivityEmployerMainBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    private ImageView currentImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEmployerMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        setupBottomNavigation();
        setupImageUploadListeners();
        fetchCompanyName();
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

    private void setupImageUploadListeners() {
        setImageClickListener(R.id.uploadedImageViewTopLeft);
        setImageClickListener(R.id.uploadedImageViewTopRight);
        setImageClickListener(R.id.uploadedImageViewBottomLeft);
        setImageClickListener(R.id.uploadedImageViewBottomRight);
    }

    private void setImageClickListener(int imageViewId) {
        ImageView imageView = binding.getRoot().findViewById(imageViewId);
        if (imageView != null) {
            imageView.setOnClickListener(v -> dispatchTakePictureIntent(imageView));
        }
    }

    private void dispatchTakePictureIntent(ImageView imageView) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            currentImageView = imageView;
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}

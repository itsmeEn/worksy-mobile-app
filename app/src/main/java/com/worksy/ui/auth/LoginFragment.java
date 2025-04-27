package com.worksy.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.worksy.R;
import com.worksy.databinding.FragmentLoginBinding;
import com.worksy.ui.employer.EmployerMainActivity;
import com.worksy.ui.jobseeker.home.JobSeekerHomeFragment;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        setupClickListeners();
    }

    private void setupClickListeners() {
        // Handle user type selection for registration
        binding.radioGroupUserType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonJobSeeker) {
                navigateToJobSeekerRegistration();
            } else if (checkedId == R.id.radioButtonEmployer) {
                navigateToRecruiterRegistration();
            }
        });

        // Handle sign-in button click
        binding.buttonSignIn.setOnClickListener(v -> attemptLogin());

        // Handle sign-up button click
        binding.textViewSignUp.setOnClickListener(v -> showSignUpMessage());
    }

    private void navigateToJobSeekerRegistration() {
        // Navigate to the Job Seeker Registration Fragment
        ((AuthActivity) requireActivity()).showRegistrationFragment();
    }

    private void navigateToRecruiterRegistration() {
        // Navigate to the Recruiter Registration Fragment
        ((AuthActivity) requireActivity()).showRecruiterRegistrationFragment();
    }

    private void attemptLogin() {
        String email = binding.editTextEmail.getText() != null ? binding.editTextEmail.getText().toString().trim() : "";
        String password = binding.editTextPassword.getText() != null ? binding.editTextPassword.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (fAuth.getCurrentUser() != null) {
                    String userId = fAuth.getCurrentUser().getUid();

                    // Fetch the user's role from FireStore
                    fStore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String role = documentSnapshot.getString("role");

                            // Redirect to respective homepage based on role
                            if ("JobSeeker".equalsIgnoreCase(role)) {
                                redirectToJobSeekerHome();
                            } else if ("Recruiter".equalsIgnoreCase(role)) {
                                redirectToRecruiterHome();
                            } else {
                                Toast.makeText(requireContext(), "Unknown user role", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Error fetching user role: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"), Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login failed";
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToJobSeekerHome() {
        Intent intent = new Intent(requireContext(), JobSeekerHomeFragment.class);
        startActivity(intent);
        requireActivity().finish(); // Finish the current activity to prevent going back to login
    }

    private void redirectToRecruiterHome() {
        Intent intent = new Intent(requireContext(), EmployerMainActivity.class);
        startActivity(intent);
        requireActivity().finish(); // Finish the current activity to prevent going back to login
    }

    private void showSignUpMessage() {
        Toast.makeText(requireContext(), "Please select a user type to sign up", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
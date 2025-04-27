package com.worksy.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.worksy.R;
import com.worksy.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
    }

    private void setupClickListeners() {
        // Set up radio group listener for redirection
        binding.radioGroupUserType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonJobSeeker) {
                navigateToJobSeekerRegistration();
            } else if (checkedId == R.id.radioButtonEmployer) {
                navigateToRecruiterRegistration();
            }
        });

        binding.buttonSignIn.setOnClickListener(v -> attemptLogin());
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
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Logic for signing in (not relevant for the redirection issue)
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
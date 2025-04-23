package com.worksy.ui.auth;

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
import com.worksy.databinding.FragmentRegistrationBinding;
import java.util.HashMap;
import java.util.Map;

public class RegistrationFragment extends Fragment {
    private FragmentRegistrationBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.buttonRegister.setOnClickListener(v -> attemptRegistration());
        binding.buttonLogin.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void attemptRegistration() {
        String email = binding.editTextEmail.getText().toString();
        String password = binding.editTextPassword.getText().toString();
        String confirmPassword = binding.editTextConfirmPassword.getText().toString();
        boolean isJobSeeker = binding.radioGroupUserType.getCheckedRadioButtonId() == binding.radioButtonJobSeeker.getId();

        if (!validateInputs(email, password, confirmPassword)) {
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                if (authResult.getUser() != null) {
                    createUserProfile(authResult.getUser().getUid(), email, isJobSeeker);
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private boolean validateInputs(String email, String password, String confirmPassword) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (binding.radioGroupUserType.getCheckedRadioButtonId() == -1) {
            Toast.makeText(requireContext(), "Please select a user type", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void createUserProfile(String userId, String email, boolean isJobSeeker) {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("email", email);
        userProfile.put("userType", isJobSeeker ? "jobSeeker" : "employer");
        userProfile.put("createdAt", System.currentTimeMillis());

        db.collection("users")
            .document(userId)
            .set(userProfile)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                navigateToMainActivity(isJobSeeker);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to create profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void navigateToMainActivity(boolean isJobSeeker) {
        // We'll implement this later when we create the main activities
        Toast.makeText(requireContext(), "Registration successful! Redirecting...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

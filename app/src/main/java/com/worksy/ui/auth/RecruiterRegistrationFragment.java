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
import com.worksy.databinding.FragmentRecruiterRegistrationBinding;
import java.util.HashMap;
import java.util.Map;

public class RecruiterRegistrationFragment extends Fragment {
    private FragmentRecruiterRegistrationBinding binding;
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
        binding = FragmentRecruiterRegistrationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.buttonRegister.setOnClickListener(v -> attemptRecruiterRegistration());
        binding.buttonLogin.setOnClickListener(v -> requireActivity().onBackPressed());
        binding.buttonBack.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void attemptRecruiterRegistration() {
        String email = binding.editTextEmail.getText().toString();
        String password = binding.editTextPassword.getText().toString();
        String confirmPassword = binding.editTextConfirmPassword.getText().toString();
        String companyName = binding.editTextCompanyName.getText().toString();

        if (!validateInputs(email, password, confirmPassword, companyName)) {
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    if (authResult.getUser() != null) {
                        createRecruiterProfile(authResult.getUser().getUid(), email, companyName);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private boolean validateInputs(String email, String password, String confirmPassword, String companyName) {
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || companyName.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(requireContext(), "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void createRecruiterProfile(String userId, String email, String companyName) {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("email", email);
        userProfile.put("userType", "recruiter");
        userProfile.put("companyName", companyName);
        userProfile.put("createdAt", System.currentTimeMillis());

        db.collection("users")
                .document(userId)
                .set(userProfile)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to create profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToMainActivity() {
        // Implement navigation to the recruiter's main activity
        Toast.makeText(requireContext(), "Registration successful! Redirecting...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

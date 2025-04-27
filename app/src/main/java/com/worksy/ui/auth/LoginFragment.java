package com.worksy.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.worksy.R;
import com.worksy.databinding.FragmentLoginBinding;
import com.worksy.ui.employer.EmployerMainActivity;
import com.worksy.ui.jobseeker.JobSeekerMainActivity;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private GoogleSignInClient googleSignInClient;

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

        setupGoogleSignIn();
        setupClickListeners();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
    }

    private void setupClickListeners() {
        binding.radioGroupUserType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButtonJobSeeker) {
                navigateToRegistrationFragment();
            } else if (checkedId == R.id.radioButtonEmployer) {
                navigateToRecruiterRegistrationFragment();
            }
        });

        binding.buttonSignIn.setOnClickListener(this::attemptLogin);
        binding.buttonGoogleSignIn.setOnClickListener(this::signInWithGoogle);
        binding.buttonPhoneSignIn.setOnClickListener(v -> navigateToPhoneAuth());
        binding.textViewForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        binding.textViewSignUp.setOnClickListener(v -> showSignUpMessage());
    }

    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    try {
                        GoogleSignInAccount account = task.getResult(Exception.class);
                        if (account != null) {
                            handleGoogleSignIn(account);
                        }
                    } catch (Exception e) {
                        Log.e("Google Sign-In", "Google sign-in failed", e);
                        Toast.makeText(requireContext(), "Google sign-in failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void signInWithGoogle(View v) {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void navigateToPhoneAuth() {
        PhoneAuthFragment phoneAuthFragment = new PhoneAuthFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_container, phoneAuthFragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToRegistrationFragment() {
        RegistrationFragment registrationFragment = new RegistrationFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_container, registrationFragment)
                .addToBackStack(null)
                .commit();
    }

    private void navigateToRecruiterRegistrationFragment() {
        RecruiterRegistrationFragment recruiterFragment = new RecruiterRegistrationFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.auth_container, recruiterFragment)
                .addToBackStack(null)
                .commit();
    }

    private void attemptLogin(View v) {
        String email = binding.editTextEmail.getText() != null ? binding.editTextEmail.getText().toString().trim() : "";
        String password = binding.editTextPassword.getText() != null ? binding.editTextPassword.getText().toString().trim() : "";

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = fAuth.getCurrentUser();
                if (user != null) {
                    redirectToDashboard(user.getUid());
                }
            } else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Login failed";
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleGoogleSignIn(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        fAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser user = fAuth.getCurrentUser();
                if (user != null) {
                    redirectToDashboard(user.getUid());
                }
            } else {
                Toast.makeText(requireContext(), "Google sign-in failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectToDashboard(String userId) {
        fStore.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String userType = documentSnapshot.getString("userType");
                if ("jobSeeker".equalsIgnoreCase(userType)) {
                    redirectToJobSeekerHome();
                } else if ("recruiter".equalsIgnoreCase(userType)) {
                    redirectToRecruiterHome();
                } else {
                    Toast.makeText(requireContext(), "Unknown user type. Contact support.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(requireContext(), "User data not found. Contact support.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(requireContext(), "Error fetching user data", Toast.LENGTH_SHORT).show());
    }

    private void redirectToJobSeekerHome() {
        Intent intent = new Intent(requireContext(), JobSeekerMainActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private void redirectToRecruiterHome() {
        Intent intent = new Intent(requireContext(), EmployerMainActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showForgotPasswordDialog() {
        String email = binding.editTextEmail.getText() != null ? binding.editTextEmail.getText().toString().trim() : "";

        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Enter your email to reset your password", Toast.LENGTH_SHORT).show();
            return;
        }

        fAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) Toast.makeText(requireContext(), "Password reset email sent", Toast.LENGTH_SHORT).show();
            else {
                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Error sending reset email";
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
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
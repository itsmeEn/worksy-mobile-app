package com.worksy.ui.auth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.worksy.R;
import com.worksy.databinding.FragmentLoginBinding;
import com.worksy.ui.employer.EmployerMainActivity;
import com.worksy.ui.jobseeker.JobSeekerMainActivity;
import java.util.concurrent.TimeUnit;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        setupGoogleSignIn();
        setupGoogleSignInLauncher();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.google_client_token))
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    private void setupGoogleSignInLauncher() {
        googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    handleGoogleSignInResult(task);
                }
            }
        );
    }

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
        binding.buttonSignIn.setOnClickListener(v -> attemptLogin());
        binding.textViewSignUp.setOnClickListener(v -> navigateToRegistration());
        binding.buttonGoogleSignIn.setOnClickListener(v -> signInWithGoogle());
        binding.buttonPhoneSignIn.setOnClickListener(v -> signInWithPhone());
        binding.textViewForgotPassword.setOnClickListener(v -> handleForgotPassword());
    }

    private void attemptLogin() {
        String email = binding.editTextEmail.getText().toString().trim();
        String password = binding.editTextPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener(authResult -> {
                checkUserTypeAndNavigate();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Toast.makeText(requireContext(), "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
            .addOnSuccessListener(authResult -> {
                checkUserTypeAndNavigate();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Authentication failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void signInWithPhone() {
        // Navigate to phone authentication fragment
        ((AuthActivity) requireActivity()).showPhoneAuthFragment();
    }

    private void handleForgotPassword() {
        String email = binding.editTextEmail.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your email address", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
            .addOnSuccessListener(unused -> {
                Toast.makeText(requireContext(), "Password reset email sent", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to send reset email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void checkUserTypeAndNavigate() {
        // In a real app, you would check the user type in Firestore
        // For now, we'll just navigate to JobSeeker as an example
        startActivity(new Intent(requireContext(), JobSeekerMainActivity.class));
        requireActivity().finish();
    }

    private void navigateToRegistration() {
        ((AuthActivity) requireActivity()).showRegistrationFragment();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

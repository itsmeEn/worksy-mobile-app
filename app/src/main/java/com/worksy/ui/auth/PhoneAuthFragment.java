package com.worksy.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.worksy.databinding.FragmentPhoneAuthBinding;
import com.worksy.ui.jobseeker.JobSeekerMainActivity;
import android.content.Intent;
import java.util.concurrent.TimeUnit;

public class PhoneAuthFragment extends Fragment {
    private static final String TAG = "PhoneAuthFragment";
    private static final String TEST_PHONE_NUMBER = "+16505553434"; // Firebase test number
    private FragmentPhoneAuthBinding binding;
    private FirebaseAuth auth;
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPhoneAuthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
        
        // Pre-fill with test phone number for development
        binding.editTextPhone.setText(TEST_PHONE_NUMBER);
    }

    private void setupClickListeners() {
        binding.buttonSendCode.setOnClickListener(v -> sendVerificationCode());
        binding.buttonVerify.setOnClickListener(v -> verifyCode());
        binding.buttonResend.setOnClickListener(v -> resendVerificationCode());
        binding.buttonBack.setOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void sendVerificationCode() {
        String phoneNumber = binding.editTextPhone.getText().toString().trim();
        if (phoneNumber.isEmpty()) {
            binding.editTextPhone.setError("Phone number is required");
            return;
        }

        // Format phone number to E.164 format
        if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+1" + phoneNumber; // Default to US format
        }

        Log.d(TAG, "Attempting to send verification code to: " + phoneNumber);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.buttonSendCode.setEnabled(false);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        Log.d(TAG, "onVerificationCompleted: Auto-verification completed");
                        binding.progressBar.setVisibility(View.GONE);
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.e(TAG, "onVerificationFailed", e);
                        binding.progressBar.setVisibility(View.GONE);
                        binding.buttonSendCode.setEnabled(true);
                        
                        String errorMessage;
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            errorMessage = "Invalid phone number format. Please enter a valid number.";
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            errorMessage = "Too many requests. Please try again later.";
                        } else {
                            errorMessage = "Verification failed: " + e.getMessage() + 
                                         "\nMake sure you've added the SHA-1 fingerprint to Firebase Console.";
                        }
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                        
                        // Log additional information for debugging
                        Log.e(TAG, "Error class: " + e.getClass().getName());
                        Log.e(TAG, "Error message: " + e.getMessage());
                        if (e.getCause() != null) {
                            Log.e(TAG, "Cause: " + e.getCause().getMessage());
                        }
                    }

                    @Override
                    public void onCodeSent(@NonNull String vId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        Log.d(TAG, "onCodeSent: Verification code sent successfully");
                        binding.progressBar.setVisibility(View.GONE);
                        verificationId = vId;
                        resendToken = token;
                        binding.layoutVerification.setVisibility(View.VISIBLE);
                        binding.layoutPhone.setVisibility(View.GONE);
                        binding.buttonResend.setEnabled(true);
                        Toast.makeText(requireContext(), "Verification code sent", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void resendVerificationCode() {
        if (resendToken == null) {
            Toast.makeText(requireContext(), "Cannot resend code at this time", Toast.LENGTH_SHORT).show();
            return;
        }

        String phoneNumber = binding.editTextPhone.getText().toString().trim();
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.buttonResend.setEnabled(false);

        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(requireActivity())
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        binding.progressBar.setVisibility(View.GONE);
                        signInWithPhoneAuthCredential(credential);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.buttonResend.setEnabled(true);
                        Toast.makeText(requireContext(), "Failed to resend code: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String vId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                        binding.progressBar.setVisibility(View.GONE);
                        verificationId = vId;
                        resendToken = token;
                        Toast.makeText(requireContext(), "New verification code sent", Toast.LENGTH_SHORT).show();
                    }
                })
                .setForceResendingToken(resendToken)
                .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyCode() {
        String code = binding.editTextCode.getText().toString().trim();
        if (code.isEmpty()) {
            binding.editTextCode.setError("Please enter the verification code");
            return;
        }

        Log.d(TAG, "Attempting to verify code");
        binding.progressBar.setVisibility(View.VISIBLE);
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        Log.d(TAG, "Attempting to sign in with phone credential");
        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    Log.d(TAG, "signInWithCredential:success");
                    binding.progressBar.setVisibility(View.GONE);
                    startActivity(new Intent(requireContext(), JobSeekerMainActivity.class));
                    requireActivity().finish();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "signInWithCredential:failure", e);
                    binding.progressBar.setVisibility(View.GONE);
                    String errorMessage;
                    if (e instanceof FirebaseAuthInvalidCredentialsException) {
                        errorMessage = "Invalid verification code. Please try again.";
                    } else {
                        errorMessage = "Authentication failed: " + e.getMessage();
                    }
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

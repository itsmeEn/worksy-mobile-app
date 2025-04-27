package com.worksy.ui.auth;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.worksy.R;

public class AuthActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        if (savedInstanceState == null) {
            showLoginFragment();
        }
    }

    public void showLoginFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_container, new LoginFragment())
                .commit();
    }

    public void showRegistrationFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_container, new RegistrationFragment())
                .addToBackStack(null)
                .commit();
    }

    public void showRecruiterRegistrationFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.auth_container, new RecruiterRegistrationFragment())
                .addToBackStack(null)
                .commit();
    }
}
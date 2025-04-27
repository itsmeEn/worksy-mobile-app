package com.worksy.ui.auth;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import com.worksy.R;

public class RoleSelectionDialog extends Dialog {
    private final RoleSelectionListener listener;

    public RoleSelectionDialog(Context context, RoleSelectionListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_role_selection); // Ensure this layout file exists

        // Set up Job Seeker button
        findViewById(R.id.buttonJobSeeker).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRoleSelected("JobSeeker");
                dismiss();
            }
        });

        // Set up Recruiter button
        findViewById(R.id.buttonRecruiter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onRoleSelected("Recruiter");
                dismiss();
            }
        });
    }

    // Listener interface
    public interface RoleSelectionListener {
        void onRoleSelected(String selectedRole);
    }
}
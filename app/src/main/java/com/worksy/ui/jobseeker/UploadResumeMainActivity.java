package com.worksy.ui.jobseeker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import com.worksy.R;

public class UploadResumeMainActivity extends AppCompatActivity {

    private static final int PICK_RESUME_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_resume); // Assuming your layout file is named activity_upload_resume_main.xml

        Button chooseFileButton = findViewById(R.id.uploadButton);

        chooseFileButton.setOnClickListener(v -> openFileChooser());
    }

    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf|application/msword|application/vnd.openxmlformats-officedocument.wordprocessingml.document"); // Filter for PDF, DOC, DOCX
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(intent, PICK_RESUME_REQUEST);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, "No file chooser app found.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_RESUME_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri resumeUri = data.getData();
            // Handle the selected resume URI here. You can:
            // 1. Get the file path (with necessary permissions and handling).
            // 2. Upload the file to a server.
            // 3. Store the URI locally.

            Toast.makeText(this, "Resume selected: " + resumeUri.getLastPathSegment(), Toast.LENGTH_LONG).show();
            // You would typically proceed with the upload or further processing here.
        }
    }
}

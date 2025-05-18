package com.worksy.ui.jobseeker;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.worksy.R;
import com.worksy.data.model.Certificate;
import com.worksy.ui.adapter.CertificateAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class JobSeekerAchievements extends AppCompatActivity {

    private Button buttonUploadCertificate;
    private RecyclerView recyclerViewCertificates;
    private TextView textViewEmptyState;
    private CertificateAdapter certificateAdapter;
    private List<Certificate> certificateList = new ArrayList<>();

    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private ActivityResultLauncher<Intent> pickCertificateLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_seeker_achievement);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize Views
        buttonUploadCertificate = findViewById(R.id.buttonUploadCertificate);
        recyclerViewCertificates = findViewById(R.id.recyclerViewCertificates);
        textViewEmptyState = findViewById(R.id.textViewEmptyState);

        // Setup RecyclerView
        recyclerViewCertificates.setLayoutManager(new LinearLayoutManager(this));
        certificateAdapter = new CertificateAdapter(certificateList); // Pass the list to the adapter
        recyclerViewCertificates.setAdapter(certificateAdapter);

        // Register for activity result to pick files
        pickCertificateLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri certificateUri = result.getData().getData();
                        uploadCertificate(certificateUri); // Call upload function
                    }
                });

        // Set click listener for the upload button
        buttonUploadCertificate.setOnClickListener(v -> openFilePicker());

        // Fetch existing certificates
        fetchCertificates();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // Allow all file types initially
        String[] mimeTypes = {
                "application/pdf",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .docx
                "image/*" // Images
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        pickCertificateLauncher.launch(Intent.createChooser(intent, "Select Certificate"));
    }

    private void uploadCertificate(Uri certificateUri) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to upload certificates.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (certificateUri == null) {
            Toast.makeText(this, "No file selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String fileName = "certificates/" + userId + "/" + System.currentTimeMillis() + "_" + getFileName(certificateUri);

        StorageReference certificateRef = storage.getReference().child(fileName);

        certificateRef.putFile(certificateUri)
                .addOnSuccessListener(taskSnapshot -> {
                    certificateRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveCertificateMetadata(userId, fileName, uri.toString());
                    }).addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("Achievements", "Failed to get download URL", e);
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Achievements", "Upload failed", e);
                });
    }

    private void saveCertificateMetadata(String userId, String fileName, String downloadUrl) {
        Map<String, Object> certificateData = new HashMap<>();
        certificateData.put("fileName", fileName);
        certificateData.put("downloadUrl", downloadUrl);
        certificateData.put("uploadDate", com.google.firebase.firestore.FieldValue.serverTimestamp());
        // You might want to extract and store the actual file name without the path
        String displayFileName = fileName.substring(fileName.lastIndexOf('/') + 1);
        certificateData.put("displayName", displayFileName);

        db.collection("users").document(userId).collection("certificates")
                .add(certificateData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Certificate uploaded successfully!", Toast.LENGTH_SHORT).show();
                    Log.d("Achievements", "Certificate metadata saved with ID: " + documentReference.getId());
                    fetchCertificates(); // Refresh the list after upload
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save certificate metadata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Achievements", "Failed to save metadata", e);
                });
    }

    private void fetchCertificates() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // Handle not logged in state, perhaps clear the list and show empty state
            certificateList.clear();
            certificateAdapter.notifyDataSetChanged();
            updateEmptyState(true);
            return;
        }

        String userId = currentUser.getUid();

        db.collection("users").document(userId).collection("certificates")
                .orderBy("uploadDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    certificateList.clear(); // Clear existing list
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        // Assuming Certificate model has a constructor or method to create from DocumentSnapshot
                         Certificate certificate = document.toObject(Certificate.class);
                         if (certificate != null) {
                             certificate.setId(document.getId()); // Set document ID as certificate ID
                             certificateList.add(certificate);
                         }
                    }
                    certificateAdapter.notifyDataSetChanged(); // Notify adapter of data changes
                    updateEmptyState(certificateList.isEmpty());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch certificates: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Achievements", "Failed to fetch certificates", e);
                    updateEmptyState(true);
                });
    }

    private void updateEmptyState(boolean isEmpty) {
        recyclerViewCertificates.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        textViewEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    // Helper to get file name from Uri
    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (android.database.Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            }
        } else if (uri.getScheme().equals("file")) {
            result = new java.io.File(uri.getPath()).getName();
        }
        return result == null ? "uploaded_file" : result;
    }

    // TODO: Create Certificate data model and CertificateAdapter class
}

package com.worksy.ui.employer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.worksy.R;  // Import the R class
import com.worksy.data.model.EmployerCompanyModel; // Import EmployerCompanyModel

import java.util.Objects;

public class EmployerCompanyProfile extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView companyLogoImageView;
    private TextInputEditText companyNameInput;
    private TextInputEditText companyAddressInput;
    private TextInputEditText industrySpecializationInput;
    private TextInputEditText foundedYearInput;
    private TextInputEditText companyBenefitsInput;
    private TextInputEditText companyCultureInput;
    private MaterialButton saveProfileButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private Uri imageUri;
    private String logoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employer_company_profile);

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Initialize UI elements
        companyLogoImageView = findViewById(R.id.companyLogo);
        companyNameInput = findViewById(R.id.companyNameInput);
        companyAddressInput = findViewById(R.id.companyAddressInput);
        industrySpecializationInput = findViewById(R.id.industrySpecializationInput);
        foundedYearInput = findViewById(R.id.foundedYearInput);
        companyBenefitsInput = findViewById(R.id.companyBenefitsInput);
        companyCultureInput = findViewById(R.id.companyCultureInput);
        saveProfileButton = findViewById(R.id.saveProfileButton);

        // Set click listener for the change logo button
        findViewById(R.id.changeLogoButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the camera
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        // Load company data
        loadCompanyData();

        // Set click listener for the save profile button
        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCompanyData();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Get the image URI
            imageUri = data.getData();
            Glide.with(this)
                    .load(imageUri)
                    .into(companyLogoImageView);
        }
    }

    private void loadCompanyData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        firebaseFirestore.collection("employers").document(userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                EmployerCompanyModel company = document.toObject(EmployerCompanyModel.class);
                                if (company != null) {
                                    logoUrl = company.getLogoUrl();
                                    if (logoUrl != null && !logoUrl.isEmpty()) {
                                        Glide.with(EmployerCompanyProfile.this)
                                                .load(logoUrl)
                                                .into(companyLogoImageView);
                                    }
                                    companyNameInput.setText(company.getCompanyName());
                                    companyAddressInput.setText(company.getCompanyAddress());
                                    industrySpecializationInput.setText(company.getIndustrySpecialization());
                                    foundedYearInput.setText(company.getFoundedYear());
                                    companyBenefitsInput.setText(company.getCompanyBenefits());
                                    companyCultureInput.setText(company.getCompanyCulture());
                                }
                            } else {
                                Log.d("loadCompanyData", "No such document");
                            }
                        } else {
                            Log.d("loadCompanyData", "get failed with ", task.getException());
                            Toast.makeText(EmployerCompanyProfile.this, "Failed to load company data", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveCompanyData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        String companyName = Objects.requireNonNull(companyNameInput.getText()).toString().trim();
        String companyAddress = Objects.requireNonNull(companyAddressInput.getText()).toString().trim();
        String industrySpecialization = Objects.requireNonNull(industrySpecializationInput.getText()).toString().trim();
        String foundedYear = Objects.requireNonNull(foundedYearInput.getText()).toString().trim();
        String companyBenefits = Objects.requireNonNull(companyBenefitsInput.getText()).toString().trim();
        String companyCulture = Objects.requireNonNull(companyCultureInput.getText()).toString().trim();

        // Basic input validation
        if (TextUtils.isEmpty(companyName) || TextUtils.isEmpty(companyAddress) ||
                TextUtils.isEmpty(industrySpecialization) || TextUtils.isEmpty(foundedYear) ||
                TextUtils.isEmpty(companyBenefits) || TextUtils.isEmpty(companyCulture)) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert foundedYear to string


        // Create a EmployerCompanyModel object
        EmployerCompanyModel company = new EmployerCompanyModel();
        company.setCompanyName(companyName);
        company.setCompanyAddress(companyAddress);
        company.setIndustrySpecialization(industrySpecialization);
        company.setFoundedYear(foundedYear);
        company.setCompanyBenefits(companyBenefits);
        company.setCompanyCulture(companyCulture);
        if (logoUrl != null && !logoUrl.isEmpty()) {
            company.setLogoUrl(logoUrl);
        }

        // Upload the image and get the download URL
        if (imageUri != null) {
            StorageReference imageRef = storageReference.child("images/company_logos/" + userId + ".jpg");
            UploadTask uploadTask = imageRef.putFile(imageUri);

            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        if (downloadUri != null) {
                            logoUrl = downloadUri.toString();
                            company.setLogoUrl(logoUrl);
                        }
                        // Save the company data to Firestore
                        saveCompanyDataToFirestore(userId, company);
                    } else {
                        // Handle the error
                        Log.e("saveCompanyData", "Failed to upload image", task.getException());
                        Toast.makeText(EmployerCompanyProfile.this, "Failed to upload company logo.", Toast.LENGTH_SHORT).show();
                        // Save the company data to Firestore even if the image upload fails
                        saveCompanyDataToFirestore(userId, company);
                    }
                }
            });
        } else {
            // Save the company data to Firestore without uploading a new image
            saveCompanyDataToFirestore(userId, company);
        }
    }

    private void saveCompanyDataToFirestore(String userId, EmployerCompanyModel company) {
        firebaseFirestore.collection("employers").document(userId)
                .set(company)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(EmployerCompanyProfile.this, "Company profile updated successfully.", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Log.e("saveCompanyData", "Failed to save company data", task.getException());
                            Toast.makeText(EmployerCompanyProfile.this, "Failed to update company profile.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}


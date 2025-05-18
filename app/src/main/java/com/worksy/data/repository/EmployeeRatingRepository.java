package com.worksy.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.worksy.data.model.EmployeeRating;

public class EmployeeRatingRepository {
    private static final String COLLECTION_RATINGS = "employee_ratings";
    private final FirebaseFirestore db;

    public EmployeeRatingRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public Task<Void> createRating(EmployeeRating rating) {
        return db.collection(COLLECTION_RATINGS)
                .document()
                .set(rating);
    }

    public Task<QuerySnapshot> getEmployeeRatings(String employeeId) {
        return db.collection(COLLECTION_RATINGS)
                .whereEqualTo("employeeId", employeeId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get();
    }

    public Task<QuerySnapshot> getEmployerRatings(String employerId) {
        return db.collection(COLLECTION_RATINGS)
                .whereEqualTo("employerId", employerId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get();
    }

    public Task<QuerySnapshot> getJobRatings(String jobId) {
        return db.collection(COLLECTION_RATINGS)
                .whereEqualTo("jobId", jobId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get();
    }
}
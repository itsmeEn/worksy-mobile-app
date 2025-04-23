package com.worksy.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.worksy.data.model.Job;
import java.util.List;
import java.util.ArrayList;

public class JobRepository {
    private static final String COLLECTION_JOBS = "jobs";
    private final FirebaseFirestore db;

    public JobRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public Task<QuerySnapshot> getRecentJobs(int limit) {
        return db.collection(COLLECTION_JOBS)
                .whereEqualTo("status", "ACTIVE")
                .orderBy("postedDate", Query.Direction.DESCENDING)
                .limit(limit)
                .get();
    }

    public Task<QuerySnapshot> searchJobs(String query, List<String> filters) {
        Query baseQuery = db.collection(COLLECTION_JOBS)
                .whereEqualTo("status", "ACTIVE");

        if (query != null && !query.isEmpty()) {
            // Search in title and description
            baseQuery = baseQuery.orderBy("title")
                    .startAt(query)
                    .endAt(query + "\uf8ff");
        }

        return baseQuery.get();
    }

    public Task<QuerySnapshot> getJobsByEmployer(String employerId) {
        return db.collection(COLLECTION_JOBS)
                .whereEqualTo("employerId", employerId)
                .orderBy("postedDate", Query.Direction.DESCENDING)
                .get();
    }

    public Task<Void> createJob(Job job) {
        return db.collection(COLLECTION_JOBS)
                .document()
                .set(job);
    }

    public Task<Void> updateJob(Job job) {
        return db.collection(COLLECTION_JOBS)
                .document(job.getId())
                .set(job);
    }

    public Task<Void> deleteJob(String jobId) {
        return db.collection(COLLECTION_JOBS)
                .document(jobId)
                .delete();
    }
}

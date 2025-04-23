package com.worksy.ui.jobseeker.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.worksy.data.model.Job;
import com.worksy.data.repository.JobRepository;
import com.worksy.databinding.FragmentJobSeekerHomeBinding;
import com.worksy.ui.adapter.JobAdapter;
import com.worksy.ui.jobseeker.filter.JobFilter;
import com.worksy.ui.jobseeker.filter.JobFilterBottomSheet;
import java.util.ArrayList;
import java.util.List;

public class JobSeekerHomeFragment extends Fragment implements JobAdapter.OnJobClickListener, JobFilterBottomSheet.OnFilterAppliedListener {
    private FragmentJobSeekerHomeBinding binding;
    private JobRepository jobRepository;
    private JobAdapter jobAdapter;
    private JobFilter currentFilter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        jobRepository = new JobRepository();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentJobSeekerHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupSearchAndFilter();
        loadRecentJobs();
        setupSwipeRefresh();
    }

    private void setupRecyclerView() {
        jobAdapter = new JobAdapter(this);
        binding.recyclerViewJobs.setAdapter(jobAdapter);
        binding.recyclerViewJobs.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

    private void setupSearchAndFilter() {
        binding.editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                performSearch(s.toString());
            }
        });

        binding.textInputLayoutSearch.setEndIconOnClickListener(v -> showFilterDialog());
    }

    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::loadRecentJobs);
    }

    private void showFilterDialog() {
        JobFilterBottomSheet filterSheet = JobFilterBottomSheet.newInstance();
        filterSheet.setOnFilterAppliedListener(this);
        filterSheet.show(getChildFragmentManager(), "JobFilter");
    }

    private void performSearch(String query) {
        binding.swipeRefreshLayout.setRefreshing(true);
        
        List<String> filters = currentFilter != null && currentFilter.hasFilters() 
            ? buildFilterList(currentFilter) 
            : new ArrayList<>();

        jobRepository.searchJobs(query, filters)
            .addOnSuccessListener(querySnapshot -> {
                List<Job> jobs = new ArrayList<>();
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    Job job = document.toObject(Job.class);
                    if (job != null) {
                        job.setId(document.getId());
                        jobs.add(job);
                    }
                }
                jobAdapter.submitList(jobs);
                updateEmptyState(jobs.isEmpty());
                binding.swipeRefreshLayout.setRefreshing(false);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.swipeRefreshLayout.setRefreshing(false);
            });
    }

    private List<String> buildFilterList(JobFilter filter) {
        List<String> filters = new ArrayList<>();
        if (filter.getEmploymentTypes() != null) {
            filters.addAll(filter.getEmploymentTypes());
        }
        if (filter.getExperienceLevels() != null) {
            filters.addAll(filter.getExperienceLevels());
        }
        if (filter.getLocation() != null && !filter.getLocation().isEmpty()) {
            filters.add(filter.getLocation());
        }
        return filters;
    }

    private void loadRecentJobs() {
        binding.swipeRefreshLayout.setRefreshing(true);
        
        jobRepository.getRecentJobs(20)
            .addOnSuccessListener(querySnapshot -> {
                List<Job> jobs = new ArrayList<>();
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    Job job = document.toObject(Job.class);
                    if (job != null) {
                        job.setId(document.getId());
                        jobs.add(job);
                    }
                }
                jobAdapter.submitList(jobs);
                updateEmptyState(jobs.isEmpty());
                binding.swipeRefreshLayout.setRefreshing(false);
            })
            .addOnFailureListener(e -> {
                Toast.makeText(requireContext(), "Failed to load jobs: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                binding.swipeRefreshLayout.setRefreshing(false);
                updateEmptyState(true);
            });
    }

    private void updateEmptyState(boolean isEmpty) {
        binding.recyclerViewJobs.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        binding.layoutEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onJobClick(Job job) {
        // Navigate to job details
        // TODO: Implement navigation to JobDetailsFragment
        Toast.makeText(requireContext(), "Viewing " + job.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveJobClick(Job job) {
        // Save job to user's saved jobs
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        // TODO: Implement save job functionality
        Toast.makeText(requireContext(), "Job saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFiltersApplied(JobFilter filter) {
        this.currentFilter = filter;
        performSearch(binding.editTextSearch.getText().toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

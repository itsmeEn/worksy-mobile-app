package com.worksy.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.worksy.data.model.Job;
import com.worksy.databinding.ItemJobBinding;
import java.util.ArrayList;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {
    private final List<Job> jobs = new ArrayList<>();
    private final OnJobClickListener listener;

    public interface OnJobClickListener {
        void onJobClick(Job job);
        void onSaveJobClick(Job job);
        void onApplyClick(Job job);
        void onUploadResumeClick(Job job);
    }

    public JobAdapter(OnJobClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public JobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemJobBinding binding = ItemJobBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new JobViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull JobViewHolder holder, int position) {
        Job job = jobs.get(position);
        holder.bind(job, listener);
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    public void submitList(List<Job> newJobs) {
        if (newJobs == null) {
            newJobs = new ArrayList<>();
        }
        
        // Create a final copy of the list for use in DiffUtil
        final List<Job> finalNewJobs = new ArrayList<>(newJobs);
        
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return jobs.size();
            }

            @Override
            public int getNewListSize() {
                return finalNewJobs.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                Job oldJob = jobs.get(oldItemPosition);
                Job newJob = finalNewJobs.get(newItemPosition);
                return oldJob != null && newJob != null && 
                       oldJob.getId() != null && oldJob.getId().equals(newJob.getId());
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Job oldJob = jobs.get(oldItemPosition);
                Job newJob = finalNewJobs.get(newItemPosition);
                return oldJob != null && newJob != null && 
                       oldJob.equals(newJob);
            }
        });

        jobs.clear();
        jobs.addAll(finalNewJobs);
        diffResult.dispatchUpdatesTo(this);
    }

    public static class JobViewHolder extends RecyclerView.ViewHolder {
        private final ItemJobBinding binding;

        JobViewHolder(ItemJobBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Job job, OnJobClickListener listener) {
            if (job == null) return;
            
            // Set text with null checks
            binding.textViewJobTitle.setText(job.getTitle() != null ? job.getTitle() : "");
            binding.textViewCompanyName.setText(job.getCompanyName() != null ? job.getCompanyName() : "");
            binding.textViewLocation.setText(job.getLocation() != null ? job.getLocation() : "");
            binding.textViewSalary.setText(job.getFormattedSalary() != null ? job.getFormattedSalary() : "");
            binding.textViewWorkSetup.setText(job.getWorkSetup() != null ? job.getWorkSetup() : "");
            binding.textViewWorkArrangement.setText(job.getWorkArrangement() != null ? job.getWorkArrangement() : "");
            binding.chipEmploymentType.setText(job.getEmploymentType() != null ? job.getEmploymentType() : "");
            binding.textViewExperienceLevel.setText(job.getExperienceLevel() != null ? job.getExperienceLevel() : "");

            // Set click listeners
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onJobClick(job);
                }
            });

            binding.buttonApplyNow.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onApplyClick(job);
                }
            });

            // Set click listener for Upload Resume button
            binding.buttonUploadResume.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUploadResumeClick(job);
                }
            });
        }
    }
}

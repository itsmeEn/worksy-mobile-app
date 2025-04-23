package com.worksy.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.worksy.data.model.Job;
import com.worksy.databinding.ItemJobBinding;
import java.util.ArrayList;
import java.util.List;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.JobViewHolder> {
    private List<Job> jobs = new ArrayList<>();
    private OnJobClickListener listener;

    public interface OnJobClickListener {
        void onJobClick(Job job);
        void onSaveJobClick(Job job);
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
        holder.bind(jobs.get(position));
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    public void submitList(List<Job> newJobs) {
        jobs.clear();
        jobs.addAll(newJobs);
        notifyDataSetChanged();
    }

    class JobViewHolder extends RecyclerView.ViewHolder {
        private final ItemJobBinding binding;

        JobViewHolder(ItemJobBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Job job) {
            binding.textViewJobTitle.setText(job.getTitle());
            binding.textViewCompanyName.setText(job.getCompanyName());
            binding.textViewLocation.setText(job.getLocation());
            binding.textViewSalary.setText(job.getFormattedSalary());
            binding.chipEmploymentType.setText(job.getEmploymentType());

            // Set click listeners
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    listener.onJobClick(job);
                }
            });

            binding.buttonSave.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onSaveJobClick(job);
                }
            });
        }
    }
}

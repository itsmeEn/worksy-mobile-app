package com.worksy.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.worksy.data.model.Job;
import java.util.List;
import com.worksy.R;

public class RecentJobAdapter extends RecyclerView.Adapter<RecentJobAdapter.RecentJobViewHolder> {

    private List<Job> jobList;

    public RecentJobAdapter(List<Job> jobList) {
        this.jobList = jobList;
    }

    @NonNull
    @Override
    public RecentJobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_job, parent, false);
        return new RecentJobViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentJobViewHolder holder, int position) {
        Job job = jobList.get(position);
        holder.textViewJobTitle.setText(job.getTitle());
        holder.textViewJobCategory.setText(job.getJobCategory());
        holder.textViewWorkArrangement.setText(job.getWorkArrangement());
        holder.textViewLocation.setText(job.getLocation());
        holder.textViewExperienceLevel.setText(job.getExperienceLevel());
        holder.textViewWorkSetUp.setText(job.getWorkSetup());
    }

    @Override
    public int getItemCount() {
        return jobList.size();
    }

    public static class RecentJobViewHolder extends RecyclerView.ViewHolder {

        public TextView textViewJobTitle;
        public TextView textViewJobCategory;
        public TextView textViewWorkArrangement;
        public TextView textViewLocation;
        public TextView textViewWorkSetUp;
        public TextView textViewExperienceLevel;

        public RecentJobViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewJobTitle = itemView.findViewById(R.id.textViewJobTitle);
            textViewJobCategory = itemView.findViewById(R.id.textViewJobCategory);
            textViewWorkArrangement = itemView.findViewById(R.id.textViewWorkArrangement);
            textViewLocation = itemView.findViewById(R.id.textViewLocation);
            textViewWorkSetUp = itemView.findViewById(R.id.textViewWorkSetUp);
            textViewExperienceLevel = itemView.findViewById(R.id.textViewExperienceLevel);
        }
    }
}
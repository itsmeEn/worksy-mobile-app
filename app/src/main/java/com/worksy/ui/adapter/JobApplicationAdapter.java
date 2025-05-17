package com.worksy.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.worksy.R;
import com.worksy.data.model.JobApplication;
import java.util.List;

public class JobApplicationAdapter extends RecyclerView.Adapter<JobApplicationAdapter.JobApplicationViewHolder> {
    private final List<JobApplication> applications;
    private final OnApplicationClickListener listener;

    public interface OnApplicationClickListener {
        void onApplicationClick(JobApplication application);
    }

    public JobApplicationAdapter(List<JobApplication> applications, OnApplicationClickListener listener) {
        this.applications = applications;
        this.listener = listener;
    }

    @NonNull
    @Override
    public JobApplicationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_job_applications, parent, false);
        return new JobApplicationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull JobApplicationViewHolder holder, int position) {
        JobApplication application = applications.get(position);
        holder.bind(application, listener);
    }

    @Override
    public int getItemCount() {
        return applications.size();
    }

    public static class JobApplicationViewHolder extends RecyclerView.ViewHolder {
        private final TextView jobTitle;
        private final TextView companyName;
        private final TextView status;
        private final TextView dateApplied;

        public JobApplicationViewHolder(@NonNull View itemView) {
            super(itemView);
            jobTitle = itemView.findViewById(R.id.textViewJobTitle);
            companyName = itemView.findViewById(R.id.textViewCompanyName);
            status = itemView.findViewById(R.id.textViewStatus);
            dateApplied = itemView.findViewById(R.id.textViewDateApplied);
        }

        public void bind(JobApplication application, OnApplicationClickListener listener) {
            jobTitle.setText(application.getJobTitle());
            companyName.setText(application.getCompanyName());
            status.setText(application.getStatus().name());
            dateApplied.setText(application.getDateApplied());

            itemView.setOnClickListener(v -> listener.onApplicationClick(application));
        }
    }
}

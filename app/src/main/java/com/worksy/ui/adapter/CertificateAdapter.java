package com.worksy.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.worksy.R;
import com.worksy.data.model.Certificate;

import java.util.List;

public class CertificateAdapter extends RecyclerView.Adapter<CertificateAdapter.CertificateViewHolder> {

    private List<Certificate> certificateList;

    public CertificateAdapter(List<Certificate> certificateList) {
        this.certificateList = certificateList;
    }

    @NonNull
    @Override
    public CertificateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_certificate, parent, false);
        return new CertificateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CertificateViewHolder holder, int position) {
        Certificate certificate = certificateList.get(position);
        holder.textViewCertificateName.setText(certificate.getDisplayName());

        // Set icon based on file type (basic example)
        if (certificate.getFileName() != null) {
            if (certificate.getFileName().toLowerCase().endsWith(".pdf")) {
                holder.imageViewCertificateIcon.setImageResource(R.drawable.ic_file_pdf); // Assuming you have this drawable
            } else if (certificate.getFileName().toLowerCase().endsWith(".docx") || certificate.getFileName().toLowerCase().endsWith(".doc")) {
                holder.imageViewCertificateIcon.setImageResource(R.drawable.ic_file_doc); // Assuming you have this drawable
            } else if (certificate.getFileName().toLowerCase().endsWith(".jpg") || certificate.getFileName().toLowerCase().endsWith(".jpeg") || certificate.getFileName().toLowerCase().endsWith(".png")) {
                 holder.imageViewCertificateIcon.setImageResource(R.drawable.ic_file_image); // Assuming you have this drawable
            } else {
                 holder.imageViewCertificateIcon.setImageResource(R.drawable.ic_file_generic); // Assuming you have a generic file drawable
            }
        }

        // TODO: Add click listener for opening the certificate (e.g., using an Intent to view URL)
    }

    @Override
    public int getItemCount() {
        return certificateList.size();
    }

    public static class CertificateViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewCertificateIcon;
        TextView textViewCertificateName;

        public CertificateViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewCertificateIcon = itemView.findViewById(R.id.imageViewCertificateIcon);
            textViewCertificateName = itemView.findViewById(R.id.textViewCertificateName);
        }
    }
}

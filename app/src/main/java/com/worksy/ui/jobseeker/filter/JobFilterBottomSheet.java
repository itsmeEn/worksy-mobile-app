package com.worksy.ui.jobseeker.filter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.worksy.databinding.BottomSheetJobFilterBinding;
import java.util.ArrayList;
import java.util.List;

public class JobFilterBottomSheet extends BottomSheetDialogFragment {
    private BottomSheetJobFilterBinding binding;
    private OnFilterAppliedListener listener;

    public interface OnFilterAppliedListener {
        void onFiltersApplied(JobFilter filter);
    }

    public static JobFilterBottomSheet newInstance() {
        return new JobFilterBottomSheet();
    }

    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetJobFilterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.buttonApply.setOnClickListener(v -> applyFilters());
        binding.buttonReset.setOnClickListener(v -> resetFilters());
    }

    private void applyFilters() {
        if (listener != null) {
            JobFilter filter = new JobFilter();
            
            // Employment Type
            List<String> employmentTypes = new ArrayList<>();
            if (binding.chipFullTime.isChecked()) employmentTypes.add("FULL_TIME");
            if (binding.chipPartTime.isChecked()) employmentTypes.add("PART_TIME");
            if (binding.chipContract.isChecked()) employmentTypes.add("CONTRACT");
            if (binding.chipInternship.isChecked()) employmentTypes.add("INTERNSHIP");
            filter.setEmploymentTypes(employmentTypes);

            // Experience Level
            List<String> experienceLevels = new ArrayList<>();
            if (binding.chipEntry.isChecked()) experienceLevels.add("ENTRY");
            if (binding.chipMid.isChecked()) experienceLevels.add("MID");
            if (binding.chipSenior.isChecked()) experienceLevels.add("SENIOR");
            filter.setExperienceLevels(experienceLevels);

            // Salary Range
            filter.setMinSalary(binding.sliderSalary.getValues().get(0).floatValue());
            filter.setMaxSalary(binding.sliderSalary.getValues().get(1).floatValue());

            // Location
            filter.setLocation(binding.editTextLocation.getText().toString());

            listener.onFiltersApplied(filter);
        }
        dismiss();
    }

    private void resetFilters() {
        binding.chipFullTime.setChecked(false);
        binding.chipPartTime.setChecked(false);
        binding.chipContract.setChecked(false);
        binding.chipInternship.setChecked(false);
        
        binding.chipEntry.setChecked(false);
        binding.chipMid.setChecked(false);
        binding.chipSenior.setChecked(false);

        binding.sliderSalary.setValues(0f, 200000f);
        binding.editTextLocation.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

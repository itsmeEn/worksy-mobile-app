package com.worksy.ui.jobseeker.filter;

import java.util.List;

public class JobFilter {
    private List<String> employmentTypes;
    private List<String> experienceLevels;
    private float minSalary;
    private float maxSalary;
    private String location;

    public List<String> getEmploymentTypes() {
        return employmentTypes;
    }

    public void setEmploymentTypes(List<String> employmentTypes) {
        this.employmentTypes = employmentTypes;
    }

    public List<String> getExperienceLevels() {
        return experienceLevels;
    }

    public void setExperienceLevels(List<String> experienceLevels) {
        this.experienceLevels = experienceLevels;
    }

    public float getMinSalary() {
        return minSalary;
    }

    public void setMinSalary(float minSalary) {
        this.minSalary = minSalary;
    }

    public float getMaxSalary() {
        return maxSalary;
    }

    public void setMaxSalary(float maxSalary) {
        this.maxSalary = maxSalary;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean hasFilters() {
        return (employmentTypes != null && !employmentTypes.isEmpty()) ||
               (experienceLevels != null && !experienceLevels.isEmpty()) ||
               minSalary > 0 ||
               maxSalary < 200000 ||
               (location != null && !location.isEmpty());
    }
}

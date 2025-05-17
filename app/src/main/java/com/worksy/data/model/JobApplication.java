package com.worksy.data.model;

public class JobApplication {
    private String id;
    private String jobTitle;
    private String companyName;
    private ApplicationStatus status;
    private String dateApplied; // Or use java.util.Date

    public enum ApplicationStatus {
        ACCEPTED,
        REJECTED,
        SHORTLISTED,
        REVIEWING,
        UNKNOWN
    }

    public JobApplication(String id, String jobTitle, String companyName, ApplicationStatus status, String dateApplied) {
        this.id = id;
        this.jobTitle = jobTitle;
        this.companyName = companyName;
        this.status = status;
        this.dateApplied = dateApplied;
    }

    public String getId() {
        return id;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getCompanyName() {
        return companyName;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public String getDateApplied() {
        return dateApplied;
    }

    // Optional: Setters if you need to modify the object after creation
    public void setId(String id) {
        this.id = id;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public void setDateApplied(String dateApplied) {
        this.dateApplied = dateApplied;
    }
}
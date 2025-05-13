package com.worksy.data.model;

// Represents an applicant.
public class Applicant {
    private int id;
    private String name;
    private String applicationDate;
    private String status;
    private String resumeUrl;
    private String jobTitle;
    private String email;

    public Applicant(int id, String name, String applicationDate, String status, String resumeUrl, String jobTitle, String email) {
        this.id = id;
        this.name = name;
        this.applicationDate = applicationDate;
        this.status = status;
        this.resumeUrl = resumeUrl;
        this.jobTitle = jobTitle;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getApplicationDate() {
        return applicationDate;
    }

    public String getStatus() {
        return status;
    }

    public String getResumeUrl() {
        return resumeUrl;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public String getEmail() {
        return email;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
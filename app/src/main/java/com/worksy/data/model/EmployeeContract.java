package com.worksy.data.model;

import com.google.firebase.Timestamp;

public class EmployeeContract {
    public String id;
    public String employerId;
    public String employeeId;
    public String jobId;
    public String jobTitle;
    public String workArrangement; // e.g., "Full-time", "Part-time", "Contract", "Internship"
    public String workSetup;       // e.g., "Hybrid", "Remote", "On-site"
    public String status;          // e.g., "COMPLETED", "ACTIVE", "TERMINATED"
    public Timestamp startDate;
    public Timestamp endDate;

    public EmployeeContract() {}

    public EmployeeContract(String id, String employerId, String employeeId, String jobId, String jobTitle,
                           String workArrangement, String workSetup, String status,
                           Timestamp startDate, Timestamp endDate) {
        this.id = id;
        this.employerId = employerId;
        this.employeeId = employeeId;
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.workArrangement = workArrangement;
        this.workSetup = workSetup;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployerId() {
        return employerId;
    }

    public void setEmployerId(String employerId) {
        this.employerId = employerId;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getWorkArrangement() {
        return workArrangement;
    }

    public void setWorkArrangement(String workArrangement) {
        this.workArrangement = workArrangement;
    }

    public String getWorkSetup() {
        return workSetup;
    }

    public void setWorkSetup(String workSetup) {
        this.workSetup = workSetup;
    }

    public Timestamp getStartDate() {
        return startDate;
    }

    public void setStartDate(Timestamp startDate) {
        this.startDate = startDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getEndDate() {
        return endDate;
    }

    public void setEndDate(Timestamp endDate) {
        this.endDate = endDate;
    }
// Getters and setters for all fields...
}

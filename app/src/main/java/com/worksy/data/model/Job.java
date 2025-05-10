package com.worksy.data.model;

import com.google.firebase.Timestamp;
import java.util.List;
import java.util.Map;

public class Job {
    private String id;
    private String title;
    private String companyName;
    private String location;
    private String description;
    private String employmentType;
    private double salaryMin;
    private double salaryMax;
    private String salaryCurrency;
    private String experienceLevel;
    private List<String> requirements;
    private List<String> skills;
    private Map<String, Object> companyDetails;
    private Timestamp postedDate;
    private String status;
    private String employerId;
    private String jobCategory;
    private String workSetup; // Added workSetup
    private String workArrangement;

    // Required empty constructor for FireStore
    public Job() {}

    // Constructor with workSetup
    public Job(String id, String title, String companyName, String location, String description,
               String employmentType, double salaryMin, double salaryMax, String salaryCurrency,
               String experienceLevel, List<String> requirements, List<String> skills,
               Map<String, Object> companyDetails, Timestamp postedDate, String status,
               String employerId, String jobCategory, String workSetup, String workArrangement) {
        this.id = id;
        this.title = title;
        this.companyName = companyName;
        this.location = location;
        this.description = description;
        this.employmentType = employmentType;
        this.salaryMin = salaryMin;
        this.salaryMax = salaryMax;
        this.salaryCurrency = salaryCurrency;
        this.experienceLevel = experienceLevel;
        this.requirements = requirements;
        this.skills = skills;
        this.companyDetails = companyDetails;
        this.postedDate = postedDate;
        this.status = status;
        this.employerId = employerId;
        this.jobCategory = jobCategory;
        this.workSetup = workSetup; // Initialize workSetup
        this.workArrangement = workArrangement;
    }


    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public double getSalaryMin() { return salaryMin; }
    public void setSalaryMin(double salaryMin) { this.salaryMin = salaryMin; }

    public double getSalaryMax() { return salaryMax; }
    public void setSalaryMax(double salaryMax) { this.salaryMax = salaryMax; }

    public String getSalaryCurrency() { return salaryCurrency; }
    public void setSalaryCurrency(String salaryCurrency) { this.salaryCurrency = salaryCurrency; }

    public String getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }

    public List<String> getRequirements() { return requirements; }
    public void setRequirements(List<String> requirements) { this.requirements = requirements; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public Map<String, Object> getCompanyDetails() { return companyDetails; }
    public void setCompanyDetails(Map<String, Object> companyDetails) { this.companyDetails = companyDetails; }

    public Timestamp getPostedDate() { return postedDate; }
    public void setPostedDate(Timestamp postedDate) { this.postedDate = postedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getEmployerId() { return employerId; }
    public void setEmployerId(String employerId) { this.employerId = employerId; }

    public String getJobCategory() { return jobCategory; }
    public void setJobCategory(String jobCategory) { this.jobCategory = jobCategory; }

    public String getWorkSetup() { // Corrected getter for workSetup
        return workSetup;
    }

    public void setWorkSetup(String workSetup) {
        this.workSetup = workSetup;
    }

    public String getFormattedSalary() {
        if (salaryMin == 0 && salaryMax == 0) {
            return "Salary negotiable";
        }
        if (salaryMin == salaryMax) {
            return String.format("%s %.0f", salaryCurrency, salaryMin);
        }
        return String.format("%s %.0f - %.0f", salaryCurrency, salaryMin, salaryMax);
    }

    public void setWorkArrangement (String workArrangement){
        this.workArrangement = workArrangement;
    }

    public String getWorkArrangement() { // Add this getter method
        return workArrangement;
    }
}
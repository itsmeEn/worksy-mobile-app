package com.worksy.data.model;

import com.google.firebase.Timestamp;

public class EmployeeRating {
    private String id;
    private String employerId;
    private String employeeId;
    private float skillsKnowledge;
    private float qualityOfWork;
    private float communicationSkills;
    private float initiativeProactiveness;
    private float teamworkCollaboration;
    private Timestamp timestamp;
    private String jobId;

    // Required empty constructor for Firestore
    public EmployeeRating() {}

    public EmployeeRating(String id, String employerId, String employeeId, float skillsKnowledge,
                          float qualityOfWork, float communicationSkills, float initiativeProactiveness,
                          float teamworkCollaboration, String jobId) {
        this.id = id;
        this.employerId = employerId;
        this.employeeId = employeeId;
        this.skillsKnowledge = skillsKnowledge;
        this.qualityOfWork = qualityOfWork;
        this.communicationSkills = communicationSkills;
        this.initiativeProactiveness = initiativeProactiveness;
        this.teamworkCollaboration = teamworkCollaboration;
        this.jobId = jobId;
        this.timestamp = Timestamp.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmployerId() { return employerId; }
    public void setEmployerId(String employerId) { this.employerId = employerId; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public float getSkillsKnowledge() { return skillsKnowledge; }
    public void setSkillsKnowledge(float skillsKnowledge) { this.skillsKnowledge = skillsKnowledge; }

    public float getQualityOfWork() { return qualityOfWork; }
    public void setQualityOfWork(float qualityOfWork) { this.qualityOfWork = qualityOfWork; }

    public float getCommunicationSkills() { return communicationSkills; }
    public void setCommunicationSkills(float communicationSkills) { this.communicationSkills = communicationSkills; }

    public float getInitiativeProactiveness() { return initiativeProactiveness; }
    public void setInitiativeProactiveness(float initiativeProactiveness) { this.initiativeProactiveness = initiativeProactiveness; }

    public float getTeamworkCollaboration() { return teamworkCollaboration; }
    public void setTeamworkCollaboration(float teamworkCollaboration) { this.teamworkCollaboration = teamworkCollaboration; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getJobId() { return jobId; }
    public void setJobId(String jobId) { this.jobId = jobId; }

    public float getAverageRating() {
        return (skillsKnowledge + qualityOfWork + communicationSkills +
                initiativeProactiveness + teamworkCollaboration) / 5.0f;
    }
}
package com.example.taskmanager.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID; // YENİ: ID için gerekli

public class TaskNode implements Comparable<TaskNode> {

    // YENİ: Benzersiz kimlik
    private String id;
    private String title;
    private List<String> steps;
    private List<String> assignedEmployees;
    private LocalDate deadline;
    private String managerName;
    private String description;
    private boolean starred = false;
    private List<String> attachedFiles;
    private LocalDateTime createdAt;

    private List<String> completedEmployees = new ArrayList<>();
    private List<String> seenBy = new ArrayList<>();

    public TaskNode(String title, List<String> steps, List<String> assignedEmployees,
                    LocalDate deadline, String managerName, String description, List<String> attachedFiles) {
        // YENİ: Her yeni nesne oluşturulduğunda benzersiz bir ID ata
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.steps = steps;
        this.assignedEmployees = assignedEmployees;
        this.deadline = deadline;
        this.managerName = managerName;
        this.description = description;
        this.starred = false;
        this.attachedFiles = attachedFiles != null ? attachedFiles : new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.completedEmployees = new ArrayList<>();
        this.seenBy = new ArrayList<>();
    }

    public TaskNode(String title, List<String> steps, List<String> assignedEmployees,
                    LocalDate deadline, String managerName) {
        this(title, steps, assignedEmployees, deadline, managerName,
                "Bu görev için henüz bir açıklama eklenmemiştir.", new ArrayList<>());
    }

    @Override
    public int compareTo(TaskNode other) {
        if (this.deadline == null || other.deadline == null) return 0;
        return this.deadline.compareTo(other.deadline);
    }
    public String getId()                           { return id; }
    public void setId(String id)                    { this.id = id; }
    public String getTitle()                        { return title; }
    public List<String> getSteps()                  { return steps; }
    public List<String> getAssignedEmployees()      { return assignedEmployees; }
    public LocalDate getDeadline()                  { return deadline; }
    public String getManagerName()                  { return managerName; }
    public String getDescription()                  { return description; }
    public boolean isStarred()                      { return starred; }
    public List<String> getAttachedFiles()          { return attachedFiles; }
    public LocalDateTime getCreatedAt()             { return createdAt; }
    public List<String> getCompletedEmployees()     { return completedEmployees; }

    public List<String> getSeenBy() {
        if (seenBy == null) seenBy = new ArrayList<>();
        return seenBy;
    }

    public boolean isCompletedBy(String username) {
        return completedEmployees != null && completedEmployees.contains(username);
    }

    public boolean isSeenBy(String username) {
        return seenBy != null && seenBy.contains(username);
    }

    public void setTitle(String title)              { this.title = title; }
    public void setDescription(String description)  { this.description = description; }
    public void setStarred(boolean starred)         { this.starred = starred; }
    public void setSteps(List<String> steps)        { this.steps = steps; }
    public void setDeadline(LocalDate deadline)     { this.deadline = deadline; }
    public void setAttachedFiles(List<String> attachedFiles) { this.attachedFiles = attachedFiles; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setCompletedEmployees(List<String> completedEmployees) {
        this.completedEmployees = completedEmployees;
    }
    public void setSeenBy(List<String> seenBy) {
        this.seenBy = seenBy != null ? seenBy : new ArrayList<>();
    }
    public void setAssignedEmployees(List<String> assignedEmployees) {
        this.assignedEmployees = assignedEmployees;
    }
}
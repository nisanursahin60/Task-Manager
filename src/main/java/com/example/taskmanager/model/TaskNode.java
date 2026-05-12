package com.example.taskmanager.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TaskNode implements Comparable<TaskNode> {

    private String title;
    private List<String> steps;
    private List<String> assignedEmployees;
    private LocalDate deadline;
    private String managerName;
    private String description;
    private boolean starred = false;
    private List<String> attachedFiles;
    private LocalDateTime createdAt; // En son atanan en başa için

    // Bir görev birden fazla kişiye atanabilir.
    // O yüzden tamamlanma bilgisi de kişi kişi tutulur.
    private List<String> completedEmployees = new ArrayList<>();

    // Ana constructor (dosyalar + açıklama + createdAt)
    public TaskNode(String title, List<String> steps, List<String> assignedEmployees,
                    LocalDate deadline, String managerName, String description, List<String> attachedFiles) {
        this.title = title;
        this.steps = steps;
        this.assignedEmployees = assignedEmployees;
        this.deadline = deadline;
        this.managerName = managerName;
        this.description = description;
        this.starred = false;
        this.attachedFiles = attachedFiles != null ? attachedFiles : new ArrayList<>();
        this.createdAt = LocalDateTime.now(); // Atama anında zaman damgası
        this.completedEmployees = new ArrayList<>();
    }

    // Geriye dönük uyumluluk constructor'ı
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

    // Getters
    public String getTitle()                        { return title; }
    public List<String> getSteps()                  { return steps; }
    public List<String> getAssignedEmployees()      { return assignedEmployees; }
    public LocalDate getDeadline()                  { return deadline; }
    public String getManagerName()                  { return managerName; }
    public String getDescription()                  { return description; }
    public boolean isStarred()                      { return starred; }
    public List<String> getAttachedFiles()          { return attachedFiles; }
    public LocalDateTime getCreatedAt()             { return createdAt; }

    public List<String> getCompletedEmployees() {
        return completedEmployees;
    }

    // Bu çalışan bu görevi tamamladı mı?
    public boolean isCompletedBy(String username) {
        return completedEmployees != null && completedEmployees.contains(username);
    }

    // Setters
    public void setDescription(String description)  { this.description = description; }
    public void setStarred(boolean starred)         { this.starred = starred; }
    public void setAttachedFiles(List<String> attachedFiles) { this.attachedFiles = attachedFiles; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void setCompletedEmployees(List<String> completedEmployees) {
        this.completedEmployees = completedEmployees;
    }
}
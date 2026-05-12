package com.example.taskmanager.model;

import java.time.LocalDate;
import java.util.List;

public class TaskNode implements Comparable<TaskNode> {

    private String title;
    private List<String> steps;
    private List<String> assignedEmployees;
    private LocalDate deadline;
    private String managerName;
    private String description;
    private boolean starred = false; // YENİ: Yıldızlı mı?

    public TaskNode(String title, List<String> steps, List<String> assignedEmployees,
                    LocalDate deadline, String managerName, String description) {
        this.title = title;
        this.steps = steps;
        this.assignedEmployees = assignedEmployees;
        this.deadline = deadline;
        this.managerName = managerName;
        this.description = description;
        this.starred = false;
    }

    public TaskNode(String title, List<String> steps, List<String> assignedEmployees,
                    LocalDate deadline, String managerName) {
        this(title, steps, assignedEmployees, deadline, managerName,
                "Bu görev için henüz bir açıklama eklenmemiştir.");
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

    // Setters
    public void setDescription(String description)  { this.description = description; }
    public void setStarred(boolean starred)         { this.starred = starred; }
}
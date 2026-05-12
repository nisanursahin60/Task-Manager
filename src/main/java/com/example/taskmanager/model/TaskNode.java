package com.example.taskmanager.model;

import java.time.LocalDate;
import java.util.List;

/**
 * TaskNode: Priority Queue (Öncelikli Kuyruk) için veri düğümü.
 * Comparable arayüzü sayesinde teslim tarihine göre otomatik sıralanır.
 */
public class TaskNode implements Comparable<TaskNode> {

    private String title;
    private List<String> steps;
    private List<String> assignedEmployees;
    private LocalDate deadline;
    private String managerName;
    private String description; // YENİ EKLENEN ALAN

    // Tüm verileri alan yeni Constructor
    public TaskNode(String title, List<String> steps, List<String> assignedEmployees, LocalDate deadline, String managerName, String description) {
        this.title = title;
        this.steps = steps;
        this.assignedEmployees = assignedEmployees;
        this.deadline = deadline;
        this.managerName = managerName;
        this.description = description;
    }

    // Eski kodların (AddTaskController) bozulmaması için eski Constructor
    // Açıklama girilmezse varsayılan bir metin atar
    public TaskNode(String title, List<String> steps, List<String> assignedEmployees, LocalDate deadline, String managerName) {
        this(title, steps, assignedEmployees, deadline, managerName, "Bu görev için henüz bir açıklama eklenmemiştir.");
    }

    // Veri Yapıları Mantığı: Kuyrukta hangi düğümün önde olacağına karar verir.
    @Override
    public int compareTo(TaskNode other) {
        if (this.deadline == null || other.deadline == null) return 0;
        // Tarihi daha eski olan (daha yakın olan) 'küçüktür' ve kuyrukta başa geçer.
        return this.deadline.compareTo(other.deadline);
    }

    // Getters
    public String getTitle() { return title; }
    public List<String> getSteps() { return steps; }
    public List<String> getAssignedEmployees() { return assignedEmployees; }
    public LocalDate getDeadline() { return deadline; }
    public String getManagerName() { return managerName; }
    public String getDescription() { return description; }

    // Setters
    public void setDescription(String description) { this.description = description; }
}
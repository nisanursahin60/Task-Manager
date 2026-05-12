package com.example.taskmanager.model;

import java.time.LocalDate;
import java.util.ArrayList; // Dosya listesinin null gelme ihtimaline karşı eklendi
import java.util.List;

public class TaskNode implements Comparable<TaskNode> {

    private String title;
    private List<String> steps;
    private List<String> assignedEmployees;
    private LocalDate deadline;
    private String managerName;
    private String description;
    private boolean starred = false; // Yıldızlı mı?
    private List<String> attachedFiles; // YENİ: Ekli dosyaların isimleri

    // Tüm verileri (Dosyalar dahil) alan ana Constructor
    public TaskNode(String title, List<String> steps, List<String> assignedEmployees,
                    LocalDate deadline, String managerName, String description, List<String> attachedFiles) {
        this.title = title;
        this.steps = steps;
        this.assignedEmployees = assignedEmployees;
        this.deadline = deadline;
        this.managerName = managerName;
        this.description = description;
        this.starred = false;
        // Eğer dosya listesi boş gönderilirse (null), hata vermemesi için boş bir liste oluşturuyoruz
        this.attachedFiles = attachedFiles != null ? attachedFiles : new ArrayList<>();
    }

    // Eski kodların bozulmaması için eski Constructor (Açıklama ve Dosya girilmediğinde çalışır)
    public TaskNode(String title, List<String> steps, List<String> assignedEmployees,
                    LocalDate deadline, String managerName) {
        // Otomatik olarak boş bir açıklama ve boş bir dosya listesi (new ArrayList<>()) atar
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
    public List<String> getAttachedFiles()          { return attachedFiles; } // YENİ

    // Setters
    public void setDescription(String description)  { this.description = description; }
    public void setStarred(boolean starred)         { this.starred = starred; }
    public void setAttachedFiles(List<String> attachedFiles) { this.attachedFiles = attachedFiles; } // YENİ
}
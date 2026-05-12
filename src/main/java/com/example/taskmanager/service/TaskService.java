package com.example.taskmanager.service;

import com.example.taskmanager.model.TaskNode;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class TaskService {

    private static final String FILE_PATH = "src/main/resources/com/example/taskmanager/tasks.json";

    private static List<TaskNode> allTasks = new ArrayList<>();
    private static PriorityQueue<TaskNode> priorityQueue = new PriorityQueue<>();

    private static final Gson gson;

    static {
        gson = new GsonBuilder()
                .setPrettyPrinting()
                // LocalDate adapter (Arkadaşının eklediği)
                .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
                    @Override
                    public void write(JsonWriter out, LocalDate value) throws IOException {
                        if (value == null) out.nullValue();
                        else out.value(value.toString());
                    }
                    @Override
                    public LocalDate read(JsonReader in) throws IOException {
                        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                            in.nextNull();
                            return null;
                        }
                        return LocalDate.parse(in.nextString());
                    }
                })
                // LocalDateTime adapter (Arkadaşının eklediği)
                .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                    @Override
                    public void write(JsonWriter out, LocalDateTime value) throws IOException {
                        if (value == null) out.nullValue();
                        else out.value(value.toString());
                    }
                    @Override
                    public LocalDateTime read(JsonReader in) throws IOException {
                        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                            in.nextNull();
                            return null;
                        }
                        return LocalDateTime.parse(in.nextString());
                    }
                })
                .create();

        loadTasksFromJson();
    }

    private static void loadTasksFromJson() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists() || file.length() == 0) {
                allTasks = new ArrayList<>();
                return;
            }

            Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            Type taskListType = new TypeToken<ArrayList<TaskNode>>(){}.getType();
            allTasks = gson.fromJson(reader, taskListType);
            reader.close();

            if (allTasks != null) {
                priorityQueue.clear(); // Kuyruğu temizleyip yeniden dolduruyoruz

                // Arkadaşının eklediği zaman damgası tamamlama mantığı
                for (int i = 0; i < allTasks.size(); i++) {
                    TaskNode task = allTasks.get(i);
                    if (task.getCreatedAt() == null) {
                        task.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, i));
                    }
                }

                priorityQueue.addAll(allTasks);
                System.out.println("✅ " + allTasks.size() + " adet görev JSON'dan yüklendi.");
            } else {
                allTasks = new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Görevler yüklenirken hata: " + e.getMessage());
        }
    }

    public static void addTask(TaskNode node) {
        // Yeni görev eklenirken zaman damgası yoksa o anki zamanı ata
        if (node.getCreatedAt() == null) {
            node.setCreatedAt(LocalDateTime.now());
        }
        allTasks.add(node);
        priorityQueue.offer(node);
        saveToJson();
    }

    public static void toggleStar(TaskNode node) {
        node.setStarred(!node.isStarred());
        saveToJson();
    }

    private static void saveToJson() {
        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(FILE_PATH), StandardCharsets.UTF_8)) {
            gson.toJson(allTasks, writer);
        } catch (IOException e) {
            System.err.println("JSON Kayıt Hatası: " + e.getMessage());
        }
    }

    /**
     * Belirli bir çalışana atanmış görevleri döner (Deadline sıralı).
     */
    public static PriorityQueue<TaskNode> getTasksForEmployee(String username) {
        PriorityQueue<TaskNode> employeeTasks = new PriorityQueue<>();
        for (TaskNode task : allTasks) {
            if (task.getAssignedEmployees() != null
                    && task.getAssignedEmployees().contains(username)
                    && !task.isCompleted()) {
                employeeTasks.offer(task);
            }
        }
        return employeeTasks;
    }

    /**
     * Belirli bir çalışana atanmış görevleri döner (Oluşturulma tarihine göre DESC).
     */
    public static List<TaskNode> getTasksForEmployeeByCreatedAt(String username) {
        List<TaskNode> result = new ArrayList<>();
        for (TaskNode task : allTasks) {
            if (task.getAssignedEmployees() != null
                    && task.getAssignedEmployees().contains(username)
                    && !task.isCompleted()) {
                result.add(task);
            }
        }
        // En son eklenen en başa (Arkadaşının eklediği sıralama mantığı)
        result.sort((a, b) -> {
            if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });
        return result;
    }

    /**
     * Çalışanın yıldızlı görevlerini döner.
     */
    public static PriorityQueue<TaskNode> getStarredTasksForEmployee(String username) {
        PriorityQueue<TaskNode> starred = new PriorityQueue<>();
        for (TaskNode task : allTasks) {
            if (task.getAssignedEmployees() != null
                    && task.getAssignedEmployees().contains(username)
                    && task.isStarred()) {
                starred.offer(task);
            }
        }
        return starred;
    }

    /**
     * Senin sistemin için: Yönetici sayfasında tüm görevleri deadline'a göre sıralı döner.
     */
    public static PriorityQueue<TaskNode> getPriorityQueue() {
        return priorityQueue;
    }

    /**
     * Senin sistemin için: Tüm görevleri liste olarak döner.
     */
    public static List<TaskNode> getAllTasks() {
        return new ArrayList<>(allTasks);
    }

    public static void completeTask(TaskNode node) {
        node.setCompleted(true);
        saveToJson();
    }

    public static List<TaskNode> getCompletedTasksForEmployee(String username) {
        List<TaskNode> result = new ArrayList<>();

        for (TaskNode task : allTasks) {
            if (task.getAssignedEmployees() != null
                    && task.getAssignedEmployees().contains(username)
                    && task.isCompleted()) {
                result.add(task);
            }
        }

        result.sort((a, b) -> {
            if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
            if (a.getCreatedAt() == null) return 1;
            if (b.getCreatedAt() == null) return -1;
            return b.getCreatedAt().compareTo(a.getCreatedAt());
        });

        return result;
    }
}
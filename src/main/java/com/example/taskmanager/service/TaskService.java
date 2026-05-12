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

                // LocalDate adapter
                .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
                    @Override
                    public void write(JsonWriter out, LocalDate value) throws IOException {
                        if (value == null) {
                            out.nullValue();
                        } else {
                            out.value(value.toString());
                        }
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

                // LocalDateTime adapter
                .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                    @Override
                    public void write(JsonWriter out, LocalDateTime value) throws IOException {
                        if (value == null) {
                            out.nullValue();
                        } else {
                            out.value(value.toString());
                        }
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
            Type taskListType = new TypeToken<ArrayList<TaskNode>>() {}.getType();

            allTasks = gson.fromJson(reader, taskListType);
            reader.close();

            if (allTasks != null) {
                priorityQueue.clear();

                for (int i = 0; i < allTasks.size(); i++) {
                    TaskNode task = allTasks.get(i);

                    // Eski JSON görevlerinde createdAt yoksa varsayılan tarih veriyoruz
                    if (task.getCreatedAt() == null) {
                        task.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, i));
                    }

                    // Eski JSON görevlerinde completedEmployees yoksa boş liste veriyoruz
                    if (task.getCompletedEmployees() == null) {
                        task.setCompletedEmployees(new ArrayList<>());
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
        if (node.getCreatedAt() == null) {
            node.setCreatedAt(LocalDateTime.now());
        }

        if (node.getCompletedEmployees() == null) {
            node.setCompletedEmployees(new ArrayList<>());
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
     * Belirli bir çalışana atanmış ve o çalışan tarafından tamamlanmamış görevleri döner.
     * Deadline sıralıdır.
     */
    public static PriorityQueue<TaskNode> getTasksForEmployee(String username) {
        PriorityQueue<TaskNode> employeeTasks = new PriorityQueue<>();

        for (TaskNode task : allTasks) {
            if (task.getAssignedEmployees() != null
                    && task.getAssignedEmployees().contains(username)
                    && !task.isCompletedBy(username)) {
                employeeTasks.offer(task);
            }
        }

        return employeeTasks;
    }

    /**
     * Belirli bir çalışana atanmış ve o çalışan tarafından tamamlanmamış görevleri döner.
     * Oluşturulma tarihine göre en yeni en üsttedir.
     */
    public static List<TaskNode> getTasksForEmployeeByCreatedAt(String username) {
        List<TaskNode> result = new ArrayList<>();

        for (TaskNode task : allTasks) {
            if (task.getAssignedEmployees() != null
                    && task.getAssignedEmployees().contains(username)
                    && !task.isCompletedBy(username)) {
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

    /**
     * Çalışanın yıldızlı ve tamamlanmamış görevlerini döner.
     */
    public static PriorityQueue<TaskNode> getStarredTasksForEmployee(String username) {
        PriorityQueue<TaskNode> starred = new PriorityQueue<>();

        for (TaskNode task : allTasks) {
            if (task.getAssignedEmployees() != null
                    && task.getAssignedEmployees().contains(username)
                    && task.isStarred()
                    && !task.isCompletedBy(username)) {
                starred.offer(task);
            }
        }

        return starred;
    }

    /**
     * Yönetici sayfası için tüm görevleri deadline'a göre sıralı döner.
     */
    public static PriorityQueue<TaskNode> getPriorityQueue() {
        return priorityQueue;
    }

    /**
     * Tüm görevleri liste olarak döner.
     */
    public static List<TaskNode> getAllTasks() {
        return new ArrayList<>(allTasks);
    }

    /**
     * Görevi sadece belirtilen çalışan için tamamlar.
     * Örneğin görev Mehmet ve Elif'e atanmışsa, sadece Elif tamamladıysa
     * completedEmployees listesine sadece "elif" eklenir.
     */
    public static void completeTask(TaskNode node, String username) {
        if (node.getCompletedEmployees() == null) {
            node.setCompletedEmployees(new ArrayList<>());
        }

        if (!node.getCompletedEmployees().contains(username)) {
            node.getCompletedEmployees().add(username);
        }

        saveToJson();
    }

    /**
     * Belirli çalışanın tamamladığı görevleri döner.
     */
    public static List<TaskNode> getCompletedTasksForEmployee(String username) {
        List<TaskNode> result = new ArrayList<>();

        for (TaskNode task : allTasks) {
            if (task.getAssignedEmployees() != null
                    && task.getAssignedEmployees().contains(username)
                    && task.isCompletedBy(username)) {
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
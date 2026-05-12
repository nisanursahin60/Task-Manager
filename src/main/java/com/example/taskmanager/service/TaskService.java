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
                .create();

        loadTasksFromJson();
    }

    private static void loadTasksFromJson() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists() || file.length() == 0) return;

            Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
            Type taskListType = new TypeToken<ArrayList<TaskNode>>(){}.getType();
            allTasks = gson.fromJson(reader, taskListType);
            reader.close();

            if (allTasks != null) {
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
        allTasks.add(node);
        priorityQueue.offer(node);
        saveToJson();
    }

    /**
     * Yıldız durumunu değiştirip JSON'a kaydeder.
     */
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

    public static PriorityQueue<TaskNode> getTasksForEmployee(String username) {
        PriorityQueue<TaskNode> employeeTasks = new PriorityQueue<>();
        for (TaskNode task : allTasks) {
            if (task.getAssignedEmployees() != null
                    && task.getAssignedEmployees().contains(username)) {
                employeeTasks.offer(task);
            }
        }
        return employeeTasks;
    }

    /**
     * VERİ YAPISI: HashSet ile yıldızlı görevler hızlıca filtrelenir.
     * HashSet → O(1) contains, yıldızlı title'ları saklar.
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

    public static PriorityQueue<TaskNode> getPriorityQueue() {
        return priorityQueue;
    }
}
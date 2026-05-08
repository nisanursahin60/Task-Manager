package com.example.taskmanager.service;

import com.example.taskmanager.model.TaskNode;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class TaskService {
    // Heap tabanlı Priority Queue veri yapısı
    private static PriorityQueue<TaskNode> priorityQueue = new PriorityQueue<>();
    private static final String FILE_PATH = "src/main/resources/com/example/taskmanager/tasks.json";

    /**
     * Yeni bir görev düğümünü kuyruğa ekler ve JSON'a kaydeder.
     */
    public static void addTask(TaskNode node) {
        // 1. Bellekteki veri yapısına ekle
        priorityQueue.add(node);

        // 2. Kalıcılık için dosyaya yaz
        saveToJson(node);
    }

    private static void saveToJson(TaskNode node) {
        try {
            File file = new File(FILE_PATH);
            StringBuilder content = new StringBuilder();

            if (file.exists() && file.length() > 5) {
                BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8));
                String line;
                StringBuilder existing = new StringBuilder();
                while ((line = reader.readLine()) != null) existing.append(line);
                reader.close();

                content.append(existing.toString().trim());
                content.deleteCharAt(content.length() - 1); // ']' karakterini kaldır
                content.append(",\n");
            } else {
                content.append("[\n");
            }

            // TaskNode verilerini manuel JSON formatına dönüştürme
            content.append("  {\n");
            content.append("    \"title\": \"").append(node.getTitle()).append("\",\n");
            content.append("    \"deadline\": \"").append(node.getDeadline()).append("\",\n");
            content.append("    \"manager\": \"").append(node.getManagerName()).append("\",\n");
            content.append("    \"employees\": [").append(node.getAssignedEmployees().stream().map(e -> "\"" + e + "\"").collect(Collectors.joining(","))).append("],\n");
            content.append("    \"steps\": [").append(node.getSteps().stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(","))).append("]\n");
            content.append("  }\n]");

            BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8));
            writer.write(content.toString());
            writer.close();
        } catch (IOException e) {
            System.err.println("JSON Kayıt Hatası: " + e.getMessage());
        }
    }

    public static PriorityQueue<TaskNode> getPriorityQueue() {
        return priorityQueue;
    }
}
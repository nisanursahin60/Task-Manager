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
    private static boolean hasNewMessages = false;

    public static boolean isHasNewMessages() { return hasNewMessages; }
    public static void setHasNewMessages(boolean value) { hasNewMessages = value; }

    private static final String FILE_PATH = "src/main/resources/com/example/taskmanager/tasks.json";
    private static final String MESSAGES_FILE_PATH = "src/main/resources/com/example/taskmanager/messages.json";

    private static List<TaskNode> allTasks = new ArrayList<>();
    private static PriorityQueue<TaskNode> priorityQueue = new PriorityQueue<>();
    private static List<TaskMessage> messages = new ArrayList<>();

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
                        if (in.peek() == com.google.gson.stream.JsonToken.NULL) { in.nextNull(); return null; }
                        return LocalDate.parse(in.nextString());
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new TypeAdapter<LocalDateTime>() {
                    @Override
                    public void write(JsonWriter out, LocalDateTime value) throws IOException {
                        if (value == null) out.nullValue();
                        else out.value(value.toString());
                    }
                    @Override
                    public LocalDateTime read(JsonReader in) throws IOException {
                        if (in.peek() == com.google.gson.stream.JsonToken.NULL) { in.nextNull(); return null; }
                        return LocalDateTime.parse(in.nextString());
                    }
                })
                .create();

        loadTasksFromJson();
        loadMessagesFromJson();
    }

    // -------------------------------------------------------------------------
    // JSON YÜKLEME / KAYDETME
    // -------------------------------------------------------------------------

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
                    if (task.getCreatedAt() == null) {
                        task.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, i));
                    }
                    if (task.getCompletedEmployees() == null) {
                        task.setCompletedEmployees(new ArrayList<>());
                    }
                    if (task.getSeenBy() == null) {
                        task.setSeenBy(new ArrayList<>());
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

    private static void saveToJson() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(FILE_PATH), StandardCharsets.UTF_8)) {
            gson.toJson(allTasks, writer);
        } catch (IOException e) {
            System.err.println("JSON Kayıt Hatası: " + e.getMessage());
        }
    }

    private static void loadMessagesFromJson() {
        try {
            File file = new File(MESSAGES_FILE_PATH);
            if (!file.exists() || file.length() == 0) { messages = new ArrayList<>(); return; }
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                Type listType = new TypeToken<ArrayList<TaskMessage>>() {}.getType();
                List<TaskMessage> loaded = gson.fromJson(reader, listType);
                if (loaded != null) {
                    messages = loaded;
                    hasNewMessages = messages.stream().anyMatch(m -> !m.isRead);
                }
            }
        } catch (Exception e) {
            System.err.println("Mesajlar yüklenirken hata: " + e.getMessage());
            messages = new ArrayList<>();
        }
    }

    private static void saveMessagesToJson() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(MESSAGES_FILE_PATH), StandardCharsets.UTF_8)) {
            gson.toJson(messages, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // GÖREV İŞLEMLERİ
    // -------------------------------------------------------------------------

    public static void addTask(TaskNode node) {
        if (node.getCreatedAt() == null) node.setCreatedAt(LocalDateTime.now());
        if (node.getCompletedEmployees() == null) node.setCompletedEmployees(new ArrayList<>());
        if (node.getSeenBy() == null) node.setSeenBy(new ArrayList<>());
        allTasks.add(node);
        priorityQueue.offer(node);
        saveToJson();
    }

    public static void toggleStar(TaskNode node) {
        node.setStarred(!node.isStarred());
        saveToJson();
    }

    public static void completeTask(TaskNode node, String username) {
        if (node.getCompletedEmployees() == null) node.setCompletedEmployees(new ArrayList<>());
        if (!node.getCompletedEmployees().contains(username)) {
            node.getCompletedEmployees().add(username);
        }
        saveToJson();
    }

    // -------------------------------------------------------------------------
    // YENİ GÖREV / GÖRÜLDÜ İŞLEMLERİ
    // -------------------------------------------------------------------------

    /**
     * Son 3 gün içinde atanmış ve belirtilen çalışanın henüz görmediği görevleri döner.
     * Bu metot sadece SAYIM için kullanılır (kırmızı nokta kararı).
     */
    public static boolean hasUnseenNewTasksForEmployee(String username) {
        LocalDateTime sinir = LocalDateTime.now().minusDays(3);
        for (TaskNode task : allTasks) {
            if (task.getAssignedEmployees() != null
                    && task.getAssignedEmployees().contains(username)
                    && !task.isCompletedBy(username)
                    && task.getCreatedAt() != null
                    && task.getCreatedAt().isAfter(sinir)
                    && !task.isSeenBy(username)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Son 3 gün içinde atanmış, tamamlanmamış görevleri döner.
     * En yeni en üstte olacak şekilde sıralar.
     */
    public static List<TaskNode> getNewTasksForEmployee(String username) {
        LocalDateTime sinir = LocalDateTime.now().minusDays(3);
        List<TaskNode> result = new ArrayList<>();

        for (TaskNode task : allTasks) {
            if (task.getAssignedEmployees() != null
                    && task.getAssignedEmployees().contains(username)
                    && !task.isCompletedBy(username)
                    && task.getCreatedAt() != null
                    && task.getCreatedAt().isAfter(sinir)) {
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
     * Belirtilen çalışanı, verilen görev listesindeki tüm görevler için "gördü" olarak işaretler.
     */
    public static void markTasksAsSeenByEmployee(List<TaskNode> tasks, String username) {
        boolean changed = false;
        for (TaskNode task : tasks) {
            if (!task.isSeenBy(username)) {
                task.getSeenBy().add(username);
                changed = true;
            }
        }
        if (changed) saveToJson();
    }

    // -------------------------------------------------------------------------
    // ÇALIŞAN GÖREV SORGULARI
    // -------------------------------------------------------------------------

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

    public static PriorityQueue<TaskNode> getPriorityQueue() { return priorityQueue; }
    public static List<TaskNode> getAllTasks() { return new ArrayList<>(allTasks); }

    // -------------------------------------------------------------------------
    // MESAJ İŞLEMLERİ
    // -------------------------------------------------------------------------

    public static class TaskMessage {
        public String sender;
        public String taskTitle;
        public String content;
        public boolean isRead = false;
        public LocalDateTime timestamp;

        public TaskMessage(String sender, String taskTitle, String content) {
            this.sender = sender;
            this.taskTitle = taskTitle;
            this.content = content;
            this.timestamp = LocalDateTime.now();
            this.isRead = false;
        }
    }

    public static void addMessage(TaskMessage msg) {
        messages.add(0, msg);
        hasNewMessages = true;
        saveMessagesToJson();
    }

    public static List<TaskMessage> getMessages() { return messages; }

    public static void removeMessage(TaskMessage msg) {
        messages.remove(msg);
        saveMessagesToJson();
    }

    public static void markAllMessagesAsRead() {
        for (TaskMessage msg : messages) msg.isRead = true;
        hasNewMessages = false;
        saveMessagesToJson();
    }
}
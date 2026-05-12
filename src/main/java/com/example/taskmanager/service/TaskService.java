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
    private static boolean hasNewMessages = false; // Yeni mesaj var mı kontrolü

    public static boolean isHasNewMessages() {
        return hasNewMessages;
    }

    public static void setHasNewMessages(boolean value) {
        hasNewMessages = value;
    }

    private static final String FILE_PATH = "src/main/resources/com/example/taskmanager/tasks.json";

    private static List<TaskNode> allTasks = new ArrayList<>();
    private static PriorityQueue<TaskNode> priorityQueue = new PriorityQueue<>();

    private static final Gson gson;

    private static final String MESSAGES_FILE_PATH = "src/main/resources/com/example/taskmanager/messages.json";
    private static List<TaskMessage> messages = new ArrayList<>();

    // static bloğu içinde veya sınıf yüklendiğinde mesajları dosyadan oku

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
        loadMessagesFromJson();
    }

    private static void saveMessagesToJson() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(MESSAGES_FILE_PATH), StandardCharsets.UTF_8)) {
            gson.toJson(messages, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- JSON YÜKLEME ---
    private static void loadMessagesFromJson() {
        try {
            File file = new File(MESSAGES_FILE_PATH);
            if (!file.exists() || file.length() == 0) {
                messages = new ArrayList<>();
                return;
            }

            // Reader'ı try-with-resources ile açıyoruz (otomatik kapanır)
            try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                Type listType = new TypeToken<ArrayList<TaskMessage>>() {}.getType();
                List<TaskMessage> loadedMessages = gson.fromJson(reader, listType);

                if (loadedMessages != null) {
                    messages = loadedMessages;
                    // Sadece okunmamış mesaj varsa noktayı yak
                    hasNewMessages = messages.stream().anyMatch(m -> !m.isRead);
                }
            }
        } catch (Exception e) {
            // Hata durumunda uygulamanın çökmesini engellemek için sadece hata basıyoruz
            System.err.println("Mesajlar yüklenirken hata oluştu: " + e.getMessage());
            messages = new ArrayList<>(); // Liste boş kalsın, null olmasın
        }
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
    // Mesajlar için basit bir iç sınıf veya ayrı dosya
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

    public static void markAllMessagesAsRead() {
        for (TaskMessage msg : messages) {
            msg.isRead = true;
        }
        hasNewMessages = false;
        saveMessagesToJson(); // JSON dosyasına isRead = true bilgisini yazar
    }

    public static void addMessage(TaskMessage msg) {
        messages.add(0, msg);
        hasNewMessages = true;
        saveMessagesToJson();
    }

    public static List<TaskMessage> getMessages() {
        return messages;
    }

    public static void removeMessage(TaskMessage msg) {
        messages.remove(msg);
        saveMessagesToJson();
    }
}
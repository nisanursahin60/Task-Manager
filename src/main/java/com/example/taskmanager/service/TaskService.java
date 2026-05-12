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

    // Verileri tutacağımız temel listeler
    private static List<TaskNode> allTasks = new ArrayList<>();
    // Heap tabanlı Priority Queue veri yapısı (Tüm görevler için)
    private static PriorityQueue<TaskNode> priorityQueue = new PriorityQueue<>();

    // Gson nesnesini statik olarak bir kere yapılandırıyoruz
    private static final Gson gson;

    static {
        // Gson Ayarları ve LocalDate Çözümü
        gson = new GsonBuilder()
                .setPrettyPrinting() // Verileri alt alta, okunabilir formatta yazar
                .registerTypeAdapter(LocalDate.class, new TypeAdapter<LocalDate>() {
                    @Override
                    public void write(JsonWriter out, LocalDate value) throws IOException {
                        if (value == null) out.nullValue();
                        else out.value(value.toString()); // Tarihi metne çevir (Örn: "2026-05-25")
                    }

                    @Override
                    public LocalDate read(JsonReader in) throws IOException {
                        if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                            in.nextNull();
                            return null;
                        }
                        return LocalDate.parse(in.nextString()); // Metni tekrar LocalDate nesnesine çevir
                    }
                })
                .create();

        // Sınıf yüklendiğinde eski verileri oku
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
                priorityQueue.addAll(allTasks); // Okunanları öncelikli kuyruğa doldur
                System.out.println("✅ " + allTasks.size() + " adet görev JSON'dan başarıyla yüklendi.");
            } else {
                allTasks = new ArrayList<>();
            }
        } catch (Exception e) {
            System.err.println("Görevler yüklenirken hata oluştu: " + e.getMessage());
        }
    }

    /**
     * Yeni bir görev düğümünü listelere ekler ve JSON'a kaydeder.
     */
    public static void addTask(TaskNode node) {

        allTasks.add(node);
        priorityQueue.offer(node);
        saveToJson();
    }

    private static void saveToJson() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(FILE_PATH), StandardCharsets.UTF_8)) {
            // allTasks listesindeki tüm nesneleri otomatik JSON'a dönüştür ve dosyaya yaz
            gson.toJson(allTasks, writer);
        } catch (IOException e) {
            System.err.println("JSON Kayıt Hatası: " + e.getMessage());
        }
    }

    /**
     * SADECE belirli bir çalışana atanmış görevleri, teslim tarihine göre
     * sıralı bir şekilde Priority Queue olarak döndürür (Arayüzde kullanacağız).
     */
    public static PriorityQueue<TaskNode> getTasksForEmployee(String username) {
        PriorityQueue<TaskNode> employeeTasks = new PriorityQueue<>();
        for (TaskNode task : allTasks) {
            if (task.getAssignedEmployees() != null && task.getAssignedEmployees().contains(username)) {
                employeeTasks.offer(task);
            }
        }
        return employeeTasks;
    }

    public static PriorityQueue<TaskNode> getPriorityQueue() {
        return priorityQueue;
    }
}
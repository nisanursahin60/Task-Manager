package com.example.taskmanager.controller;

import com.example.taskmanager.model.TaskNode;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.awt.Desktop;
import java.io.File;

public class TaskDetailPageController {

    @FXML private Label managerNameLabel;
    @FXML private Label managerAvatarLabel;
    @FXML private Label taskTitleLabel;
    @FXML private Label taskDescriptionLabel;
    @FXML private TextArea questionArea;
    @FXML private VBox filesContainer;

    private TaskNode currentTask;

    public void setTaskDetails(TaskNode task) {
        this.currentTask = task;

        managerNameLabel.setText(task.getManagerName());
        managerAvatarLabel.setText(basHarfleriAl(task.getManagerName()));
        taskTitleLabel.setText(task.getTitle().toUpperCase());

        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            taskDescriptionLabel.setText(task.getDescription());
        } else {
            taskDescriptionLabel.setText("Bu görev için yönetici tarafından herhangi bir detaylı açıklama eklenmemiştir.");
        }

        if (filesContainer.getChildren().size() > 1) {
            filesContainer.getChildren().subList(1, filesContainer.getChildren().size()).clear();
        }

        if (task.getAttachedFiles() != null && !task.getAttachedFiles().isEmpty()) {
            // Dosyalar varsa her biri için bir kutucuk oluşturur
            for (String filePath : task.getAttachedFiles()) {

                // YENİ: Dosya yolundan bir File nesnesi oluşturuyoruz ki hem adını alabilelim hem de açabilelim
                File file = new File(filePath);

                HBox dosyaKutusu = new HBox(10);
                dosyaKutusu.setAlignment(Pos.CENTER_LEFT);
                // YENİ: Kutuya tıklandığını belli etmek için fareyi el (hand) ikonuna çeviriyoruz
                dosyaKutusu.setStyle("-fx-background-color: white; -fx-padding: 12 18; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4); -fx-cursor: hand;");

                Label ikon = new Label("📄");
                ikon.setStyle("-fx-font-size: 22px;");

                // YENİ: Uzun dizin yolunu değil, sadece dosyanın adını (örn: rapor.pdf) göster
                Label isimLabel = new Label(file.getName());
                isimLabel.setStyle("-fx-text-fill: #1d4ed8; -fx-font-weight: bold;");

                // YENİ: Tüm beyaz kutucuğa tıklama özelliği kazandırıyoruz
                dosyaKutusu.setOnMouseClicked(e -> dosyayiAc(file));

                dosyaKutusu.getChildren().addAll(ikon, isimLabel);
                filesContainer.getChildren().add(dosyaKutusu);
            }
        } else {
            // Dosya yoksa bilgi ver
            Label yokLabel = new Label("Bu göreve eklenmiş herhangi bir dosya bulunmuyor.");
            yokLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
            filesContainer.getChildren().add(yokLabel);
        }
    }

    // YENİ: Tıklanan dosyayı bilgisayarın varsayılan uygulamasıyla (PDF okuyucu, Word vb.) açan sihirli metot
    private void dosyayiAc(File file) {
        try {
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Hata");
                alert.setHeaderText("Dosya Bulunamadı");
                alert.setContentText("Bu dosya bilgisayardan silinmiş veya yeri değiştirilmiş olabilir.\nAranan Yol: " + file.getAbsolutePath());
                alert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("Dosya açılamadı: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSendQuestion() {
        try {
            String question = questionArea.getText().trim();
            if (question.isEmpty()) {
                return;
            }

            UserService userService = UserService.getInstance();

            String gonderenAdi = userService.getCurrentUser()
                    .map(user -> user.getFullName())
                    .orElse("Bilinmeyen Kullanıcı");

            if (currentTask == null) {
                System.err.println("HATA: currentTask nesnesi null!");
                return;
            }

            TaskService.TaskMessage msg = new TaskService.TaskMessage(
                    gonderenAdi,
                    currentTask.getTitle(),
                    question
            );
            TaskService.addMessage(msg);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Başarılı");
            alert.setHeaderText(null);
            alert.setContentText("Sorunuz yöneticiye iletildi.");
            alert.showAndWait();

            questionArea.clear();

        } catch (Exception e) {
            System.err.println("Mesaj gönderme sırasında hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String basHarfleriAl(String adSoyad) {
        if(adSoyad == null || adSoyad.trim().isEmpty()) return "?";
        String[] parcalar = adSoyad.trim().split("\\s+");
        if (parcalar.length == 1) return parcalar[0].substring(0, 1).toUpperCase();
        return (parcalar[0].substring(0, 1) + parcalar[parcalar.length - 1].substring(0, 1)).toUpperCase();
    }
}
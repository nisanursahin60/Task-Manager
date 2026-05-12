package com.example.taskmanager.controller;

import com.example.taskmanager.model.TaskNode;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class TaskDetailPageController {

    @FXML private Label managerNameLabel;
    @FXML private Label managerAvatarLabel;
    @FXML private Label taskTitleLabel;
    @FXML private Label taskDescriptionLabel;
    @FXML private TextArea questionArea;
    @FXML private VBox filesContainer; // FXML'den bağladığımız alan

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

        // --- DİNAMİK DOSYA YÜKLEME KISMI ---
        // FXML'de "Ekli Dosyalar" başlığı var, onun altını temizliyoruz ki üst üste binmesin
        if (filesContainer.getChildren().size() > 1) {
            filesContainer.getChildren().subList(1, filesContainer.getChildren().size()).clear();
        }

        if (task.getAttachedFiles() != null && !task.getAttachedFiles().isEmpty()) {
            // Dosyalar varsa her biri için bir kutucuk oluştur
            for (String fileName : task.getAttachedFiles()) {
                HBox dosyaKutusu = new HBox(10);
                dosyaKutusu.setAlignment(Pos.CENTER_LEFT);
                dosyaKutusu.setStyle("-fx-background-color: white; -fx-padding: 12 18; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 4);");

                Label ikon = new Label("📄");
                ikon.setStyle("-fx-font-size: 22px;");

                Label isimLabel = new Label(fileName);
                isimLabel.setStyle("-fx-text-fill: #1d4ed8; -fx-cursor: hand; -fx-font-weight: bold;");

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

    @FXML
    private void handleSendQuestion() {
        String question = questionArea.getText().trim();
        if (question.isEmpty()) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mesaj Gönderildi");
        alert.setHeaderText(null);
        alert.setContentText("Sorunuz yönetici " + currentTask.getManagerName() + "'e başarıyla iletildi.");
        alert.showAndWait();

        questionArea.clear();
    }

    private String basHarfleriAl(String adSoyad) {
        if(adSoyad == null || adSoyad.trim().isEmpty()) return "?";
        String[] parcalar = adSoyad.trim().split("\\s+");
        if (parcalar.length == 1) return parcalar[0].substring(0, 1).toUpperCase();
        return (parcalar[0].substring(0, 1) + parcalar[parcalar.length - 1].substring(0, 1)).toUpperCase();
    }
}
package com.example.taskmanager.controller;

import com.example.taskmanager.model.TaskNode;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class TaskDetailPageController {

    @FXML private Label managerNameLabel;
    @FXML private Label managerAvatarLabel;
    @FXML private Label taskTitleLabel;
    @FXML private Label taskDescriptionLabel;
    @FXML private TextArea questionArea;

    private TaskNode currentTask;

    // EmployeePageController'dan Modal açılırken bu metod çağrılacak
    public void setTaskDetails(TaskNode task) {
        this.currentTask = task;

        managerNameLabel.setText(task.getManagerName());
        managerAvatarLabel.setText(basHarfleriAl(task.getManagerName()));
        taskTitleLabel.setText(task.getTitle().toUpperCase());

        // Eğer JSON'da açıklama yoksa boş dönmemesi için kontrol
        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            taskDescriptionLabel.setText(task.getDescription());
        } else {
            taskDescriptionLabel.setText("Bu görev için yönetici tarafından herhangi bir detaylı açıklama eklenmemiştir.");
        }
    }

    @FXML
    private void handleSendQuestion() {
        String question = questionArea.getText().trim();
        if (question.isEmpty()) {
            return; // Boşsa işlem yapma
        }

        // İleride buraya soruyu kaydedecek Service kodunu ekleyeceğiz.
        // Şimdilik başarılı mesajı gösterip alanı temizliyoruz.
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
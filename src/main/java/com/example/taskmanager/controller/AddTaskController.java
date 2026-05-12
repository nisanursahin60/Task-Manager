package com.example.taskmanager.controller;

import com.example.taskmanager.model.TaskNode;
import com.example.taskmanager.model.User;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.time.LocalDate;
import java.util.*;

public class AddTaskController {

    @FXML private TextField titleField;
    @FXML private TextField stepInputField;
    @FXML private ListView<String> stepsListView;
    @FXML private DatePicker deadlinePicker;
    @FXML private TreeView<String> employeeTreeView;
    @FXML private TextField personelArama;

    private final ObservableList<String> stepsList = FXCollections.observableArrayList();
    private final UserService userService = UserService.getInstance();

    @FXML
    public void initialize() {
        stepsListView.setItems(stepsList);
        setupEmployeeTree();

        personelArama.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEmployeeTree(newValue);
        });

        // Satırları özelleştiriyoruz
        stepsListView.setCellFactory(param -> new ListCell<String>() {
            private final HBox container = new HBox();
            private final Label stepText = new Label();
            private final Label deleteBtn = new Label("-"); // Eksi butonu
            private final Region spacer = new Region();

            {
                // Stil ve yerleşim ayarları
                container.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(spacer, Priority.ALWAYS); // Metin ile buton arasını açar
                deleteBtn.getStyleClass().add("delete-step-btn");
                stepText.getStyleClass().add("step-item-text");

                container.getChildren().addAll(stepText, spacer, deleteBtn);

                // Silme fonksiyonu
                deleteBtn.setOnMouseClicked(event -> {
                    String item = getItem();
                    if (item != null) {
                        // Mevcut metinden gerçek içeriği al (numarayı atla)
                        // "1. Adım İçeriği" -> "Adım İçeriği"
                        stepsList.remove(item);
                        reorderSteps(); // Numaraları düzelt
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    stepText.setText(item);
                    setGraphic(container);
                }
            }
        });
    }

    private void setupEmployeeTree() {
        CheckBoxTreeItem<String> rootItem = new CheckBoxTreeItem<>("Şirket Kadrosu");
        rootItem.setExpanded(true);

        Map<String, List<User>> departmentMap = new HashMap<>();
        for (User user : userService.getAllEmployees()) {
            departmentMap.computeIfAbsent(user.getDepartment(), k -> new ArrayList<>()).add(user);
        }

        for (Map.Entry<String, List<User>> entry : departmentMap.entrySet()) {
            CheckBoxTreeItem<String> deptItem = new CheckBoxTreeItem<>(entry.getKey());

            for (User employee : entry.getValue()) {
                // Güvenli yöntem: Ad Soyad (@kullanıcıadı)
                CheckBoxTreeItem<String> empItem = new CheckBoxTreeItem<>(
                        employee.getFullName() + " (@" + employee.getUsername() + ")"
                );
                deptItem.getChildren().add(empItem);
            }
            rootItem.getChildren().add(deptItem);
        }

        employeeTreeView.setRoot(rootItem);
        employeeTreeView.setCellFactory(CheckBoxTreeCell.forTreeView());
    }

    private void filterEmployeeTree(String filter) {
        if (filter == null || filter.isEmpty()) {
            setupEmployeeTree(); // Arama boşsa tüm listeyi tekrar göster
            return;
        }

        CheckBoxTreeItem<String> filteredRoot = new CheckBoxTreeItem<>("Arama Sonuçları");
        filteredRoot.setExpanded(true);

        for (User user : userService.getAllEmployees()) {
            if (user.getFullName().toLowerCase().contains(filter.toLowerCase())) {
                CheckBoxTreeItem<String> item = new CheckBoxTreeItem<>(user.getFullName() + " (@" + user.getUsername() + ")");
                filteredRoot.getChildren().add(item);
            }
        }
        employeeTreeView.setRoot(filteredRoot);
    }

    // Yeni adımları listeye ekleyen metodu güncelle
    @FXML
    private void handleAddStep() {
        String stepText = stepInputField.getText().trim();
        if (!stepText.isEmpty()) {
            // Listeye eklerken numara koymuyoruz, reorderSteps halledecek
            stepsList.add(stepText);
            reorderSteps();
            stepInputField.clear();
        }
    }

    // Tüm adımları baştan numaralandıran sihirli metod
    private void reorderSteps() {
        List<String> currentTexts = new ArrayList<>();

        // Mevcut metinlerin başındaki "1. ", "2. " kısımlarını temizleyip saf hallerini al
        for (String s : stepsList) {
            String cleanText = s.replaceAll("^\\d+\\.\\s*", "");
            currentTexts.add(cleanText);
        }

        stepsList.clear();

        // Yeniden numaralandırarak listeye geri ekle
        for (int i = 0; i < currentTexts.size(); i++) {
            stepsList.add((i + 1) + ". " + currentTexts.get(i));
        }
    }

    @FXML
    private void handleSave() {
        if (titleField.getText().isEmpty() || deadlinePicker.getValue() == null) {
            showAlert("Hata", "Lütfen görev başlığını ve teslim tarihini giriniz!");
            return;
        }

        List<String> selectedUsernames = new ArrayList<>();
        findSelectedUsernames(employeeTreeView.getRoot(), selectedUsernames);

        if (selectedUsernames.isEmpty()) {
            showAlert("Uyarı", "Lütfen görevin atanacağı çalışanları seçin.");
            return;
        }

        TaskNode newNode = new TaskNode(
                titleField.getText(),
                new ArrayList<>(stepsList),
                selectedUsernames,
                deadlinePicker.getValue(),
                userService.getCurrentUser().map(User::getFullName).orElse("Yönetici")
        );

        TaskService.addTask(newNode);
        showAlert("Başarılı", "Görev başarıyla eklendi.");
        clearForm();
    }

    private void findSelectedUsernames(TreeItem<String> item, List<String> list) {
        if (item instanceof CheckBoxTreeItem) {
            CheckBoxTreeItem<String> cbItem = (CheckBoxTreeItem<String>) item;
            if (cbItem.isSelected() && cbItem.isLeaf()) {
                String value = cbItem.getValue();
                if (value.contains("(@")) {
                    String username = value.substring(value.indexOf("@") + 1, value.indexOf(")"));
                    list.add(username);
                }
            }
        }
        for (TreeItem<String> child : item.getChildren()) {
            findSelectedUsernames(child, list);
        }
    }

    private void clearForm() {
        titleField.clear();
        stepsList.clear();
        stepInputField.clear();
        deadlinePicker.setValue(null);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
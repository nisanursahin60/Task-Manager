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
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.awt.Desktop;
import java.io.File;
import java.util.*;

public class AddTaskController {

    @FXML private TextField titleField;
    @FXML private TextField stepInputField;
    @FXML private ListView<String> stepsListView;
    @FXML private DatePicker deadlinePicker;
    @FXML private TreeView<String> employeeTreeView;
    @FXML private TextField personelArama;
    @FXML private TextArea descriptionField;
    @FXML private VBox secilenDosyalarKutusu;
    private final List<String> secilenDosyaYollari = new ArrayList<>();

    private final ObservableList<String> stepsList = FXCollections.observableArrayList();
    private final UserService userService = UserService.getInstance();

    @FXML
    public void initialize() {
        stepsListView.setItems(stepsList);
        setupEmployeeTree();

        personelArama.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEmployeeTree(newValue);
        });

        stepsListView.setCellFactory(param -> new ListCell<String>() {
            private final HBox container = new HBox();
            private final Label stepText = new Label();
            private final Label deleteBtn = new Label("-");
            private final Region spacer = new Region();

            {
                container.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(spacer, Priority.ALWAYS);
                deleteBtn.getStyleClass().add("delete-step-btn");
                stepText.getStyleClass().add("step-item-text");

                container.getChildren().addAll(stepText, spacer, deleteBtn);

                deleteBtn.setOnMouseClicked(event -> {
                    String item = getItem();
                    if (item != null) {
                        stepsList.remove(item);
                        reorderSteps();
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
            setupEmployeeTree();
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

    @FXML
    private void handleAddStep() {
        String stepText = stepInputField.getText().trim();
        if (!stepText.isEmpty()) {
            stepsList.add(stepText);
            reorderSteps();
            stepInputField.clear();
        }
    }

    private void reorderSteps() {
        List<String> currentTexts = new ArrayList<>();

        for (String s : stepsList) {
            String cleanText = s.replaceAll("^\\d+\\.\\s*", "");
            currentTexts.add(cleanText);
        }

        stepsList.clear();

        for (int i = 0; i < currentTexts.size(); i++) {
            stepsList.add((i + 1) + ". " + currentTexts.get(i));
        }
    }

    @FXML
    private void dosyaSec() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Göreve Dosya Ekle");

        List<File> files = fileChooser.showOpenMultipleDialog(null);

        if (files != null && !files.isEmpty()) {

            if (secilenDosyaYollari.isEmpty()) {
                secilenDosyalarKutusu.getChildren().clear();
            }

            for (File file : files) {
                if (!secilenDosyaYollari.contains(file.getAbsolutePath())) {
                    secilenDosyaYollari.add(file.getAbsolutePath());

                    Label fileLabel = new Label("📎 " + file.getName());
                    fileLabel.setStyle("-fx-text-fill: white; -fx-font-style: italic; -fx-font-size: 11px; -fx-cursor: hand;");

                    fileLabel.setOnMouseEntered(e -> fileLabel.setStyle("-fx-text-fill: white; -fx-font-style: italic; -fx-font-size: 11px; -fx-cursor: hand; -fx-underline: true;"));
                    fileLabel.setOnMouseExited(e -> fileLabel.setStyle("-fx-text-fill: white; -fx-font-style: italic; -fx-font-size: 11px; -fx-cursor: hand; -fx-underline: false;"));

                    fileLabel.setOnMouseClicked(e -> dosyayiAc(file));

                    secilenDosyalarKutusu.getChildren().add(fileLabel);
                }
            }
        }
    }

    private void dosyayiAc(File file) {
        try {
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            } else {
                showAlert("Hata", "Dosya bulunamadı! Yol: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            System.err.println("Dosya açılamadı: " + e.getMessage());
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
                userService.getCurrentUser().map(User::getFullName).orElse("Yönetici"),
                descriptionField.getText().trim(),
                new ArrayList<>(secilenDosyaYollari)
        );

        TaskService.addTask(newNode);
        showAlert("Başarılı", "Görev başarıyla eklendi.");
        clearForm();
    }

    private void clearForm() {
        titleField.clear();
        descriptionField.clear();
        stepsList.clear();
        stepInputField.clear();
        deadlinePicker.setValue(null);
        secilenDosyaYollari.clear();
        secilenDosyalarKutusu.getChildren().clear();
        Label emptyLabel = new Label("Henüz dosya seçilmedi");
        emptyLabel.setStyle("-fx-text-fill: white; -fx-font-style: italic; -fx-font-size: 11px; -fx-opacity: 0.6;");
        secilenDosyalarKutusu.getChildren().add(emptyLabel);
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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void initSelectedUser(String username) {
        if (username == null || username.isEmpty() || employeeTreeView.getRoot() == null) {
            return;
        }
        selectUserInTree(employeeTreeView.getRoot(), username);
    }

    private void selectUserInTree(TreeItem<String> item, String username) {
        if (item instanceof CheckBoxTreeItem) {
            CheckBoxTreeItem<String> cbItem = (CheckBoxTreeItem<String>) item;
            String value = cbItem.getValue();

            if (value != null && value.contains("(@" + username + ")")) {
                cbItem.setSelected(true);

                TreeItem<String> parent = cbItem.getParent();
                while (parent != null) {
                    parent.setExpanded(true);
                    parent = parent.getParent();
                }
                return;
            }
        }

        for (TreeItem<String> child : item.getChildren()) {
            selectUserInTree(child, username);
        }
    }
}
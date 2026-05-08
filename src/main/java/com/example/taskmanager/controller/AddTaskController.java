package com.example.taskmanager.controller;

import com.example.taskmanager.model.TaskNode;
import com.example.taskmanager.model.User;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import java.time.LocalDate;
import java.util.*;

public class AddTaskController {

    @FXML private TextField titleField;
    @FXML private TextField stepInputField;
    @FXML private ListView<String> stepsListView;
    @FXML private DatePicker deadlinePicker;
    @FXML private TreeView<String> employeeTreeView;

    private final ObservableList<String> stepsList = FXCollections.observableArrayList();
    private final UserService userService = UserService.getInstance();

    @FXML
    public void initialize() {
        stepsListView.setItems(stepsList);
        setupEmployeeTree();
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

    @FXML
    private void handleAddStep() {
        String newStep = stepInputField.getText().trim();
        if (!newStep.isEmpty()) {
            int stepNumber = stepsList.size() + 1;
            stepsList.add(stepNumber + ". " + newStep);
            stepInputField.clear();
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
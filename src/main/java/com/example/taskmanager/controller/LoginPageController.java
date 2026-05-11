package com.example.taskmanager.controller;

import com.example.taskmanager.model.User;
import com.example.taskmanager.service.UserService;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;

public class LoginPageController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final UserService userService = UserService.getInstance();

    @FXML
    public void initialize() {
        // Hata mesajını başlangıçta gizle
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }

    /**
     * Kullanıcı adı alanında Enter'a basıldığında şifre alanına odaklanır.
     */
    @FXML
    private void onUsernameEnter() {
        passwordField.requestFocus();
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Boş alan kontrolü
        if (username.isEmpty() || password.isEmpty()) {
            showError("Lütfen tüm alanları doldurun!");
            shakeField(username.isEmpty() ? usernameField : passwordField);
            return;
        }

        // Kullanıcı doğrulama
        Optional<User> result = userService.login(username, password);

        if (result.isEmpty()) {
            showError("Kullanıcı adı veya şifre hatalı!");
            shakeField(usernameField);
            shakeField(passwordField);
            passwordField.clear();
            return;
        }

        User user = result.get();
        navigateToPage(user);
    }

    private void navigateToPage(User user) {
        try {
            String fxmlFile = user.isManager() ? "ManagerPage.fxml" : "EmployeePage.fxml";
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/taskmanager/" + fxmlFile)
            );

            Scene scene = new Scene(loader.load());

            if (user.isManager()) {
                ManagerPageController controller = loader.getController();
                controller.initWithUser(user);
            } else {
                EmployeePageController controller = loader.getController();
                controller.initWithUser(user);
            }

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setMaximized(false);
            stage.setScene(scene);
            stage.setMaximized(true);

            FadeTransition ft = new FadeTransition(Duration.millis(400), scene.getRoot());
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
            ft.play();

        } catch (Exception e) {
            System.err.println("Sayfa yükleme hatası: " + e.getMessage());
            e.printStackTrace();
            showError("Sayfa yüklenirken hata oluştu!");
        }
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);

            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(3));
            pause.setOnFinished(e -> errorLabel.setVisible(false));
            pause.play();
        } else {
            System.out.println("⚠️ " + message);
        }
    }

    private void shakeField(javafx.scene.Node node) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(60), node);
        tt.setByX(8);
        tt.setCycleCount(6);
        tt.setAutoReverse(true);
        tt.setOnFinished(e -> node.setTranslateX(0));
        tt.play();
    }
}
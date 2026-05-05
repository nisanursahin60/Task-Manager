package com.example.taskmanager.controller;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginPageController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Lütfen tüm alanları doldurun!");
        } else {
            // Burada kullanıcı doğrulama yapıp diğer ekrana geçiş yapabilirsin
            System.out.println("Giriş yapılıyor: " + username);
        }
    }
}
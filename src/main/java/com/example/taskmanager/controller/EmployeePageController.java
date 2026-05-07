package com.example.taskmanager.controller;

import com.example.taskmanager.model.User;
import com.example.taskmanager.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.stage.Stage;

public class EmployeePageController {

    @FXML private TilePane gorevKartAlani;
    @FXML private Label panelBaslik;
    @FXML private AnchorPane anchorPane;   // cikisYap için

    // Profil label'ları - FXML'e eklenecek
    @FXML private Label profilAdLabel;
    @FXML private Label profilRolLabel;
    @FXML private Label avatarLabel;

    private final UserService userService = UserService.getInstance();
    private User currentUser;

    @FXML
    public void initialize() {
        // initWithUser çağrılana kadar bekle
    }

    /**
     * Login controller'dan çağrılır - kullanıcı bilgisiyle initialize eder.
     */
    public void initWithUser(User user) {
        this.currentUser = user;

        // Profil alanlarını doldur (varsa)
        if (profilAdLabel != null) {
            profilAdLabel.setText(user.getFullName());
        }
        if (profilRolLabel != null) {
            profilRolLabel.setText(user.getDepartment());
        }
        if (avatarLabel != null) {
            avatarLabel.setText(basHarfleriAl(user.getFullName()));
        }

        tumGorevleriGoster();
        System.out.println("✅ Employee sayfası açıldı: " + user.getFullName());
    }

    @FXML
    private void tumGorevleriGoster() {
        if (panelBaslik != null) {
            panelBaslik.setText("Tüm Görevlerim");
        }
    }

    @FXML
    private void onemliGorevler() {
        if (panelBaslik != null) {
            panelBaslik.setText("Önemli Görevler");
        }
    }

    @FXML
    private void sonTarihYaklasanlar() {
        if (panelBaslik != null) {
            panelBaslik.setText("Son Tarihi Yaklaşanlar");
        }
    }

    @FXML
    private void yeniGorevler() {
        if (panelBaslik != null) {
            panelBaslik.setText("Yeni Eklenenler");
        }
    }

    @FXML
    private void cikisYap() {
        userService.logout();
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/taskmanager/LoginPage.fxml")
            );
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) anchorPane.getScene().getWindow();

            stage.setMaximized(false);
            stage.setScene(scene);
            stage.setMaximized(true);

        } catch (Exception e) {
            System.err.println("Çıkış hatası: " + e.getMessage());
        }
    }

    private String basHarfleriAl(String adSoyad) {
        String[] parcalar = adSoyad.trim().split("\\s+");
        if (parcalar.length == 0) return "?";
        if (parcalar.length == 1) return parcalar[0].substring(0, 1).toUpperCase();
        return (parcalar[0].substring(0, 1) + parcalar[parcalar.length - 1].substring(0, 1)).toUpperCase();
    }
}
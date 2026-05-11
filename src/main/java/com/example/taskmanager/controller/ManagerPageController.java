package com.example.taskmanager.controller;

import com.example.taskmanager.model.User;
import com.example.taskmanager.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.LinkedList;

public class ManagerPageController {

    @FXML private AnchorPane anchorPane;
    @FXML private TilePane calisanKartAlani;
    @FXML private Label departmanad;
    @FXML private Label profilLabel;

    private final UserService userService = UserService.getInstance();
    private User currentUser;

    @FXML
    private void initialize() {
    }

    public void initWithUser(User user) {
        this.currentUser = user;
        if (user != null) {
            profilLabel.setText(user.getFullName());
        }
        tumCalisanlariGoster();
    }

    @FXML
    private void tumCalisanlariGoster() {
        departmanad.setText("Tüm Çalışanlar");
        calisanKartAlani.getChildren().clear();
        LinkedList<User> employees = userService.getAllEmployees();

        for (User emp : employees) {
            // Ana ekranda departman görünsün
            calisanKartEkle(emp.getFullName(), emp.getDepartment());
        }
    }

    private void departmanYukle(String dAd) {
        departmanad.setText(dAd);
        calisanKartAlani.getChildren().clear();
        LinkedList<User> employees = userService.getEmployeesByDepartment(dAd);

        for (User emp : employees) {
            // Title bilgisini al
            String unvan = emp.getTitle();

            // EĞER BURASI BOŞ GELİYORSA: Model veya Service kısmında hata var demektir.
            // Test etmek için boşsa "Unvan Girilmemiş" yazdırıyoruz.
            if (unvan == null || unvan.trim().isEmpty()) {
                unvan = "Unvan Bilgisi Yok";
            }

            calisanKartEkle(emp.getFullName(), unvan);
        }
    }

    @FXML private void muhasebeGoster() { departmanYukle("Muhasebe"); }
    @FXML private void yazilimGoster() { departmanYukle("Yazılım"); }
    @FXML private void ikGoster() { departmanYukle("İnsan Kaynakları"); }
    @FXML private void pazarlamaGoster() { departmanYukle("Pazarlama"); }
    @FXML private void destekGoster() { departmanYukle("Destek"); }

    private void calisanKartEkle(String adSoyad, String altBilgi) {
        VBox kart = new VBox(10);
        kart.setAlignment(Pos.CENTER);
        kart.getStyleClass().add("employee-card");
        kart.setPrefSize(150, 180); // Kart boyutunu sabitleyelim

        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("avatar-circle");
        Label avatarHarf = new Label(basHarfleriAl(adSoyad));
        avatarHarf.getStyleClass().add("avatar-letter");
        avatar.getChildren().add(avatarHarf);

        Label isimLabel = new Label(adSoyad);
        isimLabel.getStyleClass().add("employee-name");

        Label bilgiLabel = new Label(altBilgi);
        bilgiLabel.getStyleClass().add("employee-info");

        kart.getChildren().addAll(avatar, isimLabel, bilgiLabel);
        calisanKartAlani.getChildren().add(kart);
    }

    private String basHarfleriAl(String adSoyad) {
        if (adSoyad == null || adSoyad.isEmpty()) return "?";
        String[] parcalar = adSoyad.trim().split("\\s+");
        if (parcalar.length == 1) return parcalar[0].substring(0, 1).toUpperCase();
        return (parcalar[0].substring(0, 1) + parcalar[parcalar.length - 1].substring(0, 1)).toUpperCase();
    }

    @FXML
    private void cikisYap() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/taskmanager/LoginPage.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void atananGorevleriGoster() { departmanad.setText("Atanan Görevler"); calisanKartAlani.getChildren().clear(); }
    @FXML private void gorevEklePaneliAc() { /* AddTaskPage kodun... */ }
}
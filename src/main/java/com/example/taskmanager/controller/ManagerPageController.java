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
    @FXML private Label calisanOk;
    @FXML private VBox departmanListesi;
    @FXML private Label departmanad;
    @FXML private VBox mainPanel;
    @FXML private Label profilLabel;
    @FXML private VBox sidePanel;

    private final UserService userService = UserService.getInstance();
    private User currentUser;

    @FXML
    private void initialize() {
        // initWithUser çağrılana kadar bekle
    }

    /**
     * Login controller'dan çağrılır - kullanıcı bilgisiyle initialize eder.
     */
    public void initWithUser(User user) {
        this.currentUser = user;
        profilLabel.setText(user.getFullName());
        tumCalisanlariGoster();
    }

    @FXML
    private void tumCalisanlariGoster() {
        departmanad.setText("Tüm Çalışanlar");
        calisanKartAlani.getChildren().clear();

        // UserService'den LinkedList olarak çek
        LinkedList<User> employees = userService.getAllEmployees();

        if (employees.isEmpty()) {
            Label bosLabel = new Label("Henüz çalışan bulunmuyor.");
            bosLabel.getStyleClass().add("page-subtitle");
            calisanKartAlani.getChildren().add(bosLabel);
            return;
        }

        for (User emp : employees) {
            calisanKartEkle(emp.getFullName(), emp.getDepartment());
        }
    }

    @FXML
    private void muhasebeGoster() {
        departmanad.setText("Muhasebe");
        calisanKartAlani.getChildren().clear();

        LinkedList<User> employees = userService.getEmployeesByDepartment("Muhasebe");
        for (User emp : employees) {
            calisanKartEkle(emp.getFullName(), "Muhasebe Uzmanı");
        }
    }

    @FXML
    private void yazilimGoster() {
        departmanad.setText("Yazılım");
        calisanKartAlani.getChildren().clear();

        LinkedList<User> employees = userService.getEmployeesByDepartment("Yazılım");
        for (User emp : employees) {
            calisanKartEkle(emp.getFullName(), "Yazılım Geliştirici");
        }
    }

    @FXML
    private void ikGoster() {
        departmanad.setText("İnsan Kaynakları");
        calisanKartAlani.getChildren().clear();

        LinkedList<User> employees = userService.getEmployeesByDepartment("İnsan Kaynakları");
        for (User emp : employees) {
            calisanKartEkle(emp.getFullName(), "İK Uzmanı");
        }
    }

    @FXML
    private void atananGorevleriGoster() {
        departmanad.setText("Atanan Görevler");
        calisanKartAlani.getChildren().clear();

        Label bosLabel = new Label("Atanan görevler burada listelenecek.");
        bosLabel.getStyleClass().add("page-subtitle");
        calisanKartAlani.getChildren().add(bosLabel);
    }

    @FXML
    private void gorevEklePaneliAc() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/taskmanager/AddTaskPage.fxml"));
            Stage stage = new Stage();
            stage.setTitle("Yeni Görev Atama");
            stage.setScene(new Scene(loader.load()));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
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

    private void calisanKartEkle(String adSoyad, String bilgi) {
        VBox kart = new VBox(10);
        kart.setAlignment(Pos.CENTER);
        kart.getStyleClass().add("employee-card");

        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("avatar-circle");

        Label avatarHarf = new Label(basHarfleriAl(adSoyad));
        avatarHarf.getStyleClass().add("avatar-letter");
        avatar.getChildren().add(avatarHarf);

        Label isimLabel = new Label(adSoyad);
        isimLabel.getStyleClass().add("employee-name");

        Label bilgiLabel = new Label(bilgi);
        bilgiLabel.getStyleClass().add("employee-info");

        kart.getChildren().addAll(avatar, isimLabel, bilgiLabel);
        calisanKartAlani.getChildren().add(kart);
    }

    private String basHarfleriAl(String adSoyad) {
        String[] parcalar = adSoyad.trim().split("\\s+");
        if (parcalar.length == 0) return "?";
        if (parcalar.length == 1) return parcalar[0].substring(0, 1).toUpperCase();
        return (parcalar[0].substring(0, 1) + parcalar[parcalar.length - 1].substring(0, 1)).toUpperCase();
    }
}
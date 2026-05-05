package com.example.taskmanager.controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class ManagerPageController {

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private TilePane calisanKartAlani;

    @FXML
    private Label calisanOk;

    @FXML
    private VBox departmanListesi;

    @FXML
    private Label departmanad;

    @FXML
    private VBox mainPanel;

    @FXML
    private Label profilLabel;

    @FXML
    private VBox sidePanel;

    @FXML
    private void initialize() {
        profilLabel.setText("Yönetici Adı");
        tumCalisanlariGoster();
    }

    @FXML
    private void tumCalisanlariGoster() {
        departmanad.setText("Tüm Çalışanlar");
        calisanKartAlani.getChildren().clear();

        calisanKartEkle("Ayşe Yılmaz", "Muhasebe");
        calisanKartEkle("Mehmet Kaya", "Yazılım");
        calisanKartEkle("Zeynep Demir", "İnsan Kaynakları");
    }

    @FXML
    private void muhasebeGoster() {
        departmanad.setText("Muhasebe");
        calisanKartAlani.getChildren().clear();

        calisanKartEkle("Ayşe Yılmaz", "Müdür");
        calisanKartEkle("Ali Koç", "Muhasebe Uzmanı");
    }

    @FXML
    private void yazilimGoster() {
        departmanad.setText("Yazılım");
        calisanKartAlani.getChildren().clear();

        calisanKartEkle("Mehmet Kaya", "Yazılım Geliştirici");
        calisanKartEkle("Elif Arslan", "Frontend Developer");
    }

    @FXML
    private void ikGoster() {
        departmanad.setText("İnsan Kaynakları");
        calisanKartAlani.getChildren().clear();

        calisanKartEkle("Zeynep Demir", "İK Müdürü");
        calisanKartEkle("Burak Şahin", "İK Uzmanı");
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
        departmanad.setText("Görev Ekle");
        calisanKartAlani.getChildren().clear();

        Label bosLabel = new Label("Görev ekleme alanı burada açılacak.");
        bosLabel.getStyleClass().add("page-subtitle");
        calisanKartAlani.getChildren().add(bosLabel);
    }

    @FXML
    private void cikisYap() {
        System.out.println("Çıkış yapıldı");
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

        if (parcalar.length == 0) {
            return "?";
        }

        if (parcalar.length == 1) {
            return parcalar[0].substring(0, 1).toUpperCase();
        }

        return (parcalar[0].substring(0, 1)
                + parcalar[parcalar.length - 1].substring(0, 1)).toUpperCase();
    }
}
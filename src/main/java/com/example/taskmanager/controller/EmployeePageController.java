package com.example.taskmanager.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.TilePane;
import javafx.scene.control.Label;

public class EmployeePageController {

    @FXML private TilePane gorevKartAlani;
    @FXML private Label panelBaslik;

    @FXML
    public void initialize() {
        // Sayfa yüklendiğinde varsayılan olarak tüm görevleri getir
    }

    @FXML
    private void tumGorevleriGoster() {
        panelBaslik.setText("Tüm Görevler");
        // Görevleri listeleme mantığı buraya
    }

    @FXML
    private void onemliGorevler() {
        panelBaslik.setText("Önemli Görevler");
    }

    @FXML
    private void sonTarihYaklasanlar() {
        panelBaslik.setText("Son Tarihi Yaklaşanlar");
    }

    @FXML
    private void yeniGorevler() {
        panelBaslik.setText("Yeni Eklenenler");
    }

    @FXML
    private void cikisYap() {
        System.out.println("Giriş ekranına yönlendiriliyor...");
    }
}
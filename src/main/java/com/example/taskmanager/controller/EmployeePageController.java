package com.example.taskmanager.controller;

import com.example.taskmanager.model.TaskNode;
import com.example.taskmanager.model.User;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class EmployeePageController {

    @FXML private HBox gorevKartAlani;
    @FXML private Label panelBaslik;
    @FXML private AnchorPane anchorPane;

    @FXML private Label profilAdLabel;
    @FXML private Label profilRolLabel;
    @FXML private Label avatarLabel;

    private final UserService userService = UserService.getInstance();
    private User currentUser;

    @FXML
    public void initialize() {
    }

    public void initWithUser(User user) {
        this.currentUser = user;

        if (profilAdLabel != null) profilAdLabel.setText(user.getFullName());
        if (profilRolLabel != null) profilRolLabel.setText(user.getTitle() != null ? user.getTitle() : user.getDepartment());
        if (avatarLabel != null) avatarLabel.setText(basHarfleriAl(user.getFullName()));

        tumGorevleriGoster();
    }

    @FXML
    private void tumGorevleriGoster() {
        if (panelBaslik != null) panelBaslik.setText("Tüm Görevlerim");

        gorevKartAlani.getChildren().clear();

        PriorityQueue<TaskNode> pq = TaskService.getTasksForEmployee(currentUser.getUsername());

        if (pq.isEmpty()) {
            Label bosLabel = new Label("Şu an atanmış bir göreviniz bulunmuyor.");
            bosLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7b8a9b;");
            gorevKartAlani.getChildren().add(bosLabel);
            return;
        }


        VBox sutun1 = new VBox(20);
        VBox sutun2 = new VBox(20);
        VBox sutun3 = new VBox(20);

        HBox.setHgrow(sutun1, Priority.ALWAYS);
        HBox.setHgrow(sutun2, Priority.ALWAYS);
        HBox.setHgrow(sutun3, Priority.ALWAYS);

        int sutunSirasi = 0; // Görevleri sırayla sütunlara dağıtmak için sayaç

        while (!pq.isEmpty()) {
            TaskNode gorev = pq.poll();
            VBox kart = kartTasarimiOlustur(gorev);

            if (sutunSirasi % 3 == 0) {
                sutun1.getChildren().add(kart);
            } else if (sutunSirasi % 3 == 1) {
                sutun2.getChildren().add(kart);
            } else {
                sutun3.getChildren().add(kart);
            }
            sutunSirasi++;
        }

        gorevKartAlani.getChildren().addAll(sutun1, sutun2, sutun3);
    }

    private VBox kartTasarimiOlustur(TaskNode gorev) {
        VBox kart = new VBox();
        kart.getStyleClass().add("task-card");
        kart.setMaxWidth(Double.MAX_VALUE);
        kart.setMaxHeight(Region.USE_PREF_SIZE);


        VBox headerContainer = new VBox(8);
        headerContainer.setStyle("-fx-padding: 15; -fx-cursor: hand;");

        HBox baslikSatiri = new HBox(10);
        baslikSatiri.setAlignment(Pos.CENTER_LEFT);

        Label okIkonu = new Label("▶");
        okIkonu.getStyleClass().add("collapse-icon");

        String baslikMetni = gorev.getTitle().toUpperCase();
        Label baslikLabel = new Label(baslikMetni);
        baslikLabel.getStyleClass().add("task-card-title");
        baslikLabel.setWrapText(false);

        javafx.scene.text.Text textNode = new javafx.scene.text.Text(baslikMetni);
        textNode.setFont(javafx.scene.text.Font.font("System", javafx.scene.text.FontWeight.BOLD, 15));
        double textGenisligi = textNode.getLayoutBounds().getWidth();
        double maksimumGenislik = 180.0;

        if (textGenisligi > maksimumGenislik) {
            double yeniFontBoyutu = 15.0 * (maksimumGenislik / textGenisligi);
            yeniFontBoyutu = Math.max(yeniFontBoyutu, 10.5);
            baslikLabel.setStyle("-fx-font-size: " + yeniFontBoyutu + "px; -fx-padding: 5 0 0 0;");
        }

        Region baslikSpacer = new Region();
        HBox.setHgrow(baslikSpacer, Priority.ALWAYS);

        baslikSatiri.getChildren().addAll(okIkonu, baslikLabel, baslikSpacer);

        HBox altBilgiSatiri = new HBox();
        altBilgiSatiri.setAlignment(Pos.CENTER_LEFT);

        Label atayanLabel = new Label("Görevi Atayan: " + gorev.getManagerName());
        atayanLabel.getStyleClass().add("small-text");
        atayanLabel.setStyle("-fx-text-fill: #64748b;");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        Label anaDeadline = new Label(gorev.getDeadline().toString());
        anaDeadline.setStyle("-fx-font-weight: bold; -fx-text-fill: #1d4ed8; -fx-font-size: 12px;");

        altBilgiSatiri.getChildren().addAll(atayanLabel, headerSpacer, anaDeadline);
        headerContainer.getChildren().addAll(baslikSatiri, altBilgiSatiri);

        VBox bodyContent = new VBox(12);
        bodyContent.setStyle("-fx-padding: 0 15 15 15;");
        bodyContent.setVisible(false);
        bodyContent.setManaged(false);

        VBox progBox = new VBox(5);
        HBox progTextSatiri = new HBox();
        Label progLabel = new Label("İlerleme");
        progLabel.getStyleClass().add("small-text");
        Region progSpacer = new Region(); HBox.setHgrow(progSpacer, Priority.ALWAYS);
        Label yuzdeLabel = new Label("%0");
        yuzdeLabel.getStyleClass().add("progress-text");
        progTextSatiri.getChildren().addAll(progLabel, progSpacer, yuzdeLabel);

        ProgressBar pb = new ProgressBar(0);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.getStyleClass().add("task-progress");
        progBox.getChildren().addAll(progTextSatiri, pb);

        Label detayLink = new Label("Görev Açıklamasını Gör ↗");
        detayLink.getStyleClass().add("description-link");
        detayLink.setAlignment(Pos.CENTER);
        detayLink.setMaxWidth(Double.MAX_VALUE);
        detayLink.setOnMouseClicked(e -> modalAc(gorev));

        VBox adimlarKutusu = new VBox(10);
        adimlarKutusu.setStyle("-fx-padding: 0 5 0 0;");
        List<CheckBox> cbs = new ArrayList<>();

        for (String stepRaw : gorev.getSteps()) {
            String[] parts = stepRaw.split("\\|");
            String adimMetni = parts[0].trim();
            String adimTarihi = (parts.length > 1) ? parts[1].trim() : "";

            boolean isDep = adimMetni.startsWith("[DEPENDENT]");
            String temizMetin = isDep ? adimMetni.replace("[DEPENDENT]", "") : adimMetni;

            HBox adimSatiri = new HBox(5);
            adimSatiri.setAlignment(Pos.CENTER_LEFT);

            CheckBox cb = new CheckBox(temizMetin);
            cb.getStyleClass().add("task-check");
            cb.setWrapText(true);
            cb.setMaxWidth(160);
            cbs.add(cb);

            Region stepSpacer = new Region(); HBox.setHgrow(stepSpacer, Priority.ALWAYS);
            Label stepDate = new Label(adimTarihi);
            stepDate.getStyleClass().add("step-deadline");

            if (isDep) {
                adimSatiri.setStyle("-fx-padding: 0 0 0 15;");
                cb.setDisable(true);
                adimSatiri.getChildren().addAll(new Label("↳"), cb, stepSpacer, stepDate, new Label("🔒"));
            } else {
                adimSatiri.getChildren().addAll(cb, stepSpacer, stepDate);
            }
            adimlarKutusu.getChildren().add(adimSatiri);
        }

        cbs.forEach(c -> c.setOnAction(e -> {
            double p = (double) cbs.stream().filter(CheckBox::isSelected).count() / cbs.size();
            pb.setProgress(p);
            yuzdeLabel.setText("%" + Math.round(p * 100));

            int idx = cbs.indexOf(c);
            if(c.isSelected() && idx + 1 < cbs.size() && gorev.getSteps().get(idx+1).contains("[DEPENDENT]")) {
                cbs.get(idx+1).setDisable(false);
            }
        }));

        ScrollPane adimlarScroll = new ScrollPane(adimlarKutusu);
        adimlarScroll.setFitToWidth(true);
        adimlarScroll.setPrefHeight(130);
        adimlarScroll.getStyleClass().add("scroll-pane-clean");
        adimlarScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        Separator s1 = new Separator(); s1.setStyle("-fx-opacity: 0.3;");
        Separator s2 = new Separator(); s2.setStyle("-fx-opacity: 0.3;");

        bodyContent.getChildren().addAll(progBox, s1, detayLink, s2, adimlarScroll);

        headerContainer.setOnMouseClicked(e -> {
            if (bodyContent.isVisible()) {
                bodyContent.setVisible(false);
                bodyContent.setManaged(false);
                okIkonu.setText("▶");
            } else {
                bodyContent.setVisible(true);
                bodyContent.setManaged(true);
                okIkonu.setText("▼");
            }
        });

        kart.getChildren().addAll(headerContainer, bodyContent);
        return kart;
    }

    private void modalAc(TaskNode gorev) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/taskmanager/TaskDetailPage.fxml"));
            Parent root = loader.load();

            TaskDetailPageController controller = loader.getController();
            controller.setTaskDetails(gorev);

            Stage stage = new Stage();
            stage.setTitle("Görev Detayları");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception ex) {
            System.err.println("Modal açılırken hata oluştu: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    @FXML private void onemliGorevler() { if (panelBaslik != null) panelBaslik.setText("Önemli Görevler"); }
    @FXML private void sonTarihYaklasanlar() { if (panelBaslik != null) panelBaslik.setText("Son Tarihi Yaklaşanlar"); }
    @FXML private void yeniGorevler() { if (panelBaslik != null) panelBaslik.setText("Yeni Eklenenler"); }

    @FXML
    private void cikisYap() {
        userService.logout();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/taskmanager/LoginPage.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = (Stage) anchorPane.getScene().getWindow();
            stage.setScene(scene);
        } catch (Exception e) {
            System.err.println("Çıkış hatası: " + e.getMessage());
        }
    }

    private String basHarfleriAl(String adSoyad) {
        if(adSoyad == null || adSoyad.trim().isEmpty()) return "?";
        String[] parcalar = adSoyad.trim().split("\\s+");
        if (parcalar.length == 1) return parcalar[0].substring(0, 1).toUpperCase();
        return (parcalar[0].substring(0, 1) + parcalar[parcalar.length - 1].substring(0, 1)).toUpperCase();
    }
}
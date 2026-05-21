package com.example.taskmanager.controller;

import com.example.taskmanager.model.TaskNode;
import com.example.taskmanager.model.User;
import com.example.taskmanager.service.TaskService;
import com.example.taskmanager.service.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.List;

public class ManagerPageController {

    @FXML private AnchorPane anchorPane;
    @FXML private TilePane calisanKartAlani;
    @FXML private HBox gorevSutunAlani;
    @FXML private Label departmanad;
    @FXML private Label profilLabel;
    @FXML private TextField aramaCubugu;
    @FXML private Label yeniMesajNoktasi;
    @FXML private Label sayfaAciklamasi;
    @FXML private HBox aramaAlani;
    private String aktifDepartman = null;

    private final UserService userService = UserService.getInstance();
    private User currentUser;

    @FXML
    public void initialize() {
        gorunumDegistir(false, "Çalışan bilgilerini buradan görüntüleyebilirsin.");

        if (aramaCubugu != null) {
            aramaCubugu.textProperty().addListener((observable, oldValue, newValue) -> {
                filtreleVeGoster(newValue);
            });
        }

        if (yeniMesajNoktasi != null) {
            boolean hasNew = TaskService.isHasNewMessages();
            yeniMesajNoktasi.setVisible(hasNew);
            yeniMesajNoktasi.setManaged(hasNew);
        }
        noktaKontroluYap();
    }

    private void noktaKontroluYap() {
        if (yeniMesajNoktasi != null) {
            yeniMesajNoktasi.setVisible(TaskService.isHasNewMessages());
        }
    }

    private void filtreleVeGoster(String arananKelime) {
        calisanKartAlani.getChildren().clear();

        LinkedList<User> sonucListesi =
                userService.searchEmployeesWithAVL(arananKelime, aktifDepartman);

        for (User emp : sonucListesi) {
            calisanKartEkle(emp);
        }
    }

    public void initWithUser(User user) {
        this.currentUser = user;
        if (user != null) {
            profilLabel.setText(user.getFullName());
        }
        tumCalisanlariGoster();
    }

    private void gorunumDegistir(boolean gorevModu, String aciklamaMetni) {
        calisanKartAlani.setVisible(!gorevModu);
        calisanKartAlani.setManaged(!gorevModu);

        if (gorevSutunAlani != null) {
            gorevSutunAlani.setVisible(gorevModu);
            gorevSutunAlani.setManaged(gorevModu);
        }

        if (sayfaAciklamasi != null) {
            sayfaAciklamasi.setText(aciklamaMetni);
        }

        if (aramaAlani != null) {
            aramaAlani.setVisible(!gorevModu);
            aramaAlani.setManaged(!gorevModu);
        }
    }


    @FXML
    private void tumCalisanlariGoster() {
        aktifDepartman = null;

        if (aramaCubugu != null) {
            aramaCubugu.clear();
        }

        gorunumDegistir(false, "Çalışan bilgilerini buradan görüntüleyebilirsin.");
        noktaKontroluYap();
        departmanad.setText("Tüm Çalışanlar");
        calisanKartAlani.getChildren().clear();

        LinkedList<User> employees = userService.getAllEmployees();

        for (User emp : employees) {
            calisanKartEkle(emp);
        }
    }

    private void departmanYukle(String dAd) {
        aktifDepartman = dAd;

        if (aramaCubugu != null) {
            aramaCubugu.clear();
        }

        gorunumDegistir(false, dAd + " departmanı çalışanlarını görüntülüyorsun.");
        departmanad.setText(dAd);
        calisanKartAlani.getChildren().clear();

        LinkedList<User> employees = userService.getEmployeesByDepartment(dAd);

        for (User emp : employees) {
            calisanKartEkle(emp);
        }
    }

    @FXML private void muhasebeGoster() { departmanYukle("Muhasebe"); }
    @FXML private void yazilimGoster() { departmanYukle("Yazılım"); }
    @FXML private void ikGoster() { departmanYukle("İnsan Kaynakları"); }
    @FXML private void pazarlamaGoster() { departmanYukle("Pazarlama"); }
    @FXML private void destekGoster() { departmanYukle("Destek"); }

    private void calisanKartEkle(User emp) {
        VBox kart = new VBox(10);
        kart.setAlignment(Pos.CENTER);
        kart.getStyleClass().add("employee-card");
        kart.setPrefSize(150, 180);
        kart.setStyle("-fx-cursor: hand;");

        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("avatar-circle");
        Label avatarHarf = new Label(basHarfleriAl(emp.getFullName()));
        avatarHarf.getStyleClass().add("avatar-letter");
        avatar.getChildren().add(avatarHarf);

        Label isimLabel = new Label(emp.getFullName());
        isimLabel.getStyleClass().add("employee-name");
        Label bilgiLabel = new Label(emp.getDepartment());
        bilgiLabel.getStyleClass().add("employee-info");

        kart.getChildren().addAll(avatar, isimLabel, bilgiLabel);

        kart.setOnMouseClicked(event -> {
            gorevEklePaneliAcParametreli(emp.getUsername());
        });

        calisanKartAlani.getChildren().add(kart);
    }

    @FXML
    private void atananGorevleriGoster() {
        gorunumDegistir(true, "Şu an sistemde aktif olan tüm görevleri görüntülüyorsun.");
        departmanad.setText("Atanan Görevler");
        gorevSutunAlani.getChildren().clear();

        VBox sutun1 = new VBox(20);
        VBox sutun2 = new VBox(20);
        VBox sutun3 = new VBox(20);

        VBox[] sutunlar = {sutun1, sutun2, sutun3};
        for (VBox sutun : sutunlar) {
            sutun.setPrefWidth(280);
            HBox.setHgrow(sutun, Priority.ALWAYS);
            gorevSutunAlani.getChildren().add(sutun);
        }

        PriorityQueue<TaskNode> tasks = new PriorityQueue<>(TaskService.getPriorityQueue());
        int sayac = 0;
        while (!tasks.isEmpty()) {
            TaskNode task = tasks.poll();
            sutunlar[sayac % 3].getChildren().add(kartTasarimiOlustur(task));
            sayac++;
        }
    }

    private VBox kartTasarimiOlustur(TaskNode gorev) {
        VBox kart = new VBox();
        kart.getStyleClass().add("task-card");
        kart.setMaxWidth(Double.MAX_VALUE);

        VBox header = new VBox(8);
        header.setStyle("-fx-padding: 15; -fx-cursor: hand;");

        HBox baslikSatiri = new HBox(10);
        baslikSatiri.setAlignment(Pos.CENTER_LEFT);
        Label okIkonu = new Label("▶");
        okIkonu.getStyleClass().add("collapse-icon");
        String baslikMetni = gorev.getTitle().toUpperCase();
        Label baslikLabel = new Label(baslikMetni);
        baslikLabel.getStyleClass().add("task-card-title");
        baslikLabel.setWrapText(false);

        Text textNode = new Text(baslikMetni);
        textNode.setFont(Font.font("System", FontWeight.BOLD, 15));
        double textGenisligi = textNode.getLayoutBounds().getWidth();
        double maksimumGenislik = 180.0;

        if (textGenisligi > maksimumGenislik) {
            double yeniFontBoyutu = 15.0 * (maksimumGenislik / textGenisligi);
            yeniFontBoyutu = Math.max(yeniFontBoyutu, 10.5);
            baslikLabel.setStyle("-fx-font-size: " + yeniFontBoyutu + "px; -fx-padding: 5 0 0 0;");
        }

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        baslikSatiri.getChildren().addAll(okIkonu, baslikLabel, sp);

        boolean isNewTask = false;
        if (gorev.getCreatedAt() != null) {

            isNewTask = gorev.getCreatedAt().isAfter(LocalDateTime.now().minusHours(24));
        }

        if (isNewTask) {
            Label kalemBtn = new Label("🖋 Düzenle");
            kalemBtn.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px; -fx-background-color: #f1f5f9; -fx-padding: 4 8; -fx-background-radius: 6;");

            kalemBtn.setOnMouseEntered(e -> kalemBtn.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px; -fx-background-color: #475569; -fx-padding: 4 8; -fx-background-radius: 6;"));
            kalemBtn.setOnMouseExited(e -> kalemBtn.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 11px; -fx-background-color: #f1f5f9; -fx-padding: 4 8; -fx-background-radius: 6;"));

            kalemBtn.setOnMouseClicked(e -> {
                e.consume();
                gorevDuzenlePaneliAc(gorev);
            });

            baslikSatiri.getChildren().add(kalemBtn);
        }


        HBox altBilgi = new HBox();
        altBilgi.setAlignment(Pos.CENTER_LEFT);
        String calisanlar = String.join(", ", gorev.getAssignedEmployees());
        Label cLabel = new Label("Atanan: " + calisanlar);
        cLabel.getStyleClass().add("small-text");
        Region sp2 = new Region(); HBox.setHgrow(sp2, Priority.ALWAYS);
        Label deadline = new Label(gorev.getDeadline().toString());
        deadline.setStyle("-fx-font-weight: bold; -fx-text-fill: #1d4ed8; -fx-font-size: 11px;");

        altBilgi.getChildren().addAll(cLabel, sp2, deadline);
        header.getChildren().addAll(baslikSatiri, altBilgi);

        VBox body = new VBox(12);
        body.setStyle("-fx-padding: 0 15 15 15;");
        body.setVisible(false);
        body.setManaged(false);

        VBox adimlar = new VBox(8);
        for (String stepRaw : gorev.getSteps()) {
            String[] parts = stepRaw.split("\\|");
            String adimMetni = parts[0].trim();

            boolean isDep = adimMetni.startsWith("[DEPENDENT]");
            String temizMetin = adimMetni.replace("[DEPENDENT]", "").replace("[DONE]", "").trim();

            HBox adimSatiri = new HBox(5);
            adimSatiri.setAlignment(Pos.CENTER_LEFT);

            CheckBox cb = new CheckBox(temizMetin);
            cb.getStyleClass().add("task-check");
            cb.setDisable(true);

            if (isDep) {
                adimSatiri.setStyle("-fx-padding: 0 0 0 15;");
                Label altOk = new Label("↳");
                Label kilit = new Label("🔒");
                kilit.setStyle("-fx-opacity: 0.5;");

                adimSatiri.getChildren().addAll(altOk, cb, new Region(), kilit);
                HBox.setHgrow(adimSatiri.getChildren().get(2), Priority.ALWAYS);
            } else {
                adimSatiri.getChildren().add(cb);
            }

            adimlar.getChildren().add(adimSatiri);
        }

        body.getChildren().addAll(new Separator(), adimlar);

        header.setOnMouseClicked(e -> {
            boolean visible = !body.isVisible();
            body.setVisible(visible);
            body.setManaged(visible);
            okIkonu.setText(visible ? "▼" : "▶");
        });

        kart.getChildren().addAll(header, body);
        return kart;
    }

    private void gorevDuzenlePaneliAc(TaskNode task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/taskmanager/AddTaskPage.fxml"));
            Parent root = loader.load();

            AddTaskController controller = loader.getController();
            controller.loadTaskForEdit(task);

            Stage stage = new Stage();
            stage.setTitle("Görevi Güncelle");
            stage.setScene(new Scene(root));
            stage.show();

            stage.setOnHidden(e -> atananGorevleriGoster()); // Kapanınca listeyi yenile
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String basHarfleriAl(String ad) {
        if (ad == null || ad.isEmpty()) return "?";
        String[] parcalar = ad.split("\\s+");
        if (parcalar.length > 1) return (parcalar[0].charAt(0) + "" + parcalar[parcalar.length-1].charAt(0)).toUpperCase();
        return (parcalar[0].charAt(0) + "").toUpperCase();
    }

    @FXML
    private void cikisYap() {
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/com/example/taskmanager/LoginPage.fxml"));
            anchorPane.getScene().setRoot(login);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void gorevEklePaneliAc() {
        gorevEklePaneliAcParametreli(null);
    }

    private void gorevEklePaneliAcParametreli(String targetUsername) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/taskmanager/AddTaskPage.fxml"));
            Parent root = loader.load();

            AddTaskController addTaskController = loader.getController();
            if (targetUsername != null) {
                addTaskController.initSelectedUser(targetUsername);
            }

            Stage stage = new Stage();
            stage.setTitle("Yeni Görev Ekle");
            stage.setScene(new Scene(root));

            // ÇOK ÖNEMLİ EKLENTİ: Sen görev ekleyip pencereyi kapattığında ekran anında yenilensin!
            stage.setOnHidden(e -> atananGorevleriGoster());

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void sorulanSorulariGoster() {
        gorunumDegistir(true, "Çalışanlardan gelen soruları ve geri bildirimleri yönetebilirsin.");
        departmanad.setText("Gelen Sorular");

        TaskService.markAllMessagesAsRead();

        if (yeniMesajNoktasi != null) {
            yeniMesajNoktasi.setVisible(false);
            yeniMesajNoktasi.setManaged(false);
        }
        gorevSutunAlani.getChildren().clear();
        gorevSutunAlani.setSpacing(0);

        VBox anaSutun = new VBox(15);
        HBox.setHgrow(anaSutun, Priority.ALWAYS);
        gorevSutunAlani.getChildren().add(anaSutun);

        List<TaskService.TaskMessage> mesajlar = TaskService.getMessages();
        if (mesajlar.isEmpty()) {
            anaSutun.getChildren().add(new Label("Henüz bir soru sorulmamış."));
            return;
        }

        for (TaskService.TaskMessage msg : mesajlar) {
            anaSutun.getChildren().add(soruKutusuOlustur(msg));
        }
    }

    private VBox soruKutusuOlustur(TaskService.TaskMessage msg) {
        VBox kutu = new VBox(10);
        kutu.getStyleClass().add("task-card");
        kutu.setMaxWidth(Double.MAX_VALUE);
        kutu.setStyle("-fx-padding: 20; -fx-background-color: white;");

        Label info = new Label("GÖNDEREN:  " + msg.sender.toUpperCase() + "     |     GÖREV:  " + msg.taskTitle.toUpperCase());
        info.getStyleClass().add("page-title");
        info.setStyle("-fx-font-weight: bold; -fx-font-size: 11px;");

        Separator sep = new Separator();
        sep.setOpacity(0.2);

        Label icerik = new Label(msg.content);
        icerik.setWrapText(true);
        icerik.setStyle("-fx-font-size: 13px; -fx-text-fill: #7b8a9b; -fx-padding: 5 0 15 0;");

        HBox altSatir = new HBox();
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button onayButonu = new Button("ONAY");
        onayButonu.setStyle("-fx-background-color: #1d4ed8; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 30; " +
                "-fx-background-radius: 18;");

        onayButonu.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Onay");
            alert.setHeaderText(null);
            alert.setContentText("Onayladığınız mesaj sayfadan silinecektir, emin misiniz?");

            if (alert.showAndWait().get() == ButtonType.OK) {
                TaskService.removeMessage(msg);
                sorulanSorulariGoster();
            }
        });

        altSatir.getChildren().addAll(spacer, onayButonu);
        kutu.getChildren().addAll(info, sep, icerik, altSatir);

        return kutu;
    }
}
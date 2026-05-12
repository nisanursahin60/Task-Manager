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

import java.util.LinkedList;
import java.util.PriorityQueue;

public class ManagerPageController {

    @FXML private AnchorPane anchorPane;
    @FXML private TilePane calisanKartAlani; // Mevcut çalışan listesi (TilePane)
    @FXML private HBox gorevSutunAlani;     // Yeni eklediğimiz görev sütunları (HBox)
    @FXML private Label departmanad;
    @FXML private Label profilLabel;

    private final UserService userService = UserService.getInstance();
    private User currentUser;

    @FXML
    public void initialize() {
        // İlk açılışta çalışanlar listesi görünsün
        gorunumDegistir(false);
    }

    public void initWithUser(User user) {
        this.currentUser = user;
        if (user != null) {
            profilLabel.setText(user.getFullName());
        }
        tumCalisanlariGoster();
    }

    /**
     * Görünüm Yönetici:
     * gorevModu true ise HBox (Sütunlar) görünür, TilePane gizlenir.
     * gorevModu false ise TilePane (Çalışanlar) görünür, HBox gizlenir.
     */
    private void gorunumDegistir(boolean gorevModu) {
        calisanKartAlani.setVisible(!gorevModu);
        calisanKartAlani.setManaged(!gorevModu);

        if (gorevSutunAlani != null) {
            gorevSutunAlani.setVisible(gorevModu);
            gorevSutunAlani.setManaged(gorevModu);
        }
    }

    // --- ÇALIŞAN LİSTELEME (ESKİ DÜZEN) ---
    @FXML
    private void tumCalisanlariGoster() {
        gorunumDegistir(false);
        departmanad.setText("Tüm Çalışanlar");
        calisanKartAlani.getChildren().clear();
        LinkedList<User> employees = userService.getAllEmployees();
        for (User emp : employees) {
            calisanKartEkle(emp.getFullName(), emp.getDepartment());
        }
    }

    private void departmanYukle(String dAd) {
        gorunumDegistir(false);
        departmanad.setText(dAd);
        calisanKartAlani.getChildren().clear();
        LinkedList<User> employees = userService.getEmployeesByDepartment(dAd);
        for (User emp : employees) {
            calisanKartEkle(emp.getFullName(), emp.getDepartment());
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
        kart.setPrefSize(150, 180);

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

    // --- GÖREV LİSTELEME (YENİ SÜTUNLU DÜZEN) ---
    @FXML
    private void atananGorevleriGoster() {
        gorunumDegistir(true);
        departmanad.setText("Atanan Görevler");
        gorevSutunAlani.getChildren().clear();

        // 3 adet bağımsız sütun (VBox) oluşturuyoruz
        VBox sutun1 = new VBox(20);
        VBox sutun2 = new VBox(20);
        VBox sutun3 = new VBox(20);

        VBox[] sutunlar = {sutun1, sutun2, sutun3};
        for (VBox sutun : sutunlar) {
            sutun.setPrefWidth(280);
            HBox.setHgrow(sutun, Priority.ALWAYS);
            gorevSutunAlani.getChildren().add(sutun);
        }

        // Görevleri öncelik sırasına göre (Heap/PriorityQueue) dağıt
        PriorityQueue<TaskNode> tasks = new PriorityQueue<>(TaskService.getPriorityQueue());
        int sayac = 0;
        while (!tasks.isEmpty()) {
            TaskNode task = tasks.poll();
            sutunlar[sayac % 3].getChildren().add(kartTasarimiOlustur(task));
            sayac++;
        }
    }

    // Görev Kartı Tasarımı (EmployeePage'deki gibi genişleyebilir yapı)
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

        // --- PUNTO KÜÇÜLTME MANTIĞI (GÜNCELLENEN YER) ---
        // Görünmez bir Text nesnesi ile başlığın gerçek genişliğini ölçüyoruz
        Text textNode = new Text(baslikMetni);
        textNode.setFont(Font.font("System", FontWeight.BOLD, 15));
        double textGenisligi = textNode.getLayoutBounds().getWidth();
        double maksimumGenislik = 180.0;

        if (textGenisligi > maksimumGenislik) {
            // Eğer başlık 180px'den genişse, oranı koruyarak fontu küçültüyoruz
            double yeniFontBoyutu = 15.0 * (maksimumGenislik / textGenisligi);
            yeniFontBoyutu = Math.max(yeniFontBoyutu, 10.5); // Minimum 10.5 puntoya kadar düşer
            baslikLabel.setStyle("-fx-font-size: " + yeniFontBoyutu + "px; -fx-padding: 5 0 0 0;");
        }

        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        baslikSatiri.getChildren().addAll(okIkonu, baslikLabel, sp);

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
        for (String step : gorev.getSteps()) {
            CheckBox cb = new CheckBox(step);
            cb.getStyleClass().add("task-check");
            cb.setDisable(true); // Yönetici sadece izler
            adimlar.getChildren().add(cb);
        }

        body.getChildren().addAll(new Separator(), adimlar);

        // Genişletme/Daraltma Mantığı
        header.setOnMouseClicked(e -> {
            boolean visible = !body.isVisible();
            body.setVisible(visible);
            body.setManaged(visible);
            okIkonu.setText(visible ? "▼" : "▶");
        });

        kart.getChildren().addAll(header, body);
        return kart;
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
        try {
            Stage stage = new Stage();
            stage.setTitle("Yeni Görev Ekle");
            stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/com/example/taskmanager/AddTaskPage.fxml"))));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
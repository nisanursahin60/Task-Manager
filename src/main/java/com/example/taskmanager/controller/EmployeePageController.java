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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class EmployeePageController {

    @FXML private HBox gorevKartAlani;
    @FXML private Label panelBaslik;
    @FXML private AnchorPane anchorPane;

    @FXML private Label profilAdLabel;
    @FXML private Label profilRolLabel;
    @FXML private Label panelAciklamasi;
    @FXML private Label avatarLabel;

    private final UserService userService = UserService.getInstance();
    private User currentUser;

    @FXML private Label yeniGorevNokta;

    /** VERİ YAPISI 1: STACK — navigasyon geçmişi */
    private final Stack<String> viewHistory = new Stack<>();

    /**
     * VERİ YAPISI 2: HASHSET — yıldızlı görev title'larını O(1) ile tutar.
     */
    private final HashSet<String> starredTitles = new HashSet<>();
    private final Stack<TaskNode> starStack = new Stack<>();

    private static final String VIEW_TUM      = "TÜM GÖREVLERİM";
    private static final String VIEW_YAKLASAN = "Son Tarihi Yaklaşanlar";
    private static final String VIEW_YILDIZLI = "Yıldızlı Görevler";
    private static final String VIEW_YENI     = "Yeni Eklenenler";
    private static final String VIEW_TAMAMLANAN = "Tamamlanan Görevler";

    @FXML
    public void initialize() { }

    public void initWithUser(User user) {
        this.currentUser = user;

        if (profilAdLabel != null) {
            profilAdLabel.setText(user.getFullName());
        }

        if (profilRolLabel != null) {
            profilRolLabel.setText(
                    user.getTitle() != null ? user.getTitle() : user.getDepartment()
            );
        }

        if (avatarLabel != null) {
            avatarLabel.setText(basHarfleriAl(user.getFullName()));
        }

        syncStarredSet();
        refreshYeniNokta();
        tumGorevleriGoster();
    }

    private void refreshYeniNokta() {
        if (yeniGorevNokta == null || currentUser == null) return;
        boolean var = TaskService.hasUnseenNewTasksForEmployee(currentUser.getUsername());
        yeniGorevNokta.setVisible(var);
        yeniGorevNokta.setManaged(var);
    }

    private void syncStarredSet() {
        starredTitles.clear();

        PriorityQueue<TaskNode> tum = TaskService.getTasksForEmployee(currentUser.getUsername());

        for (TaskNode t : tum) {
            if (t.isStarred()) {
                starredTitles.add(t.getTitle());
            }
        }
    }

    // -------------------------------------------------------------------------
    // MENÜ AKSİYONLARI
    // -------------------------------------------------------------------------

    @FXML
    private void tumGorevleriGoster() {
        viewHistory.push(VIEW_TUM);

        if (panelBaslik != null) {
            panelBaslik.setText(VIEW_TUM);
        }
        if (panelAciklamasi != null) {
            panelAciklamasi.setText("Tamamladığın adımları işaretlemeyi unutma.");
        }
        List<TaskNode> sirali =
                TaskService.getTasksForEmployeeByCreatedAt(currentUser.getUsername());

        kartlariEkranListeden(sirali, false);
    }

    @FXML
    private void sonTarihYaklasanlar() {
        viewHistory.push(VIEW_YAKLASAN);

        if (panelBaslik != null) {
            panelBaslik.setText(VIEW_YAKLASAN);
        }
        if (panelAciklamasi != null) {
            panelAciklamasi.setText("Son 7 günü kalan görevler görüntülenmektedir.");
        }

        /**
         * VERİ YAPISI 3: PRIORITYQUEUE (Min-Heap) — deadline'a göre sıralı
         */
        PriorityQueue<TaskNode> tumGorevler =
                TaskService.getTasksForEmployee(currentUser.getUsername());

        PriorityQueue<TaskNode> yaklasanlar = new PriorityQueue<>();

        LocalDate bugun = LocalDate.now();
        LocalDate sinir = bugun.plusDays(7);

        for (TaskNode t : tumGorevler) {
            if (t.getDeadline() != null
                    && !t.getDeadline().isBefore(bugun)
                    && !t.getDeadline().isAfter(sinir)) {
                yaklasanlar.offer(t);
            }
        }

        kartlariEkrana(yaklasanlar, true);
    }

    @FXML
    private void yildizliGorevler() {
        viewHistory.push(VIEW_YILDIZLI);

        if (panelBaslik != null) {
            panelBaslik.setText(VIEW_YILDIZLI);
        }
        if (panelAciklamasi != null) {
            panelAciklamasi.setText("Yıldızlı işaretlenen görevler görüntülenmektedir.");
        }

        PriorityQueue<TaskNode> starred =
                TaskService.getStarredTasksForEmployee(currentUser.getUsername());

        kartlariEkrana(starred, false);
    }

    @FXML
    private void yeniGorevler() {
        viewHistory.push(VIEW_YENI);
        if (panelBaslik != null) panelBaslik.setText(VIEW_YENI);
        if (panelAciklamasi != null) { // Eklendi
            panelAciklamasi.setText("Yönetici tarafından son 3 gün içinde atanan görevler görüntülenmektedir.");
        }

        List<TaskNode> yeniGorevler = TaskService.getNewTasksForEmployee(currentUser.getUsername());

        // Kullanıcı bu sekmeyi açtığında tüm görevleri "görüldü" say
        TaskService.markTasksAsSeenByEmployee(yeniGorevler, currentUser.getUsername());

        // Nokta sönsün
        refreshYeniNokta();

        kartlariEkranListeden(yeniGorevler, false);
    }

    @FXML
    void tamamlananGorevler() {
        viewHistory.push(VIEW_TAMAMLANAN);

        if (panelBaslik != null) {
            panelBaslik.setText(VIEW_TAMAMLANAN);
        }
        if (panelAciklamasi != null) { // Eklendi
            panelAciklamasi.setText("Tamamlanıp ve arşive kaldırılan görevler görüntülenmektedir.");
        }

        List<TaskNode> tamamlananlar =
                TaskService.getCompletedTasksForEmployee(currentUser.getUsername());

        kartlariEkranListeden(tamamlananlar, false);
    }

    // -------------------------------------------------------------------------
    // KARTLARI EKRANA DÖŞE — List<TaskNode> versiyonu
    // -------------------------------------------------------------------------

    /**
     * VERİ YAPISI 4: LINKEDLIST — sıralı görevleri 3 sütuna dağıtmak için
     * VERİ YAPISI 5: HASHMAP   — urgency grubu → badge rengi
     */
    private void kartlariEkranListeden(List<TaskNode> liste, boolean urgencyBadge) {
        gorevKartAlani.getChildren().clear();

        if (liste.isEmpty()) {
            Label bos = new Label("Bu kategoride görev bulunmuyor.");
            bos.setStyle("-fx-font-size: 14px; -fx-text-fill: #7b8a9b;");
            gorevKartAlani.getChildren().add(bos);
            return;
        }

        HashMap<String, String> urgencyColors = new HashMap<>();
        urgencyColors.put("KRITIK",   "#dc2626");
        urgencyColors.put("YAKLASAN", "#dc2626");
        urgencyColors.put("NORMAL",   "#16a34a");

        VBox sutun1 = new VBox(20);
        VBox sutun2 = new VBox(20);
        VBox sutun3 = new VBox(20);

        HBox.setHgrow(sutun1, Priority.ALWAYS);
        HBox.setHgrow(sutun2, Priority.ALWAYS);
        HBox.setHgrow(sutun3, Priority.ALWAYS);

        int index = 0;

        for (TaskNode gorev : liste) {
            String urgencyKey = urgencyHesapla(gorev.getDeadline());
            String badgeRenk  = urgencyColors.get(urgencyKey);

            VBox kart = kartTasarimiOlustur(
                    gorev,
                    urgencyBadge ? badgeRenk : null,
                    urgencyKey
            );

            switch (index % 3) {
                case 0 -> sutun1.getChildren().add(kart);
                case 1 -> sutun2.getChildren().add(kart);
                case 2 -> sutun3.getChildren().add(kart);
            }

            index++;
        }

        gorevKartAlani.getChildren().addAll(sutun1, sutun2, sutun3);
    }

    // -------------------------------------------------------------------------
    // KARTLARI EKRANA DÖŞE — PriorityQueue versiyonu
    // -------------------------------------------------------------------------

    private void kartlariEkrana(PriorityQueue<TaskNode> pq, boolean urgencyBadge) {
        gorevKartAlani.getChildren().clear();

        if (pq.isEmpty()) {
            Label bos = new Label("Bu kategoride görev bulunmuyor.");
            bos.setStyle("-fx-font-size: 14px; -fx-text-fill: #7b8a9b;");
            gorevKartAlani.getChildren().add(bos);
            return;
        }

        HashMap<String, String> urgencyColors = new HashMap<>();
        urgencyColors.put("KRITIK",   "#dc2626");
        urgencyColors.put("YAKLASAN", "#dc2626");
        urgencyColors.put("NORMAL",   "#16a34a");

        LinkedList<TaskNode> siraliGorevler = new LinkedList<>();

        while (!pq.isEmpty()) {
            siraliGorevler.addLast(pq.poll());
        }

        VBox sutun1 = new VBox(20);
        VBox sutun2 = new VBox(20);
        VBox sutun3 = new VBox(20);

        HBox.setHgrow(sutun1, Priority.ALWAYS);
        HBox.setHgrow(sutun2, Priority.ALWAYS);
        HBox.setHgrow(sutun3, Priority.ALWAYS);

        int index = 0;

        for (TaskNode gorev : siraliGorevler) {
            String urgencyKey = urgencyHesapla(gorev.getDeadline());
            String badgeRenk  = urgencyColors.get(urgencyKey);

            VBox kart = kartTasarimiOlustur(
                    gorev,
                    urgencyBadge ? badgeRenk : null,
                    urgencyKey
            );

            switch (index % 3) {
                case 0 -> sutun1.getChildren().add(kart);
                case 1 -> sutun2.getChildren().add(kart);
                case 2 -> sutun3.getChildren().add(kart);
            }

            index++;
        }

        gorevKartAlani.getChildren().addAll(sutun1, sutun2, sutun3);
    }

    private String urgencyHesapla(LocalDate deadline) {
        if (deadline == null) {
            return "NORMAL";
        }

        long gunFarki = ChronoUnit.DAYS.between(LocalDate.now(), deadline);

        if (gunFarki <= 0) {
            return "KRITIK";
        }

        if (gunFarki <= 7) {
            return "YAKLASAN";
        }

        return "NORMAL";
    }

    // -------------------------------------------------------------------------
    // KART TASARIMI
    // -------------------------------------------------------------------------

    private VBox kartTasarimiOlustur(TaskNode gorev, String badgeRenk, String urgencyKey) {
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
        textNode.setFont(javafx.scene.text.Font.font(
                "System",
                javafx.scene.text.FontWeight.BOLD,
                15
        ));

        double textGenisligi = textNode.getLayoutBounds().getWidth();
        double maksimumGenislik = 180.0;

        if (textGenisligi > maksimumGenislik) {
            double yeni = Math.max(15.0 * (maksimumGenislik / textGenisligi), 10.5);
            baslikLabel.setStyle("-fx-font-size: " + yeni + "px; -fx-padding: 5 0 0 0;");
        }

        Region baslikSpacer = new Region();
        HBox.setHgrow(baslikSpacer, Priority.ALWAYS);

        boolean isStarred = starredTitles.contains(gorev.getTitle());

        Label yildizIkonu = new Label(isStarred ? "★" : "☆");
        yildizIkonu.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-cursor: hand;" +
                        "-fx-text-fill: " + (isStarred ? "#f59e0b" : "#cbd5e1") + ";"
        );

        yildizIkonu.setOnMouseClicked(e -> {
            e.consume();

            TaskService.toggleStar(gorev);
            syncStarredSet();

            boolean nowStarred = starredTitles.contains(gorev.getTitle());

            yildizIkonu.setText(nowStarred ? "★" : "☆");
            yildizIkonu.setStyle(
                    "-fx-font-size: 18px;" +
                            "-fx-cursor: hand;" +
                            "-fx-text-fill: " + (nowStarred ? "#f59e0b" : "#cbd5e1") + ";"
            );
        });

        if (badgeRenk != null) {
            long kalanGun = ChronoUnit.DAYS.between(LocalDate.now(), gorev.getDeadline());

            String kalanGunMetin;

            if (kalanGun < 0) {
                kalanGunMetin = Math.abs(kalanGun) + " gün geçti";
            } else if (kalanGun == 0) {
                kalanGunMetin = "Bugün son gün!";
            } else {
                kalanGunMetin = kalanGun + " gün kaldı";
            }

            Label badge = new Label(kalanGunMetin);
            badge.setStyle(
                    "-fx-background-color: " + badgeRenk + "22;" +
                            "-fx-text-fill: " + badgeRenk + ";" +
                            "-fx-font-size: 10px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 6;" +
                            "-fx-padding: 2 6;"
            );

            baslikSatiri.getChildren().addAll(
                    okIkonu,
                    baslikLabel,
                    baslikSpacer,
                    badge,
                    yildizIkonu
            );
        } else {
            baslikSatiri.getChildren().addAll(
                    okIkonu,
                    baslikLabel,
                    baslikSpacer,
                    yildizIkonu
            );
        }

        HBox altBilgiSatiri = new HBox();
        altBilgiSatiri.setAlignment(Pos.CENTER_LEFT);

        Label atayanLabel = new Label("Görevi Atayan: " + gorev.getManagerName());
        atayanLabel.getStyleClass().add("small-text");
        atayanLabel.setStyle("-fx-text-fill: #64748b;");

        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);

        String deadlineRenk = "#1d4ed8";

        Label anaDeadline = new Label(gorev.getDeadline().toString());
        anaDeadline.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + deadlineRenk + ";" +
                        "-fx-font-size: 12px;"
        );

        altBilgiSatiri.getChildren().addAll(
                atayanLabel,
                headerSpacer,
                anaDeadline
        );

        headerContainer.getChildren().addAll(
                baslikSatiri,
                altBilgiSatiri
        );

        // ---------------------------------------------------------------------
        // BODY
        // ---------------------------------------------------------------------

        VBox bodyContent = new VBox(12);
        bodyContent.setStyle("-fx-padding: 0 15 15 15;");
        bodyContent.setVisible(false);
        bodyContent.setManaged(false);

        VBox progBox = new VBox(5);

        HBox progTextSatiri = new HBox();

        Label progLabel = new Label("İlerleme");
        progLabel.getStyleClass().add("small-text");

        Region progSpacer = new Region();
        HBox.setHgrow(progSpacer, Priority.ALWAYS);

        Label yuzdeLabel = new Label("%0");
        yuzdeLabel.getStyleClass().add("progress-text");

        progTextSatiri.getChildren().addAll(
                progLabel,
                progSpacer,
                yuzdeLabel
        );

        ProgressBar pb = new ProgressBar(0);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.getStyleClass().add("task-progress");

        progBox.getChildren().addAll(
                progTextSatiri,
                pb
        );

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

            Region stepSpacer = new Region();
            HBox.setHgrow(stepSpacer, Priority.ALWAYS);

            Label stepDate = new Label(adimTarihi);
            stepDate.getStyleClass().add("step-deadline");

            if (isDep) {
                adimSatiri.setStyle("-fx-padding: 0 0 0 15;");
                cb.setDisable(true);

                adimSatiri.getChildren().addAll(
                        new Label("↳"),
                        cb,
                        stepSpacer,
                        stepDate,
                        new Label("🔒")
                );
            } else {
                adimSatiri.getChildren().addAll(
                        cb,
                        stepSpacer,
                        stepDate
                );
            }

            adimlarKutusu.getChildren().add(adimSatiri);
        }

        ScrollPane adimlarScroll = new ScrollPane(adimlarKutusu);
        adimlarScroll.setFitToWidth(true);
        adimlarScroll.setPrefHeight(130);
        adimlarScroll.getStyleClass().add("scroll-pane-clean");
        adimlarScroll.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-background: transparent;"
        );

        Separator s1 = new Separator();
        s1.setStyle("-fx-opacity: 0.3;");

        Separator s2 = new Separator();
        s2.setStyle("-fx-opacity: 0.3;");

        // ---------------------------------------------------------------------
        // GÖREVİ TAMAMLA BUTONU / COMPLETED LABEL
        // ---------------------------------------------------------------------

        boolean buKullaniciTamamladi =
                gorev.isCompletedBy(currentUser.getUsername());

        Button tamamlaButton = new Button("Görevi Tamamla");

        Label completedLabel = new Label("Completed");
        completedLabel.setStyle(
                "-fx-background-color: #dcfce7;" +
                        "-fx-text-fill: #16a34a;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 8 14;"
        );

        HBox tamamlaSatiri = new HBox();
        tamamlaSatiri.setAlignment(Pos.CENTER_RIGHT);

        if (buKullaniciTamamladi) {
            tamamlaButton.setVisible(false);
            tamamlaButton.setManaged(false);

            completedLabel.setVisible(true);
            completedLabel.setManaged(true);

            tamamlaSatiri.getChildren().add(completedLabel);

            pb.setProgress(1);
            yuzdeLabel.setText("%100");

            for (CheckBox cb : cbs) {
                cb.setSelected(true);
                cb.setDisable(true);
            }

        } else {
            completedLabel.setVisible(false);
            completedLabel.setManaged(false);

            tamamlaButton.setDisable(true);
            tamamlaButton.setStyle(
                    "-fx-background-color: #cbd5e1;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 8 14;" +
                            "-fx-opacity: 0.6;" +
                            "-fx-cursor: default;"
            );

            tamamlaSatiri.getChildren().add(tamamlaButton);
        }

        if (!buKullaniciTamamladi && cbs.isEmpty()) {
            tamamlaButton.setDisable(false);
            tamamlaButton.setStyle(
                    "-fx-background-color: #2563eb;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-background-radius: 8;" +
                            "-fx-padding: 8 14;" +
                            "-fx-opacity: 1;" +
                            "-fx-cursor: hand;"
            );
        }

        if (!buKullaniciTamamladi) {
            cbs.forEach(c -> c.setOnAction(e -> {
                int idx = cbs.indexOf(c);

                // --- TİK ATMA VE GERİ ALMA (KİLİT MANTIĞI) ---
                if (idx + 1 < cbs.size() && gorev.getSteps().get(idx + 1).contains("[DEPENDENT]")) {
                    CheckBox altGorev = cbs.get(idx + 1);
                    HBox altSatir = (HBox) adimlarKutusu.getChildren().get(idx + 1);

                    // Satırın en sonundaki eleman kilit ikonudur (🔒 veya 🔓)
                    Label kilitIkonu = (Label) altSatir.getChildren().get(altSatir.getChildren().size() - 1);

                    if (c.isSelected()) {
                        // Üst görev seçildiyse: Bir altındaki bağlı görevin kilidini aç
                        altGorev.setDisable(false);
                        kilitIkonu.setText("🔓");
                    } else {
                        // Üst görevin tiki kaldırıldıysa: Altındaki tüm bağlı görevleri zincirleme kapat ve kilitle
                        zincirlemeKapat(cbs, adimlarKutusu, idx + 1, gorev.getSteps());
                    }
                }

                // İlerleme çubuğunu (ProgressBar) ve yüzdelik durumları anlık güncelle
                double p = (double) cbs.stream()
                        .filter(CheckBox::isSelected)
                        .count() / cbs.size();

                pb.setProgress(p);
                yuzdeLabel.setText("%" + Math.round(p * 100));

                // İlerleme durumuna göre "Görevi Tamamla" butonunu aktif/deaktif et
                if (p == 1.0) {
                    tamamlaButton.setDisable(false);
                    tamamlaButton.setStyle(
                            "-fx-background-color: #2563eb;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-background-radius: 8;" +
                                    "-fx-padding: 8 14;" +
                                    "-fx-opacity: 1;" +
                                    "-fx-cursor: hand;"
                    );
                } else {
                    tamamlaButton.setDisable(true);
                    tamamlaButton.setStyle(
                            "-fx-background-color: #cbd5e1;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-background-radius: 8;" +
                                    "-fx-padding: 8 14;" +
                                    "-fx-opacity: 0.6;" +
                                    "-fx-cursor: default;"
                    );
                }
            }));
        }

        tamamlaButton.setOnAction(e -> {
            if (gorev.isCompletedBy(currentUser.getUsername())) {
                return;
            }

            Alert onay = new Alert(Alert.AlertType.CONFIRMATION);
            onay.setTitle("Görevi Tamamla");
            onay.setHeaderText(null);
            onay.setContentText("\"" + gorev.getTitle() + "\" görevini tamamlamak istediğinizden emin misiniz?");

            if (onay.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }

            TaskService.completeTask(gorev, currentUser.getUsername());

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Görev Tamamlandı");
            alert.setHeaderText(null);
            alert.setContentText("Görev başarıyla tamamlandı.");
            alert.showAndWait();

            String aktifBaslik = panelBaslik != null ? panelBaslik.getText() : "";

            if (aktifBaslik.equals(VIEW_TAMAMLANAN)) {
                tamamlananGorevler();
            } else if (aktifBaslik.equals(VIEW_TUM)) {
                tumGorevleriGoster();
            } else if (aktifBaslik.equals(VIEW_YAKLASAN)) {
                sonTarihYaklasanlar();
            } else if (aktifBaslik.equals(VIEW_YILDIZLI)) {
                yildizliGorevler();
            } else if (aktifBaslik.equals(VIEW_YENI)) {
                yeniGorevler();
            } else {
                tumGorevleriGoster();
            }
        });

        bodyContent.getChildren().addAll(
                progBox,
                s1,
                detayLink,
                s2,
                adimlarScroll,
                tamamlaSatiri
        );

        headerContainer.setOnMouseClicked(e -> {
            boolean acik = bodyContent.isVisible();

            bodyContent.setVisible(!acik);
            bodyContent.setManaged(!acik);

            okIkonu.setText(acik ? "▶" : "▼");
        });

        kart.getChildren().addAll(
                headerContainer,
                bodyContent
        );

        return kart;
    }

    // -------------------------------------------------------------------------
    // YARDIMCI METODLAR
    // -------------------------------------------------------------------------
    /**
     * Üst görevin tiki kaldırıldığında ona bağlı olan tüm alt görevleri
     * recursive (zincirleme) olarak kilitleyen ve tiklerini sıfırlayan yardımcı metot.
     */
    private void zincirlemeKapat(List<CheckBox> cbs, VBox adimlarKutusu, int index, List<String> steps) {
        if (index >= cbs.size()) return;

        // Şu anki alt görevin tikini kaldır ve deaktif et
        CheckBox mevcutAlt = cbs.get(index);
        mevcutAlt.setSelected(false);
        mevcutAlt.setDisable(true);

        // Kilit ikonunu tekrar kapalıya (🔒) çevir
        HBox satir = (HBox) adimlarKutusu.getChildren().get(index);
        if (satir != null && !satir.getChildren().isEmpty()) {
            Label kilitIkonu = (Label) satir.getChildren().get(satir.getChildren().size() - 1);
            kilitIkonu.setText("🔒");
        }

        // Eğer bir sonraki adım da [DEPENDENT] yapısındaysa zinciri bozmadan kapatmaya devam et
        if (index + 1 < cbs.size() && steps.get(index + 1).contains("[DEPENDENT]")) {
            zincirlemeKapat(cbs, adimlarKutusu, index + 1, steps);
        }
    }

    private void modalAc(TaskNode gorev) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/taskmanager/TaskDetailPage.fxml")
            );

            Parent root = loader.load();

            TaskDetailPageController controller = loader.getController();
            controller.setTaskDetails(gorev);

            Stage stage = new Stage();
            stage.setTitle("Görev Detayları");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();

        } catch (Exception ex) {
            System.err.println("Modal açılırken hata: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    @FXML
    private void takvimSayfasiniAc() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/taskmanager/CalendarPage.fxml"));
            AnchorPane takvimSayfasi = loader.load();

            CalendarPageController controller = loader.getController();
            controller.initWithUser(currentUser);
            if (panelBaslik != null) panelBaslik.setVisible(false);
            if (panelAciklamasi != null) {
                panelAciklamasi.setText("Görevlerinin son teslim tarihlerini takvim üzerinden takip edebilirsin.");
            }

            //anchorPane.getChildren().setAll(takvimSayfasi);

            gorevKartAlani.getChildren().clear();
            HBox.setHgrow(takvimSayfasi, Priority.ALWAYS);
            gorevKartAlani.getChildren().add(takvimSayfasi);

            if (panelBaslik != null) {
                panelBaslik.setText("Aylık Takvim Görünümü");
                panelBaslik.setVisible(true);
            }

            viewHistory.push("Aylık Takvim Görünümü");

        } catch (Exception e) {
            System.err.println("Takvim açılırken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @FXML
    private void cikisYap() {
        try {
            Parent login = FXMLLoader.load(getClass().getResource("/com/example/taskmanager/LoginPage.fxml"));
            anchorPane.getScene().setRoot(login);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private String basHarfleriAl(String adSoyad) {
        if (adSoyad == null || adSoyad.trim().isEmpty()) {
            return "?";
        }

        String[] parcalar = adSoyad.trim().split("\\s+");

        if (parcalar.length == 1) {
            return parcalar[0].substring(0, 1).toUpperCase();
        }

        return (parcalar[0].substring(0, 1) +
                parcalar[parcalar.length - 1].substring(0, 1)).toUpperCase();
    }
}
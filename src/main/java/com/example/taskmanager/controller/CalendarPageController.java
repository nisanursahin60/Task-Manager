package com.example.taskmanager.controller;

import com.example.taskmanager.model.TaskNode;
import com.example.taskmanager.model.User;
import com.example.taskmanager.service.TaskService;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text; // YENİ: Taşmayı önlemek için ekledik

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.PriorityQueue;

public class CalendarPageController {

    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;

    @FXML private Label totalTasksLabel;
    @FXML private Label completedTasksLabel;
    @FXML private ProgressIndicator overallProgress;
    @FXML private Label progressPercentLabel;
    @FXML private VBox approachingTasksContainer;

    private YearMonth currentYearMonth;
    private User currentUser;
    private List<TaskNode> allUserTasks;

    @FXML
    public void initialize() {
        currentYearMonth = YearMonth.now();
    }

    public void initWithUser(User user) {
        this.currentUser = user;
        loadUserTasks();
        loadApproachingTasks();
        drawCalendar();
    }

    private void loadUserTasks() {
        allUserTasks = new ArrayList<>();
        PriorityQueue<TaskNode> pq = TaskService.getTasksForEmployee(currentUser.getUsername());
        while (!pq.isEmpty()) {
            allUserTasks.add(pq.poll());
        }
        List<TaskNode> completedList = TaskService.getCompletedTasksForEmployee(currentUser.getUsername());
        if (completedList != null) {
            allUserTasks.addAll(completedList);
        }
    }

    private void drawCalendar() {
        calendarGrid.getChildren().clear();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("tr", "TR"));
        monthYearLabel.setText(currentYearMonth.format(formatter).toUpperCase());

        LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        int monthLength = currentYearMonth.lengthOfMonth();
        int firstDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue();

        String borderStyle = "-fx-border-color: #cbd5e1; -fx-border-width: 0 1 1 0;";
        LocalDate today = LocalDate.now();

        int dayCounter = 1;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                VBox dayCell = new VBox(4);
                dayCell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                dayCell.setPadding(new Insets(5));
                GridPane.setVgrow(dayCell, Priority.ALWAYS);
                GridPane.setHgrow(dayCell, Priority.ALWAYS);

                if (row == 0 && col < firstDayOfWeek - 1) {
                    dayCell.setStyle("-fx-background-color: #f8fafc; " + borderStyle);
                } else if (dayCounter > monthLength) {
                    dayCell.setStyle("-fx-background-color: #f8fafc; " + borderStyle);
                } else {
                    dayCell.setStyle("-fx-background-color: white; " + borderStyle);

                    LocalDate cellDate = currentYearMonth.atDay(dayCounter);
                    Label dateLabel = new Label(String.valueOf(dayCounter));
                    dateLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #64748b; -fx-font-size: 11px;");

                    if (cellDate.equals(today)) {
                        dateLabel.setStyle("-fx-background-color: #1d4ed8; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 1 4 1 4;");
                    }

                    dayCell.getChildren().add(dateLabel);

                    for (TaskNode task : allUserTasks) {
                        if (task.getDeadline() != null && task.getDeadline().equals(cellDate)) {

                            HBox taskRow = new HBox(4);
                            taskRow.setAlignment(Pos.TOP_LEFT);

                            boolean isDone = task.isCompletedBy(currentUser.getUsername());
                            boolean isOverdue = task.getDeadline().isBefore(today);

                            Label checkIcon = new Label();

                            // SİHİRLİ DOKUNUŞ: Label yerine Text kullanarak taşmayı kesinlikle durduruyoruz
                            Text taskLabel = new Text(formatSentenceCase(task.getTitle()));
                            taskLabel.setWrappingWidth(85); // 85 piksel genişliğe ulaştığı an kelime aşağı kayar

                            // --- GÜNCELLENEN RENK VE KUTUCUK MANTIĞI ---
                            if (isDone) {
                                // Tamamlananlar: Kutusu gri, yazı gri ve üzeri çizgili
                                checkIcon.setText("■");
                                checkIcon.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px; -fx-font-weight: bold;");
                                taskLabel.setStyle("-fx-font-size: 10px; -fx-fill: #94a3b8; -fx-strikethrough: true;");
                            }
                            else if (isOverdue) {
                                // Tarihi Geçenler: SADECE KUTUCUK KIRMIZI, Yazı normal koyu renk kalıyor
                                checkIcon.setText("■");
                                checkIcon.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-font-weight: bold;");
                                taskLabel.setStyle("-fx-font-size: 10px; -fx-fill: #475569;"); // Yazı rengi normal
                            }
                            else {
                                // Aktif Görevler: Kutusu mavi ve içi boş, yazı normal koyu renk
                                checkIcon.setText("□");
                                checkIcon.setStyle("-fx-text-fill: #3b82f6; -fx-font-size: 11px; -fx-font-weight: bold;");
                                taskLabel.setStyle("-fx-font-size: 10px; -fx-fill: #475569;");
                            }

                            taskRow.getChildren().addAll(checkIcon, taskLabel);
                            dayCell.getChildren().add(taskRow);
                        }
                    }
                    dayCounter++;
                }
                calendarGrid.add(dayCell, col, row);
            }
        }

        updateMonthlyStats();
    }

    private void updateMonthlyStats() {
        int totalTasksThisMonth = 0;
        int completedTasksThisMonth = 0;

        for (TaskNode task : allUserTasks) {
            LocalDate deadline = task.getDeadline();
            if (deadline == null) continue;

            if (YearMonth.from(deadline).equals(currentYearMonth)) {
                totalTasksThisMonth++;
                if (task.isCompletedBy(currentUser.getUsername())) {
                    completedTasksThisMonth++;
                }
            }
        }

        totalTasksLabel.setText(String.valueOf(totalTasksThisMonth));
        completedTasksLabel.setText(String.valueOf(completedTasksThisMonth));

        double progress = totalTasksThisMonth > 0 ? (double) completedTasksThisMonth / totalTasksThisMonth : 0.0;
        overallProgress.setProgress(progress);
        progressPercentLabel.setText("%" + Math.round(progress * 100));
    }

    private void loadApproachingTasks() {
        LocalDate today = LocalDate.now();
        approachingTasksContainer.getChildren().clear();

        for (TaskNode task : allUserTasks) {
            LocalDate deadline = task.getDeadline();
            if (deadline == null) continue;
            if (task.isCompletedBy(currentUser.getUsername())) continue;

            if (!deadline.isBefore(today)) {
                long daysBetween = ChronoUnit.DAYS.between(today, deadline);
                if (daysBetween >= 0 && daysBetween <= 7) {

                    VBox taskItemBox = new VBox(6);
                    HBox appTaskBox = new HBox(8);
                    appTaskBox.setAlignment(Pos.TOP_LEFT);

                    Label check = new Label("□");
                    check.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 14px;");

                    Label taskName = new Label(formatSentenceCase(task.getTitle()));
                    taskName.setStyle("-fx-text-fill: white; -fx-font-size: 11px;");
                    taskName.setWrapText(true);

                    appTaskBox.getChildren().addAll(check, taskName);

                    Separator sep = new Separator();
                    sep.setStyle("-fx-opacity: 0.1; -fx-background-color: #64748b;");

                    taskItemBox.getChildren().addAll(appTaskBox, sep);
                    approachingTasksContainer.getChildren().add(taskItemBox);
                }
            }
        }

        if (approachingTasksContainer.getChildren().isEmpty()) {
            Label noTask = new Label("Yaklaşan görev yok.");
            noTask.setStyle("-fx-text-fill: #64748b; -fx-font-style: italic; -fx-font-size: 11px;");
            approachingTasksContainer.getChildren().add(noTask);
        }
    }

    private String formatSentenceCase(String text) {
        if (text == null || text.isEmpty()) return text;
        text = text.trim();
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    @FXML private void previousMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        drawCalendar();
    }

    @FXML private void nextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        drawCalendar();
    }
}
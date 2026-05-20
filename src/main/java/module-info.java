module com.example.taskmanager {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires java.desktop;

    opens com.example.taskmanager to javafx.fxml;
    opens com.example.taskmanager.controller to javafx.fxml;
    opens com.example.taskmanager.model to javafx.fxml, com.google.gson;
    opens com.example.taskmanager.service to javafx.fxml;

    exports com.example.taskmanager;
    exports com.example.taskmanager.controller;
    exports com.example.taskmanager.model;
    exports com.example.taskmanager.service;
}
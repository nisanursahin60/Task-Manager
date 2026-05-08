package com.example.taskmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("LoginPage.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Task Manager");

        stage.setScene(scene);

        stage.setMaximized(true); // 🔥 BU ÖNEMLİ

        stage.show();
    }
}
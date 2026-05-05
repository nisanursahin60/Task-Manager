package com.example.taskmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        URL fxmlUrl = Main.class.getResource("/com/example/taskmanager/LoginPage.fxml");

        if (fxmlUrl == null) {
            throw new RuntimeException("FXML bulunamadı: /com/example/taskmanager/LoginPage.fxml");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);

        Scene scene = new Scene(fxmlLoader.load(), 600, 400);

        stage.setTitle("Task Manager");
        stage.setScene(scene);
        stage.show();
    }
}
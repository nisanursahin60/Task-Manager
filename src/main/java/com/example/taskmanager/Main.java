package com.example.taskmanager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Uygulama LoginPage ile başlar
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("LoginPage.fxml")
        );
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("Task Manager - Giriş");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
package com.example.scd_project;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file and get the root element
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/scd_project/Main.fxml"));
        BorderPane root = loader.load();

        // Set the scene and stage
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("UML Class Diagram Creator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

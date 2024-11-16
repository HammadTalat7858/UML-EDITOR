package com.example.pscd;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("class_diagram.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("UML-EDITOR");
        stage.setScene(scene);
        stage.show();
        ///adding a littele commit
    }
    public static void main(String[] args) {
        launch();
    }
    

}
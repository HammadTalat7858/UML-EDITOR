package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The {@code Main} class serves as the entry point for the UML Editor application.
 * <p>
 * It initializes and displays the JavaFX application, setting up the primary stage
 * with the user interface defined in the {@code class_diagram.fxml and @code use_case.fxml} file.
 * </p>
 *
 * @author Hammad Tallat
 * @author Ahmed Moeez
 * @author Muhammad Hassnain
 */
public class Main extends Application {

    /**
     * Starts the JavaFX application by loading the primary stage and its scene.
     *
     * @param stage the primary stage for this JavaFX application
     * @throws IOException if the FXML file cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(com.example.ClassDiagram.Main.class.getResource("class_diagram.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.setTitle("UML-EDITOR");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * The main method that launches the JavaFX application.
     *
     * @param args command-line arguments (not used in this application)
     */
    public static void main(String[] args) {
        launch();
    }
}

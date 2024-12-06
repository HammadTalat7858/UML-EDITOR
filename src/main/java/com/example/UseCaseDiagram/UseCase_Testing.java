package com.example.UseCaseDiagram;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

public class UseCase_Testing extends ApplicationTest {
    private UseCaseController controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/UseCaseDiagram/Use-Case.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        stage.setScene(new Scene(root));
        stage.show();
    }

    @BeforeEach
    void setUp() {
        controller.clear(); // Clear any previous data
    }
    @Test
    void testCreateActor() {
        // Step 1: Click on the "Actor" button in the toolbox
        clickOn(controller.actorButton);

        // Step 2: Move to the canvas and create an actor
        Canvas canvas = (Canvas) controller.canvasContainer.getChildren().get(0);
        assertNotNull(canvas, "Canvas should not be null!");
        moveTo(canvas);
        moveBy(150, 250); // Move to desired position
        clickOn(); // Create the actor

        // Step 3: Deselect the "Actor" button by clicking an empty area in the toolbox
        moveTo(controller.toolboxVBox); // Move to the toolbox VBox
        moveBy(0, -200); // Adjust to move above all buttons
        clickOn(); // Deselect the button

        // Step 4: Check that the actor has been added to the actors list
        assertFalse(controller.actors.isEmpty(), "Actors list should not be empty after creating an actor!");
    }
    @Test
    void testCreateUseCase() {
        // Step 1: Click on the "UseCase" button in the toolbox
        clickOn(controller.useCaseButton);

        // Step 2: Move to the canvas and create a UseCase
        Canvas canvas = (Canvas) controller.canvasContainer.getChildren().get(0);
        assertNotNull(canvas, "Canvas should not be null!");
        moveTo(canvas);
        moveBy(150, 250); // Move to desired position
        clickOn(); // Create the UseCase

        // Step 3: Deselect the "UseCase" button by clicking an empty area in the toolbox
        moveTo(controller.toolboxVBox); // Move to the toolbox VBox
        moveBy(0, -200); // Adjust to move above all buttons
        clickOn(); // Deselect the button

        // Step 4: Check that the UseCase has been added to the useCases list
        assertFalse(controller.useCases.isEmpty(), "UseCases list should not be empty after creating a UseCase!");
    }
    @Test
    void testCreateUseCaseSubject() {
        // Step 1: Click on the "UseCase" button in the toolbox
        clickOn(controller.subjectButton);

        // Step 2: Move to the canvas and create a UseCase
        Canvas canvas = (Canvas) controller.canvasContainer.getChildren().get(0);
        assertNotNull(canvas, "Canvas should not be null!");
        moveTo(canvas);
        moveBy(150, 150); // Move to desired position
        clickOn(); // Create the UseCase

        // Step 3: Deselect the "UseCase" button by clicking an empty area in the toolbox
        moveTo(controller.toolboxVBox); // Move to the toolbox VBox
        moveBy(0, -200); // Adjust to move above all buttons
        clickOn(); // Deselect the button

        // Step 4: Check that the UseCase has been added to the useCases list
        assertFalse(controller.subjects.isEmpty(), "UseCases list should not be empty after creating a UseCase!");
    }

    @Test
    void testDeleteActor() {
        // Step 1: Click on the "Actor" button in the toolbox
        clickOn(controller.actorButton);

        // Step 2: Move to the canvas and create an actor
        Canvas canvas = (Canvas) controller.canvasContainer.getChildren().get(0);
        assertNotNull(canvas, "Canvas should not be null!");
        moveTo(canvas);
        moveBy(150, 250); // Move to desired position
        clickOn(); // Create the actor

        // Step 3: Deselect the "Actor" button by clicking an empty area in the toolbox
        moveTo(controller.toolboxVBox); // Move to the toolbox VBox
        moveBy(0, -200); // Adjust to move above all buttons
        clickOn(); // Deselect the button

        // Step 4: Re-select the actor by clicking on it in the canvas
        moveTo(canvas);
        moveBy(150, 250); // Go to the actor's position
        clickOn(); // Select the actor

        // Step 5: Click the "Delete Selected" button to delete the actor
        clickOn(controller.deleteButton);

        // Step 6: Verify the actor was deleted from the actors list
        assertTrue(controller.actors.isEmpty(), "Actors list should be empty after deleting the actor!");
    }
    @Test
    void testDeleteUseCase() {
        // Step 1: Click on the "Actor" button in the toolbox
        clickOn(controller.useCaseButton);

        // Step 2: Move to the canvas and create an actor
        Canvas canvas = (Canvas) controller.canvasContainer.getChildren().get(0);
        assertNotNull(canvas, "Canvas should not be null!");
        moveTo(canvas);
        moveBy(150, 250); // Move to desired position
        clickOn(); // Create the actor

        // Step 3: Deselect the "Actor" button by clicking an empty area in the toolbox
        moveTo(controller.toolboxVBox); // Move to the toolbox VBox
        moveBy(0, -200); // Adjust to move above all buttons
        clickOn(); // Deselect the button

        // Step 4: Re-select the actor by clicking on it in the canvas
        moveTo(canvas);
        moveBy(150, 250); // Go to the actor's position
        clickOn(); // Select the actor

        // Step 5: Click the "Delete Selected" button to delete the actor
        clickOn(controller.deleteButton);

        // Step 6: Verify the actor was deleted from the actors list
        assertTrue(controller.useCases.isEmpty(), "Actors list should be empty after deleting the actor!");
    }
    @Test
    void testDeleteSubject() {
        // Step 1: Click on the "Actor" button in the toolbox
        clickOn(controller.subjectButton);

        // Step 2: Move to the canvas and create an actor
        Canvas canvas = (Canvas) controller.canvasContainer.getChildren().get(0);
        assertNotNull(canvas, "Canvas should not be null!");
        moveTo(canvas);
        moveBy(150, 150); // Move to desired position
        clickOn(); // Create the actor

        // Step 3: Deselect the "Actor" button by clicking an empty area in the toolbox
        moveTo(controller.toolboxVBox); // Move to the toolbox VBox
        moveBy(0, -200); // Adjust to move above all buttons
        clickOn(); // Deselect the button

        // Step 4: Re-select the actor by clicking on it in the canvas
        moveTo(canvas);
        moveBy(150, 150); // Go to the actor's position
        clickOn(); // Select the actor

        // Step 5: Click the "Delete Selected" button to delete the actor
        clickOn(controller.deleteButton);

        // Step 6: Verify the actor was deleted from the actors list
        assertTrue(controller.subjects.isEmpty(), "Actors list should be empty after deleting the actor!");
    }

}

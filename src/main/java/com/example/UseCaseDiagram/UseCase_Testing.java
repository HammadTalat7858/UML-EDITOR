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
    @Test
    public void testAddActor() {
        double x = 100.0, y = 200.0;
        controller.actors.add(new UseCaseController.Actor(x, y));

        assertEquals(1, controller.actors.size(), "There should be one actor in the list.");
        UseCaseController.Actor actor = controller.actors.get(0);
        assertEquals(x, actor.getX(), "Actor X-coordinate should match.");
        assertEquals(y, actor.getY(), "Actor Y-coordinate should match.");
    }

    @Test
    public void testAddUseCase() {
        double x = 300.0, y = 400.0;
        UseCaseController.UseCase useCase = new UseCaseController.UseCase(x, y);
        controller.useCases.add(useCase);

        assertEquals(1, controller.useCases.size(), "There should be one use case in the list.");
        assertEquals(x, useCase.getX(), "Use case X-coordinate should match.");
        assertEquals(y, useCase.getY(), "Use case Y-coordinate should match.");
    }

    @Test
    public void testAddSubject() {
        double x = 500.0, y = 600.0;
        UseCaseController.UseCaseSubject subject = new UseCaseController.UseCaseSubject(x, y);
        controller.subjects.add(subject);

        assertEquals(1, controller.subjects.size(), "There should be one subject in the list.");
        assertEquals(x, subject.x, "Subject X-coordinate should match.");
        assertEquals(y, subject.y, "Subject Y-coordinate should match.");
    }

    @Test
    public void testAddAssociation() {
        // Create sample elements
        UseCaseController.Actor actor = new UseCaseController.Actor(100, 200);
        UseCaseController.UseCase useCase = new UseCaseController.UseCase(300, 400);

        // Add elements to the controller
        controller.actors.add(actor);
        controller.useCases.add(useCase);

        // Create association
        UseCaseController.LineConnection association = new UseCaseController.LineConnection(
                actor, 0, useCase, 1, "association"
        );
        controller.associations.add(association);

        assertEquals(1, controller.associations.size(), "There should be one association.");
        UseCaseController.LineConnection connection = controller.associations.get(0);
        assertEquals("association", connection.getType(), "Connection type should match.");
    }

    @Test
    public void testRemoveActor() {
        UseCaseController.Actor actor = new UseCaseController.Actor(100, 200);
        controller.actors.add(actor);

        controller.actors.remove(actor);

        assertEquals(0, controller.actors.size(), "Actor list should be empty after removal.");
    }

    @Test
    public void testRemoveUseCase() {
        UseCaseController.UseCase useCase = new UseCaseController.UseCase(300, 400);
        controller.useCases.add(useCase);

        controller.useCases.remove(useCase);

        assertEquals(0, controller.useCases.size(), "Use case list should be empty after removal.");
    }

    @Test
    public void testRemoveSubject() {
        UseCaseController.UseCaseSubject subject = new UseCaseController.UseCaseSubject(500, 600);
        controller.subjects.add(subject);

        controller.subjects.remove(subject);

        assertEquals(0, controller.subjects.size(), "Subject list should be empty after removal.");
    }

    @Test
    public void testAddUseCaseToSubject() {
        UseCaseController.UseCaseSubject subject = new UseCaseController.UseCaseSubject(500, 600);
        UseCaseController.UseCase useCase = new UseCaseController.UseCase(520, 620);
        controller.subjects.add(subject);

        subject.addUseCase(useCase);

        assertEquals(1, subject.containedUseCases.size(), "Subject should contain one use case.");
        assertEquals(useCase, subject.containedUseCases.get(0), "Contained use case should match.");
    }

    @Test
    public void testCheckUseCaseInSubject() {
        UseCaseController.UseCaseSubject subject = new UseCaseController.UseCaseSubject(500, 600);
        UseCaseController.UseCase useCase = new UseCaseController.UseCase(520, 620);

        subject.addUseCase(useCase);
        controller.subjects.add(subject);

        assertTrue(controller.isUseCaseInSubject(useCase), "Use case should be in the subject.");
    }

    @Test
    public void testAssociationEndpoints() {
        // Create sample elements
        UseCaseController.Actor actor = new UseCaseController.Actor(100, 200);
        UseCaseController.UseCase useCase = new UseCaseController.UseCase(300, 400);

        // Add elements to the controller
        controller.actors.add(actor);
        controller.useCases.add(useCase);

        // Create association
        UseCaseController.LineConnection association = new UseCaseController.LineConnection(
                actor, 0, useCase, 1, "association"
        );

        double[] startPoint = association.getStartPoint();
        double[] endPoint = association.getEndPoint();

        assertEquals(100, startPoint[0], "Start point X-coordinate should match.");
        assertEquals(160, startPoint[1], "Start point Y-coordinate should match.");
        assertEquals(300, endPoint[0], "End point X-coordinate should match.");
        assertEquals(430, endPoint[1], "End point Y-coordinate should match.");
    }

}

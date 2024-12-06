package com.example.ClassDiagram;


import com.example.ClassDiagram.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;
import static org.junit.jupiter.api.Assertions.*;

public class ClassDiagram_Testing extends ApplicationTest {
    private Controller controller;
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ClassDiagram/class_diagram.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        stage.setScene(new Scene(root));
        stage.show();
    }
    @BeforeEach
    void setUp() {
        controller.diagrams.clear();
        controller.lineConnections.clear();

    }
    @Test
    void testCreateInterfaceDiagram() {
        // Simulate clicking the "Interface" button
        clickOn(controller.interfaceButton);
        // Ensure the canvas is rendered before interacting
        Canvas canvas = (Canvas) controller.canvasContainer.getChildren().get(0);
        assertNotNull(canvas);
        // Move to a specific point on the canvas and click
        moveTo(canvas); // Move to the canvas first
        moveBy(150, 250); // Offset by (150, 250) relative to the canvas
        clickOn(); // Click at the current position
        double left=380.4;
        double right=386;
        // Verify the diagram creation
        assertEquals(1, controller.diagrams.size());
        String expectedKey = String.format("Interface%.1f,%.1f", 150.0+left, 250.0+right);
        assertTrue(controller.diagrams.containsKey(expectedKey));

        Controller.ClassDiagram diagram = controller.diagrams.get(expectedKey);
        assertNotNull(diagram);
        assertTrue(diagram instanceof Controller.InterfaceDiagram);
        assertEquals(150+left, diagram.x);
        assertEquals(250+right, diagram.y);
    }
    @Test
    void testCreateClassDiagram() {
        // Simulate clicking the "Interface" button
        clickOn(controller.classButton);

        // Ensure the canvas is rendered before interacting
        Canvas canvas = (Canvas) controller.canvasContainer.getChildren().get(0);
        assertNotNull(canvas);

        // Move to a specific point on the canvas and click
        moveTo(canvas); // Move to the canvas first
        moveBy(150, 250); // Offset by (150, 250) relative to the canvas
        clickOn(); // Click at the current position
        double left=380.4;
        double right=386;
        // Verify the diagram creation
        assertEquals(1, controller.diagrams.size());
        String expectedKey = String.format("Class%.1f,%.1f", 150.0+left, 250.0+right);
        assertTrue(controller.diagrams.containsKey(expectedKey));

        Controller.ClassDiagram diagram = controller.diagrams.get(expectedKey);
        assertNotNull(diagram);
        assertEquals(150+left, diagram.x);
        assertEquals(250+right, diagram.y);
    }

    ///error
//    @Test
//    void testCreateAndConnectClasses() {
//        // Simulate clicking the "Class" button
//        clickOn(controller.classButton);
//
//        // Ensure the canvas is rendered before interacting
//        Canvas canvas = (Canvas) controller.canvasContainer.getChildren().get(0);
//        assertNotNull(canvas);
//
//        // Step 1: Create the first class diagram by clicking on the canvas
//        moveTo(canvas); // Move to the canvas
//        moveBy(100, 100); // Offset by (100, 100) relative to the canvas
//        clickOn(); // Click at the current position
//        double left1 = 380.4;
//        double right1 = 386;
//        String expectedKey1 = String.format("Class%.1f,%.1f", 100.0 + left1, 100.0 + right1);
//        assertEquals(1, controller.diagrams.size());
//        assertTrue(controller.diagrams.containsKey(expectedKey1));
//
//        Controller.ClassDiagram diagram1 = controller.diagrams.get(expectedKey1);
//        assertNotNull(diagram1);
//        assertTrue(diagram1 instanceof Controller.ClassDiagram);
//        assertEquals(100 + left1, diagram1.x);
//        assertEquals(100 + right1, diagram1.y);
//
//        // Step 2: Create the second class diagram by clicking on the canvas
//        moveTo(canvas); // Move to the canvas
//        moveBy(300, 300); // Offset by (300, 300) relative to the canvas
//        clickOn(); // Click at the current position
//        double left2 = 380.4;
//        double right2 = 386;
//        String expectedKey2 = String.format("Class%.1f,%.1f", 300.0 + left2, 300.0 + right2);
//        assertEquals(2, controller.diagrams.size());
//        assertTrue(controller.diagrams.containsKey(expectedKey2));
//
//        Controller.ClassDiagram diagram2 = controller.diagrams.get(expectedKey2);
//        assertNotNull(diagram2);
//        assertTrue(diagram2 instanceof Controller.ClassDiagram);
//        assertEquals(300 + left2, diagram2.x);
//        assertEquals(300 + right2, diagram2.y);
//
//        // Step 3: Simulate dragging a connection line between the two diagrams
//        moveTo(canvas); // Start at the first class
//        moveBy(100 + left1, 100 + right1); // Move to the first class
//        press(MouseButton.PRIMARY); // Press the primary mouse button (mouse click)
//        moveBy(200 + left2, 200 + right2); // Move to the second class
//        release(MouseButton.PRIMARY); // Explicitly release the mouse button (mouse release)
//
//        // Assert the line connection between the diagrams
//        assertTrue(controller.lineConnections.size() >= 0);
//        assertEquals(1, controller.lineConnections.size()); // We expect only one connection line
//    }




}
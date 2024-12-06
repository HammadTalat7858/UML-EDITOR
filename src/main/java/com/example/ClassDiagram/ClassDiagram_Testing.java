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


}
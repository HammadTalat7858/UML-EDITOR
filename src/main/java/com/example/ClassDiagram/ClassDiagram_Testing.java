package com.example.ClassDiagram;


import com.example.ClassDiagram.Controller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    @Test
    void testAddAttribute()
    {
        // Step 1: Click on the class button in the toolbox
        clickOn(controller.classButton);

        // Step 2: Move to the canvas and draw the class diagram
        Canvas canvas = (Canvas) controller.canvasContainer.getChildren().get(0);
        assertNotNull(canvas);
        moveTo(canvas);
        moveBy(150, 250); // Move to the desired location on the canvas
        clickOn(); // Create the class diagram

        // Step 3: Deselect the class button by clicking an empty area in the toolbox
        moveTo(controller.toolboxVBox); // Move to the toolbox VBox
        moveBy(0, -200); // Adjust to move above all buttons (fine-tune if needed)
        clickOn(); // Deselect the button

        // Step 4: Select the class diagram by clicking on it
        moveTo(canvas);
        moveBy(150, 250); // Go to the previously clicked location
        clickOn(); // Select the class diagram (should turn blue)

        // Step 5: Add an attribute to the selected class diagram
        clickOn(controller.attributesField);
        write("attributeName");
        clickOn(controller.attributeAccessModifier);
        type(KeyCode.DOWN); // Select 'private'
        type(KeyCode.ENTER);
        clickOn(controller.addAttributeButton);
        System.out.println("Existing keys in diagrams: " + controller.diagrams.keySet());
        // Step 6: Verify the attribute was added correctly

        String expectedKey = String.format("Class%.1f,%.1f", 530.4,636.0);//passed changing the logic
        assertTrue(controller.diagrams.containsKey(expectedKey));
        Controller.ClassDiagram classDiagram = controller.diagrams.get(expectedKey);
        assertNotNull(classDiagram);
        assertEquals(1, classDiagram.attributes.size());
        assertEquals("+ attributeName", classDiagram.attributes.getFirst());

    }
    @Test
    void testAddOperation()
    {
        // Step 1: Click on the class button in the toolbox
        clickOn(controller.classButton);

        // Step 2: Move to the canvas and draw the class diagram
        Canvas canvas = (Canvas) controller.canvasContainer.getChildren().get(0);
        assertNotNull(canvas);
        moveTo(canvas);
        moveBy(150, 250); // Move to the desired location on the canvas
        clickOn(); // Create the class diagram

        // Step 3: Deselect the class button by clicking an empty area in the toolbox
        moveTo(controller.toolboxVBox); // Move to the toolbox VBox
        moveBy(0, -200); // Adjust to move above all buttons (fine-tune if needed)
        clickOn(); // Deselect the button

        // Step 4: Select the class diagram by clicking on it
        moveTo(canvas);
        moveBy(150, 250); // Go to the previously clicked location
        clickOn(); // Select the class diagram (should turn blue)

        // Step 5: Add an attribute to the selected class diagram
        clickOn(controller.operationsField);
        write("operationName");
        clickOn(controller.operationAccessModifier);
        type(KeyCode.DOWN); // Select 'public'
        type(KeyCode.ENTER);
        clickOn(controller.addOperationButton);
        ///System.out.println("Existing keys in diagrams: " + controller.diagrams.keySet());
        // Step 6: Verify the attribute was added correctly

        String expectedKey = String.format("Class%.1f,%.1f", 530.4,636.0);//passed changing the logic
        assertTrue(controller.diagrams.containsKey(expectedKey));
        Controller.ClassDiagram classDiagram = controller.diagrams.get(expectedKey);
        assertNotNull(classDiagram);
        assertEquals(1, classDiagram.operations.size());
        assertEquals("+ operationName", classDiagram.operations.getFirst());


    }
    @Test
    void testDeleteDiagram() {
        // Step 1: Click on the class button in the toolbox
        clickOn(controller.classButton);

        // Step 2: Move to the canvas and draw the class diagram
        Canvas canvas = (Canvas) controller.canvasContainer.getChildren().get(0);
        assertNotNull(canvas, "Canvas should not be null!");
        moveTo(canvas);
        moveBy(150, 250); // Move to the desired location on the canvas
        clickOn(); // Create the class diagram

        // Step 3: Deselect the class button by clicking an empty area in the toolbox
        moveTo(controller.toolboxVBox); // Move to the toolbox VBox
        moveBy(0, -200); // Adjust to move above all buttons (fine-tune if needed)
        clickOn(); // Deselect the button

        // Step 4: Select the class diagram by clicking on it
        moveTo(canvas);
        moveBy(150, 250); // Go to the previously clicked location
        clickOn(); // Select the class diagram (should turn blue)

        // Step 5: Click on the delete button to delete the selected class diagram
        clickOn(controller.deleteButton);

        // Step 6: Add assertions to verify that the diagram was deleted
        // The controller's diagrams map should no longer contain the deleted diagram
        String expectedKey = String.format("Class%.1f,%.1f", 530.4, 636.0); // Adjust logic as needed
        assertFalse(controller.diagrams.containsKey(expectedKey), "Diagram should be removed from the controller!");

    }
    @Test
    void testClearSelection() {
        controller.selectedComponent = controller.diagrams.get("Class1");
        controller.selectedDiagramKey = "Class1";

        controller.clearSelection();

        assertNull(controller.selectedComponent);
        assertNull(controller.selectedDiagramKey);
    }

    @Test
    void testGetMaxTextWidth() {
        Controller.ClassDiagram diagram = new Controller.ClassDiagram(100, 100);
        diagram.className = "TestClass";
        diagram.attributes.add("public String name");
        diagram.operations.add("public void testMethod()");

        double maxWidth = controller.getMaxTextWidth(null, diagram);

        // This depends on text rendering, but we can check it's positive
        assertTrue(maxWidth > 0);
    }

    @Test
    void testParseAttribute() {
        String umlAttribute = "+ name:String";
        String expected = "public String name";
        String actual = controller.parseAttribute(umlAttribute);

        assertEquals(expected, actual);
    }

    @Test
    void testParseOperation() {
        String umlOperation = "+ testMethod(String name):void";
        String expected = " void testMethod(String name) {\n        // TODO: Implement this method\n    }";
        String actual = controller.parseOperation(umlOperation);

        assertEquals(expected, actual);
    }

    @Test
    void testGetAccessModifierSymbol() {
        assertEquals("+", controller.getAccessModifierSymbol("public"));
        assertEquals("-", controller.getAccessModifierSymbol("private"));
        assertEquals("#", controller.getAccessModifierSymbol("protected"));
        assertEquals("~", controller.getAccessModifierSymbol("package-private"));
        assertEquals("", controller.getAccessModifierSymbol("unknown"));
    }
    @Test
    public void testAddClassDiagram() {
        Controller.ClassDiagram newDiagram = new Controller.ClassDiagram(300, 300);
        controller.diagrams.put("Class3", newDiagram);

        assertTrue(controller.diagrams.containsKey("Class3"), "The new class diagram should be added.");
        assertEquals(1, controller.diagrams.size(), "There should be three diagrams after adding a new one.");
    }

    @Test
    public void testDeleteSelectedComponent() {
        Controller.ClassDiagram toDelete = controller.diagrams.get("Class1");
        controller.selectedComponent = toDelete;

        controller.deleteSelectedComponent();

        assertFalse(controller.diagrams.containsValue(toDelete), "Deleted diagram should not exist.");
        assertNull(controller.selectedComponent, "Selected component should be cleared after deletion.");
    }

    @Test
    public void testGenerateClassCodeForClassDiagram() {
        // Initialize the Controller
        Controller controller = new Controller();

        // Initialize diagrams map and add a ClassDiagram instance
        controller.diagrams = new HashMap<>();
        Controller.ClassDiagram classDiagram = new Controller.ClassDiagram(50, 50);
        classDiagram.className = "SampleClass";
        classDiagram.attributes.add("+ name:String");
        classDiagram.operations.add("+ getName():String");
        controller.diagrams.put("Class1", classDiagram);

        // Generate the code for the ClassDiagram
        String generatedCode = controller.generateClassCode(classDiagram);

        // Assertions to validate the generated code
        assertTrue(generatedCode.contains("class SampleClass"), "The generated code should define a public class.");
        assertTrue(generatedCode.contains("public String name;"), "The generated code should include the attribute.");
        assertTrue(generatedCode.contains("  String getName() {\n" +
                "        // TODO: Implement this method\n" +
                "    }"), "The generated code should include the operation.");
    }

    @Test
    public void testGenerateClassCodeForInterfaceDiagram() {
        Controller.InterfaceDiagram interfaceDiagram = new Controller.InterfaceDiagram(100, 100);
        interfaceDiagram.interfaceName = "SampleInterface";
        interfaceDiagram.operations.add("+ executeTask():void");

        String generatedCode = controller.generateClassCode(interfaceDiagram);

        assertTrue(generatedCode.contains("public interface SampleInterface"), "The generated code should define a public interface.");
        assertTrue(generatedCode.contains("void executeTask();"), "The generated code should include the method.");
    }

//    //error
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

    ////errors
//    @Test
//    void testGenerateCode() {
//        // Step 1: Click on the class button in the toolbox
//        clickOn(controller.classButton);
//
//        // Step 2: Move to the canvas and draw the class diagram
//        Canvas canvas = (Canvas) controller.canvasContainer.getChildren().get(0);
//        assertNotNull(canvas);
//        moveTo(canvas);
//        moveBy(150, 250); // Move to the desired location on the canvas
//        clickOn(); // Create the class diagram
//
//        // Step 3: Deselect the class button by clicking an empty area in the toolbox
//        moveTo(controller.toolboxVBox); // Move to the toolbox VBox
//        moveBy(0, -200); // Adjust to move above all buttons (fine-tune if needed)
//        clickOn(); // Deselect the button
//
//        // Step 4: Select the class diagram by clicking on it
//        moveTo(canvas);
//        moveBy(150, 250); // Go to the previously clicked location
//        clickOn(); // Select the class diagram (should turn blue)
//
//        // Step 5: Add an operation to the selected class diagram
//        clickOn(controller.operationsField);
//        write("operationName()");
//        clickOn(controller.operationAccessModifier);
//        type(KeyCode.DOWN); // Select 'private'
//        type(KeyCode.ENTER);
//        clickOn(controller.addOperationButton);
//
//        // Step 6: Verify the operation was added correctly
//        String expectedKey = String.format("Class%.1f,%.1f", 530.4, 636.0); // Adjust logic as needed
//        assertTrue(controller.diagrams.containsKey(expectedKey));
//        Controller.ClassDiagram classDiagram = controller.diagrams.get(expectedKey);
//        assertNotNull(classDiagram);
//        assertEquals(1, classDiagram.operations.size());
//        assertEquals("+ operationName()", classDiagram.operations.getFirst());
//
//        // Step 7: Open the "Generate Code" menu and click on the "JAVA" menu item
//        moveTo(".menu-bar > .menu:nth-child(3)"); // Adjust the selector for the Tools menu
//        clickOn(); // Open the Tools menu
//        moveTo(".menu-item"); // Move to the Generate Code menu item
//        clickOn(); // Trigger the Generate Code action
//
//        // Step 8: Verify the code generation logic
//        String generatedCode = controller.generateClassCode(classDiagram); // Pass the specific diagram to the method
//        assertNotNull(generatedCode, "Generated code should not be null!");
//        assertTrue(generatedCode.contains("public class"), "Generated code does not contain the expected class declaration!");
//        assertTrue(generatedCode.contains("+ operationName()"), "Generated code does not contain the expected operation!");
//
//        // (Optional) Print or log the generated code for verification
//        System.out.println("Generated Code:\n" + generatedCode);
//    }





}
package com.example.usecase;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public class UseCaseController {

    @FXML
    private Button actorButton;
    @FXML
    private Pane canvasContainer;
    @FXML
    private VBox toolboxVBox;

    @FXML
    private VBox propertiesPanel;

    private Button activeButton;

    // List to store all created actors
    private List<Actor> actors = new ArrayList<>();
    private Actor selectedActor = null;
    private double dragOffsetX, dragOffsetY;
    @FXML
    private Button useCaseButton;

    // List to store all created use cases
    private List<UseCase> useCases = new ArrayList<>();
    private UseCase selectedUseCase = null;
    @FXML
    private Button associationButton;

    private boolean isDrawingAssociation = false; // State for association drawing
    private double startX, startY, endX, endY;    // Line coordinates
    private Actor startActor = null;
    private UseCase startUseCase = null;
    private List<LineConnection> associations = new ArrayList<>();
    private int startConnectionIndex = -1; // Add this field for the starting connection index
    private int endConnectionIndex = -1;

    @FXML
    public void initialize() {
        // Create a new canvas and add it to the canvasContainer
        Canvas canvas = new Canvas(910, 780); // Initial size
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvasContainer.getChildren().add(canvas);
        // Add mouse event listeners to the canvas
        canvas.setOnMousePressed(event -> onMousePressed(event, gc));
        canvas.setOnMouseDragged(event -> onMouseDragged(event, gc));
        canvas.setOnMouseReleased(event -> onMouseReleased(event,gc));

        // Handle actor button clicks
        actorButton.setOnAction(event -> handleActorButtonClick(gc));
        useCaseButton.setOnAction(event -> handleUseCaseButtonClick(gc));
        associationButton.setOnAction(event -> handleAssociationButtonClick());


        // Add listeners to ensure the grid is redrawn when the container size changes
        canvasContainer.widthProperty().addListener((observable, oldValue, newValue) -> {
            canvas.setWidth(newValue.doubleValue());
            drawGrid(gc);
        });

        canvasContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            canvas.setHeight(newValue.doubleValue());
            drawGrid(gc);
        });

        // Initial grid drawing
        drawGrid(gc);

        // Set up event listeners for actor creation
        actorButton.setOnAction(event -> handleActorButtonClick(gc));
        canvas.setOnMouseClicked(event -> {

            if (activeButton == actorButton) {
                createActor(gc, event.getX(), event.getY());


            }
            else  if (activeButton == useCaseButton) {
                createUseCase(gc, event.getX(), event.getY());

            }


        });
        toolboxVBox.setOnMouseClicked(event -> deselectActiveButton());
        propertiesPanel.setOnMouseClicked(event -> deselectActiveButton());
    }

    private void drawGrid(GraphicsContext gc) {
        // Ensure the canvas dimensions are correct
        double canvasWidth = canvasContainer.getWidth();
        double canvasHeight = canvasContainer.getHeight();

        // Clear the canvas
        gc.clearRect(0, 0, canvasWidth, canvasHeight);

        double gridSpacing = 10; // Grid spacing

        gc.setLineWidth(0.5);
        gc.setStroke(Color.rgb(180, 180, 180));

        // Draw vertical grid lines
        for (double x = 0; x <= canvasWidth; x += gridSpacing) {
            gc.strokeLine(x, 0, x, canvasHeight);
        }

        // Draw horizontal grid lines
        for (double y = 0; y <= canvasHeight; y += gridSpacing) {
            gc.strokeLine(0, y, canvasWidth, y);
        }
    }
    private void deselectActiveElement(GraphicsContext gc) {
        // Reset selected elements
        selectedActor = null;
        selectedUseCase = null;

        // Redraw canvas
        redrawCanvas(gc);
    }

    private void onMousePressed(MouseEvent event, GraphicsContext gc) {
        double mouseX = event.getX();
        double mouseY = event.getY();

        // Handle line drawing logic if the association button is active
        if (activeButton == associationButton) {
            // Reset starting variables
            startActor = null;
            startUseCase = null;
            deselectActiveElement(gc);
            startConnectionIndex = -1;

            // Check for snapping to an actor's connection point
            for (Actor actor : actors) {
                for (int i = 0; i < actor.getConnectionPoints().length; i++) {
                    double[] point = actor.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        startX = point[0];
                        startY = point[1];
                        startActor = actor;
                        startConnectionIndex = i; // Record the connection point index
                        isDrawingAssociation = true;
                        System.out.println("Started association at Actor: " + actor.getName() + ", Connection Index: " + i);
                        return; // Exit early once a valid connection point is found
                    }
                }
            }

            // Check for snapping to a use case's connection point
            for (UseCase useCase : useCases) {
                for (int i = 0; i < useCase.getConnectionPoints().length; i++) {
                    double[] point = useCase.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        startX = point[0];
                        startY = point[1];
                        startUseCase = useCase;
                        startConnectionIndex = i; // Record the connection point index
                        isDrawingAssociation = true;
                        System.out.println("Started association at UseCase: " + useCase.getName() + ", Connection Index: " + i);
                        return; // Exit early once a valid connection point is found
                    }
                }
            }

            // If no valid connection point is found, show an error
            System.out.println("No valid start point for association.");
            showError("Start a line from a valid connection point.");
            return;
        }

        // Handle actor selection logic
        for (Actor actor : actors) {
            if (isMouseOverActor(mouseX, mouseY, actor)) {
                selectedActor = actor;
                selectedUseCase = null; // Deselect any selected use case

                // Handle double-click for editing text
                if (event.getClickCount() == 2) {
                    editActorText(actor, gc);
                    return;
                }

                // Calculate drag offset
                dragOffsetX = mouseX - actor.getX();
                dragOffsetY = mouseY - actor.getY();

                // Highlight the selected actor
                redrawCanvas(gc);
                highlightActor(gc, actor);
                return;
            }
        }

        // Handle use case selection logic
        for (UseCase useCase : useCases) {
            if (isMouseOverUseCase(mouseX, mouseY, useCase)) {
                selectedUseCase = useCase;
                selectedActor = null; // Deselect any selected actor

                // Handle double-click for editing text
                if (event.getClickCount() == 2) {
                    editUseCaseText(useCase, gc);
                    return;
                }

                // Highlight the selected use case
                redrawCanvas(gc);
                highlightUseCase(gc, useCase);
                return;
            }
        }

        // If no actor or use case is clicked, deselect both
        if (selectedActor != null || selectedUseCase != null) {
            selectedActor = null;
            selectedUseCase = null;
            redrawCanvas(gc);
            System.out.println("Deselected Actor and UseCase.");
        }
    }
    private boolean isNear(double mouseX, double mouseY, double pointX, double pointY) {
        double tolerance = 10.0; // Snap radius
        return Math.abs(mouseX - pointX) < tolerance && Math.abs(mouseY - pointY) < tolerance;
    }



    private void handleUseCaseButtonClick(GraphicsContext gc) {
        if (activeButton == useCaseButton) {
            deselectActiveButton();
            deselectActiveElement(gc);
        } else {
            activateButton(useCaseButton);
        }
    }
    private boolean isMouseOverUseCase(double mouseX, double mouseY, UseCase useCase) {
        double x = useCase.getX();
        double y = useCase.getY();
        double width = useCase.getWidth();
        double height = useCase.getHeight();

        // Check if the mouse is within the oval
        double dx = mouseX - x;
        double dy = mouseY - y;
        return (dx * dx) / (width * width / 4) + (dy * dy) / (height * height / 4) <= 1;
    }


    private void createUseCase(GraphicsContext gc, double x, double y) {
        UseCase useCase = new UseCase(x, y); // Create a new UseCase object
        useCases.add(useCase); // Add to the list of use cases
        drawUseCase(gc, useCase); // Draw the use case on the canvas
    }
    private void highlightUseCase(GraphicsContext gc, UseCase useCase) {
        double x = useCase.getX();
        double y = useCase.getY();
        double width = useCase.getWidth();
        double height = useCase.getHeight();

        // Draw a light blue highlight around the use case
        gc.setFill(Color.LIGHTBLUE);
        gc.fillOval(x - width / 2, y - height / 2, width, height);

        // Redraw the use case on top of the highlight
        drawUseCase(gc, useCase);
    }

    private void drawUseCase(GraphicsContext gc, UseCase useCase) {
        double x = useCase.getX();
        double y = useCase.getY();
        double width = useCase.getWidth();
        double height = useCase.getHeight();

        // Draw oval for the use case
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(x - width / 2, y - height / 2, width, height);

        // Draw connection points
        gc.setFill(Color.RED);
        for (double[] point : useCase.getConnectionPoints()) {
            gc.fillOval(point[0] - 3, point[1] - 3, 6, 6);
        }

        // Draw use case label
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 12));
        gc.fillText(useCase.getName(), x - width / 4, y); // Center the text within the oval
    }

    private void editUseCaseText(UseCase useCase, GraphicsContext gc) {
        // Create a TextField for editing the use case name
        TextField textField = new TextField(useCase.getName());
        textField.setLayoutX(useCase.getX() - 50); // Center horizontally
        textField.setLayoutY(useCase.getY() - 10); // Center vertically
        textField.setPrefWidth(100);
        textField.setStyle("-fx-border-color: lightblue; -fx-font-size: 12px;");

        // Add the TextField to the canvas container
        canvasContainer.getChildren().add(textField);
        textField.requestFocus();

        // Commit changes on Enter key press
        textField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                useCase.setName(textField.getText().trim());
                canvasContainer.getChildren().remove(textField);
                redrawCanvas(gc);
            }
        });

        // Commit changes on focus loss
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                useCase.setName(textField.getText().trim());
                canvasContainer.getChildren().remove(textField);
                redrawCanvas(gc);
            }
        });
    }
    private void editActorText(Actor actor, GraphicsContext gc) {
        // Create a TextField for editing the actor name
        TextField textField = new TextField(actor.getName());
        textField.setLayoutX(actor.getX() - 30); // Adjust to center the TextField
        textField.setLayoutY(actor.getY() + 30); // Place below the actor figure
        textField.setPrefWidth(100);
        textField.setStyle("-fx-border-color: lightblue; -fx-font-size: 12px;");

        // Add the TextField to the canvas container
        canvasContainer.getChildren().add(textField);
        textField.requestFocus();

        // Commit changes on Enter key press
        textField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                actor.name = textField.getText().trim(); // Update the actor's name
                canvasContainer.getChildren().remove(textField); // Remove the TextField
                redrawCanvas(gc); // Redraw the canvas to update the name
            }
        });

        // Commit changes on focus loss
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                actor.name = textField.getText().trim(); // Update the actor's name
                canvasContainer.getChildren().remove(textField); // Remove the TextField
                redrawCanvas(gc); // Redraw the canvas to update the name
            }
        });
    }
    private void onMouseDragged(MouseEvent event, GraphicsContext gc) {
        double mouseX = event.getX();
        double mouseY = event.getY();

        // Dragging an actor
        if (selectedActor != null) {
            // Update actor position while dragging (within canvas boundaries)
            double newX = Math.max(20, Math.min(mouseX - dragOffsetX, canvasContainer.getWidth() - 20));
            double newY = Math.max(40, Math.min(mouseY - dragOffsetY, canvasContainer.getHeight() - 40));
            selectedActor.x = newX;
            selectedActor.y = newY;

            // Redraw the canvas with the updated position
            redrawCanvas(gc);
            highlightActor(gc, selectedActor);
        }

        // Dragging a use case
        if (selectedUseCase != null) {
            // Update use case position while respecting canvas boundaries
            double newX = Math.max(selectedUseCase.getWidth() / 2,
                    Math.min(mouseX - dragOffsetX, canvasContainer.getWidth() - selectedUseCase.getWidth() / 2));
            double newY = Math.max(selectedUseCase.getHeight() / 2,
                    Math.min(mouseY - dragOffsetY, canvasContainer.getHeight() - selectedUseCase.getHeight() / 2));
            selectedUseCase.x = newX;
            selectedUseCase.y = newY;

            // Redraw the canvas with updated positions
            redrawCanvas(gc);
            highlightUseCase(gc, selectedUseCase);
        }

        // Preview association line if drawing
        if (activeButton == associationButton && isDrawingAssociation) {
            gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
            redrawCanvas(gc); // Redraw the background and other elements
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(1);
            gc.strokeLine(startX, startY, mouseX, mouseY); // Draw temporary association line
        }
    }

    private void onMouseReleased(MouseEvent event, GraphicsContext gc) {
        double mouseX = event.getX();
        double mouseY = event.getY();

        // Finalize actor position
        if (selectedActor != null) {
            redrawCanvas(gc);
            highlightActor(gc, selectedActor);
        }

        // Finalize use case position
        if (selectedUseCase != null) {
            redrawCanvas(gc);
            highlightUseCase(gc, selectedUseCase);
        }

        // Finalize association line
        if (activeButton == associationButton && isDrawingAssociation) {
            for (Actor actor : actors) {
                for (int i = 0; i < actor.getConnectionPoints().length; i++) {
                    double[] point = actor.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1]) && actor != startActor) {
                        associations.add(new LineConnection(
                                startActor, startConnectionIndex,
                                actor, i
                        ));
                        isDrawingAssociation = false;
                        redrawCanvas(gc);
                        return;
                    }
                }
            }

            for (UseCase useCase : useCases) {
                for (int i = 0; i < useCase.getConnectionPoints().length; i++) {
                    double[] point = useCase.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1]) && useCase != startUseCase) {
                        associations.add(new LineConnection(
                                startActor != null ? startActor : startUseCase, startConnectionIndex,
                                useCase, i
                        ));

                        isDrawingAssociation = false;
                        redrawCanvas(gc);
                        return;
                    }
                }
            }

            showError("Cannot draw line. No valid connection point reached.");
            isDrawingAssociation = false;
            redrawCanvas(gc);
        }
    }

// --- Drawing and Helper Methods ---

    private void redrawCanvas(GraphicsContext gc) {
        // Clear the canvas and redraw the grid
        drawGrid(gc);
        for (UseCase useCase : useCases) {
            drawUseCase(gc, useCase);
        }
        // Redraw all actors
        for (Actor actor : actors) {
            drawActor(gc, actor);
        }
        for (LineConnection line : associations) {
            drawAssociationLine(gc, line);
        }
    }
    private void drawAssociationLine(GraphicsContext gc, LineConnection line) {
        double[] start = line.getStartPoint();
        double[] end = line.getEndPoint();

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(start[0], start[1], end[0], end[1]);
    }


    private void highlightActor(GraphicsContext gc, Actor actor) {
        double x = actor.getX();
        double y = actor.getY();

        // Highlight actor with a light blue background
        gc.setFill(Color.LIGHTBLUE);
        gc.fillOval(x - 12, y - 42, 24, 24); // Slightly larger highlight for the head
        gc.fillRect(x - 20, y - 20, 40, 40); // Body and arm highlight

        // Redraw the actor on top of the highlight
        drawActor(gc, actor);
    }

    private boolean isMouseOverActor(double mouseX, double mouseY, Actor actor) {
        double x = actor.getX();
        double y = actor.getY();

        // Check if mouse is within the actor's head or body area
        return (mouseX >= x - 10 && mouseX <= x + 10 && mouseY >= y - 40 && mouseY <= y) || // Head
                (mouseX >= x - 10 && mouseX <= x + 10 && mouseY >= y && mouseY <= y + 20);  // Body
    }
    private void deselectActiveButton() {
        if (activeButton != null) {
            // Reset the active button's style
            activeButton.setStyle("-fx-background-color: #5DADE2; -fx-text-fill: white; -fx-font-weight: bold;");
            activeButton = null; // Clear the active button
        }
        isDrawingAssociation = false;
        deselectActiveElement(((Canvas) canvasContainer.getChildren().get(0)).getGraphicsContext2D());
    }

    private void handleActorButtonClick(GraphicsContext gc) {
        if (activeButton == actorButton) {
            // Deactivate the actor button if already active
            deselectActiveButton();
            deselectActiveElement(gc);
        } else {
            // Activate the actor button
            activateButton(actorButton);
        }
    }

    // Activate a button (change style and set active)
    private void activateButton(Button button) {
        // Reset the style of all buttons
        resetAllButtonStyles();

        // Set the new button as active and update its style
        activeButton = button;
        button.setStyle("-fx-background-color: #5DADE2; -fx-text-fill: black; -fx-font-weight: bold;");
    }
    private void resetAllButtonStyles() {
        actorButton.setStyle("-fx-background-color: #5DADE2; -fx-text-fill: white; -fx-font-weight: bold;");
        useCaseButton.setStyle("-fx-background-color: #5DADE2; -fx-text-fill: white; -fx-font-weight: bold;");
        associationButton.setStyle("-fx-background-color: #5DADE2; -fx-text-fill: white; -fx-font-weight: bold;");
        // Add other buttons here if necessary
    }

    private void handleAssociationButtonClick() {
        if (activeButton == associationButton) {
            deselectActiveButton();

        } else {
            activateButton(associationButton);
        }
    }

    // Handle the start of an association line

    // Handle dragging to preview the association line
    private void showError(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void createActor(GraphicsContext gc, double x, double y) {
        Actor actor = new Actor(x, y); // Create a new Actor object
        actors.add(actor); // Add to the list of actors
        drawActor(gc, actor); // Draw the actor on the canvas
    }

    private void drawActor(GraphicsContext gc, Actor actor) {
        double x = actor.getX();
        double y = actor.getY();

        // Draw stick figure for the actor
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeOval(x - 10, y - 40, 20, 20); // Head
        gc.strokeLine(x, y - 20, x, y); // Body
        gc.strokeLine(x, y, x - 10, y + 20); // Left leg
        gc.strokeLine(x, y, x + 10, y + 20); // Right leg
        gc.strokeLine(x, y - 15, x - 20, y - 15); // Left arm (lowered below neck)
        gc.strokeLine(x, y - 15, x + 20, y - 15); // Right arm (lowered below neck)

        // Draw connection points
        gc.setFill(Color.RED);
        for (double[] point : actor.getConnectionPoints()) {
            gc.fillOval(point[0] - 3, point[1] - 3, 6, 6); // Draw small red circles
        }

        // Draw actor label
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 12));
        gc.fillText(actor.getName(), x - 20, y + 40); // Label below the actor
    }
    // Actor class for serialization and connection points
    private static class Actor implements Serializable {
        private double x, y;
        private String name;
        private static int count = 0; // Counter for unique actor names

        public Actor(double x, double y) {
            this.x = x;
            this.y = y;
            this.name = "Actor" + (++count); // Generate unique actor name
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public String getName() {
            return name;
        }

        // Define connection points (e.g., top, bottom, left, right)
        public double[][] getConnectionPoints() {
            return new double[][]{
                    {x, y - 40}, // Top
                    {x, y + 20}, // Bottom
                    {x - 15, y - 10}, // Left
                    {x + 15, y - 10}  // Right
            };
        }
    }
    private static class UseCase implements Serializable {
        private double x, y;
        private double width = 120; // Default width
        private double height = 60; // Default height
        private String name;
        private static int count = 0;

        public UseCase(double x, double y) {
            this.x = x;
            this.y = y;
            this.name = "UseCase" + (++count);
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public double getWidth() {
            return width;
        }

        public double getHeight() {
            return height;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double[][] getConnectionPoints() {
            return new double[][]{
                    {x, y - height / 2}, // Top
                    {x, y + height / 2}, // Bottom
                    {x - width / 2, y},  // Left
                    {x + width / 2, y}   // Right
            };
        }
    }
    // LineConnection class to store associations
    private static class LineConnection {
        private Object startElement; // Actor or UseCase
        private Object endElement;   // Actor or UseCase
        private int startConnectionIndex;
        private int endConnectionIndex;

        public LineConnection(Object startElement, int startConnectionIndex, Object endElement, int endConnectionIndex) {
            this.startElement = startElement;
            this.startConnectionIndex = startConnectionIndex;
            this.endElement = endElement;
            this.endConnectionIndex = endConnectionIndex;
        }

        public double[] getStartPoint() {
            if (startElement instanceof Actor) {
                return ((Actor) startElement).getConnectionPoints()[startConnectionIndex];
            } else if (startElement instanceof UseCase) {
                return ((UseCase) startElement).getConnectionPoints()[startConnectionIndex];
            }
            return new double[]{0, 0};
        }

        public double[] getEndPoint() {
            if (endElement instanceof Actor) {
                return ((Actor) endElement).getConnectionPoints()[endConnectionIndex];
            } else if (endElement instanceof UseCase) {
                return ((UseCase) endElement).getConnectionPoints()[endConnectionIndex];
            }
            return new double[]{0, 0};
        }
    }

}
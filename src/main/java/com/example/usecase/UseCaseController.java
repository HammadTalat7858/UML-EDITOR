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

    @FXML
    public void initialize() {
        // Create a new canvas and add it to the canvasContainer
        Canvas canvas = new Canvas(910, 780); // Initial size
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvasContainer.getChildren().add(canvas);
        // Add mouse event listeners to the canvas
        canvas.setOnMousePressed(event -> onMousePressed(event, gc));
        canvas.setOnMouseDragged(event -> onMouseDragged(event, gc));
        canvas.setOnMouseReleased(event -> onMouseReleased(gc));

        // Handle actor button clicks
        actorButton.setOnAction(event -> handleActorButtonClick(gc));
        useCaseButton.setOnAction(event -> handleUseCaseButtonClick(gc));

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
                createUseCase(gc, event.getX(), event.getY()); // Create a new use case

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
    private void onMousePressed(MouseEvent event, GraphicsContext gc) {
        double mouseX = event.getX();
        double mouseY = event.getY();

        // Check if an actor is clicked
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

        // Check if a use case is clicked
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
        }
    }

    private void handleUseCaseButtonClick(GraphicsContext gc) {
        if (activeButton == useCaseButton) {
            deselectActiveButton();
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
    }

    private void onMouseReleased(GraphicsContext gc) {
        if (selectedActor != null) {
            // Finalize position and redraw the canvas
            redrawCanvas(gc);
            highlightActor(gc, selectedActor);
        }

        if (selectedUseCase != null) {
            redrawCanvas(gc);
            highlightUseCase(gc, selectedUseCase);
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
    }

    private void handleActorButtonClick(GraphicsContext gc) {
        if (activeButton == actorButton) {
            // Deactivate the actor button if already active
            deselectActiveButton();
        } else {
            // Activate the actor button
            activateButton(actorButton);
        }
    }

    // Activate a button (change style and set active)
    private void activateButton(Button button) {
        activeButton = button;
        button.setStyle("-fx-background-color: #5DADE2; -fx-text-fill: black; -fx-font-weight: bold;");
    }

    // Deactivate a button (reset style)
    private void deactivateButton(Button button) {
        activeButton = null;
        button.setStyle("-fx-background-color: #5DADE2; -fx-text-fill: white; -fx-font-weight: bold;");
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
        double startX, startY, endX, endY;
        Actor startActor, endActor;
        UseCase startUseCase, endUseCase;

        public LineConnection(double startX, double startY, double endX, double endY, Actor startActor, UseCase startUseCase, Actor endActor, UseCase endUseCase) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.startActor = startActor;
            this.startUseCase = startUseCase;
            this.endActor = endActor;
            this.endUseCase = endUseCase;
        }
    }


}
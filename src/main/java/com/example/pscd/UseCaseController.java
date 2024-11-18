package com.example.pscd;

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
import javafx.scene.text.Text;
import javafx.scene.transform.Scale;

import java.util.ArrayList;
import java.util.List;

public class UseCaseController {

    @FXML
    private Button actorButton;

    @FXML
    private Button associationButton;

    @FXML
    private Button useCaseButton;

    @FXML
    public Pane canvasContainer;

    @FXML
    private TextField attributesField;
    @FXML
    private VBox toolboxVBox;

    private Button activeButton; // Keeps track of the currently selected button
    private Scale scaleTransform;

    private boolean addingActor = false;
    private boolean addingAssociation = false;
    private boolean addingUseCase = false;

    private double startX = 0, startY = 0; // Start point for associations
    private Actor selectedActor = null; // Store selected Actor for moving
    private UseCase selectedUseCase = null; // Store selected UseCase for moving

    private List<Actor> actors = new ArrayList<>();
    private List<UseCase> useCases = new ArrayList<>();
    private List<Association> associations = new ArrayList<>();
    @FXML
    public void initialize() {
        Canvas canvas = new Canvas(880, 780);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvasContainer.getChildren().add(canvas);

        // Set up zoom functionality
        scaleTransform = new Scale(1, 1, 0, 0);
        canvasContainer.getTransforms().add(scaleTransform);

        // Add zooming event listeners
        canvasContainer.addEventFilter(ScrollEvent.SCROLL, this::handleZoom);
        canvasContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleZoomKeys);
            }
        });

        // Draw the grid initially
        drawGrid(gc);

        // Manage button clicks for mode selection
        List<Button> buttons = List.of(actorButton, associationButton, useCaseButton);
        for (Button button : buttons) {
            button.setOnAction(event -> handleButtonClick(button, buttons));
        }

        // Handle canvas clicks for drawing
        canvasContainer.setOnMouseClicked(this::onCanvasClick);


        // Handle clicks in the toolbox but outside the buttons
        toolboxVBox.setOnMouseClicked(event -> {
            // If the mouse click occurs in the left panel but outside the buttons
            if (isClickOutsideButtons(event.getX(), event.getY())) {
                deselectAllButtons(buttons); // Deselect all buttons
                resetModes(); // Reset all active modes
            }
        });


        // Ensure buttons are initially unpressed (no functionality active)
        deselectAllButtons(buttons);
    }

    // Check if the click occurred outside the buttons but inside the toolbox
    private boolean isClickOutsideButtons(double x, double y) {
        double actorButtonX = actorButton.getLayoutX();
        double actorButtonY = actorButton.getLayoutY();
        double actorButtonWidth = actorButton.getWidth();
        double actorButtonHeight = actorButton.getHeight();

        double associationButtonX = associationButton.getLayoutX();
        double associationButtonY = associationButton.getLayoutY();
        double associationButtonWidth = associationButton.getWidth();
        double associationButtonHeight = associationButton.getHeight();

        double useCaseButtonX = useCaseButton.getLayoutX();
        double useCaseButtonY = useCaseButton.getLayoutY();
        double useCaseButtonWidth = useCaseButton.getWidth();
        double useCaseButtonHeight = useCaseButton.getHeight();

        // Check if the click is outside all button boundaries
        return !(
                (x >= actorButtonX && x <= actorButtonX + actorButtonWidth &&
                        y >= actorButtonY && y <= actorButtonY + actorButtonHeight) ||
                        (x >= associationButtonX && x <= associationButtonX + associationButtonWidth &&
                                y >= associationButtonY && y <= associationButtonY + associationButtonHeight) ||
                        (x >= useCaseButtonX && x <= useCaseButtonX + useCaseButtonWidth &&
                                y >= useCaseButtonY && y <= useCaseButtonY + useCaseButtonHeight)
        );
    }

    // Handle button click to set the active button and its functionality
    private void handleButtonClick(Button clickedButton, List<Button> buttons) {
        // Deselect all buttons
        for (Button button : buttons) {
            button.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: gray; -fx-font-weight: normal;");
        }

        // Highlight the selected button
        clickedButton.setStyle("-fx-background-color: lightblue; -fx-text-fill: black; -fx-border-color: gray; -fx-font-weight: bold;");
        activeButton = clickedButton; // Set the active button

        resetModes();
        if (clickedButton == actorButton) {
            addingActor = true;
        } else if (clickedButton == associationButton) {
            addingAssociation = true;
        } else if (clickedButton == useCaseButton) {
            addingUseCase = true;
        }
    }

    // Reset the functionality of all modes when clicking outside the buttons
    private void resetModes() {
        addingActor = false;
        addingAssociation = false;
        addingUseCase = false;
        startX = 0;
        startY = 0;
    }
    // Deselect all buttons and reset the active button
    private void deselectAllButtons(List<Button> buttons) {
        for (Button button : buttons) {
            button.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: gray; -fx-font-weight: normal;");
        }
        activeButton = null; // No active button selected
        resetModes(); // Reset any active drawing modes
    }


    private void drawGrid(GraphicsContext gc) {
        double width = 910;
        double height = 780;
        double gridSize = 20;

        gc.clearRect(0, 0, width, height);
        gc.setStroke(Color.LIGHTGRAY);
        gc.setLineWidth(0.5);

        // Draw vertical lines
        for (double x = 0; x <= width; x += gridSize) {
            gc.strokeLine(x, 0, x, height);
        }

        // Draw horizontal lines
        for (double y = 0; y <= height; y += gridSize) {
            gc.strokeLine(0, y, width, y);
        }
    }

    private void handleZoom(ScrollEvent event) {
        if (event.isControlDown()) {
            double zoomFactor = (event.getDeltaY() > 0) ? 1.1 : 0.9;
            double newScaleX = Math.max(1, Math.min(scaleTransform.getX() * zoomFactor, 5));
            double newScaleY = Math.max(1, Math.min(scaleTransform.getY() * zoomFactor, 5));
            scaleTransform.setX(newScaleX);
            scaleTransform.setY(newScaleY);
            event.consume();
        }
    }

    private void handleZoomKeys(KeyEvent event) {
        if (event.isControlDown()) {
            if (event.getCode() == KeyCode.PLUS || event.getCode() == KeyCode.EQUALS) {
                scaleTransform.setX(scaleTransform.getX() * 1.1);
                scaleTransform.setY(scaleTransform.getY() * 1.1);
            } else if (event.getCode() == KeyCode.MINUS) {
                scaleTransform.setX(scaleTransform.getX() * 0.9);
                scaleTransform.setY(scaleTransform.getY() * 0.9);
            }
        }
    }
    private void onCanvasClick(MouseEvent event) {
        double x = event.getX();
        double y = event.getY();

        // Ensure no drawing occurs if no mode is active
        if (!addingActor && !addingUseCase && !addingAssociation) {
            return;
        }

        if (addingActor) {
            drawActor(x, y);
        } else if (addingUseCase) {
            String name = attributesField.getText().trim();
            drawUseCase(x, y, name.isEmpty() ? "Use Case" : name);
        } else if (addingAssociation) {
            handleAssociationClick(x, y); // Ensure this method is accessible
        }
    }
    private void handleAssociationClick(double x, double y) {
        if (startX == 0 && startY == 0) {
            startX = x;
            startY = y;
        } else {
            drawAssociationLine(startX, startY, x, y);
            startX = 0;
            startY = 0;
        }
    }
    private void drawActor(double x, double y) {
        Actor actor = new Actor(x, y);
        actors.add(actor);

        Circle head = new Circle(x, y - 30, 10, Color.TRANSPARENT);
        head.setStroke(Color.BLACK);

        Line body = new Line(x, y - 20, x, y + 20);
        Line leftArm = new Line(x, y - 10, x - 20, y + 10);
        Line rightArm = new Line(x, y - 10, x + 20, y + 10);
        Line leftLeg = new Line(x, y + 20, x - 15, y + 40);
        Line rightLeg = new Line(x, y + 20, x + 15, y + 40);

        // Group components for movement
        List<javafx.scene.Node> components = List.of(head, body, leftArm, rightArm, leftLeg, rightLeg);
        for (javafx.scene.Node component : components) {
            component.setOnMouseClicked(event -> {
                // Set the selected actor and visually highlight it
                selectedActor = actor;
                highlightActor(actor);
            });
        }


        canvasContainer.getChildren().addAll(head, body, leftArm, rightArm, leftLeg, rightLeg);
    }
    private void highlightActor(Actor actor) {
        // Change the color or appearance of the actor to indicate selection
        actor.getHead().setStroke(Color.BLUE);
        actor.getBody().setStroke(Color.BLUE);
        actor.getLeftArm().setStroke(Color.BLUE);
        actor.getRightArm().setStroke(Color.BLUE);
        actor.getLeftLeg().setStroke(Color.BLUE);
        actor.getRightLeg().setStroke(Color.BLUE);
    }
    private void clearHighlights() {
        for (Actor actor : actors) {
            actor.getHead().setStroke(Color.BLACK);
            actor.getBody().setStroke(Color.BLACK);
            actor.getLeftArm().setStroke(Color.BLACK);
            actor.getRightArm().setStroke(Color.BLACK);
            actor.getLeftLeg().setStroke(Color.BLACK);
            actor.getRightLeg().setStroke(Color.BLACK);
        }

        for (UseCase useCase : useCases) {
            useCase.getShape().setStroke(Color.BLACK);
        }
    }

    private void drawUseCase(double x, double y, String name) {
        UseCase useCase = new UseCase(x, y, name);
        useCases.add(useCase);

        Ellipse useCaseShape = new Ellipse(x, y, 50, 25);
        useCaseShape.setFill(Color.TRANSPARENT);
        useCaseShape.setStroke(Color.BLACK);

        Text useCaseText = new Text(x - 30, y + 5, name);

        // Attach drag handlers
        useCaseShape.setOnMousePressed(event -> {
            selectedUseCase = useCase; // Track which use case is being dragged
        });
        useCaseShape.setOnMouseClicked(event -> {
            // Set the selected use case and visually highlight it
            selectedUseCase = useCase;
            highlightUseCase(useCase);
        });



        canvasContainer.getChildren().addAll(useCaseShape, useCaseText);
    }
    private void highlightUseCase(UseCase useCase) {
        useCase.getShape().setStroke(Color.BLUE);
    }

    private void drawAssociationLine(double startX, double startY, double endX, double endY) {
        Association association = new Association(startX, startY, endX, endY);
        associations.add(association);

        Line line = new Line(startX, startY, endX, endY);
        line.setStroke(Color.BLACK);

        // Use arrays for lambda compatibility
        final double[] tempStartX = {startX};
        final double[] tempStartY = {startY};
        final double[] tempEndX = {endX};
        final double[] tempEndY = {endY};

        // Set mouse handlers
        line.setOnMousePressed(event -> {
            tempStartX[0] = line.getStartX();
            tempStartY[0] = line.getStartY();
            tempEndX[0] = line.getEndX();
            tempEndY[0] = line.getEndY();
        });

        line.setOnMouseDragged(event -> {
            double mouseX = event.getX();
            double mouseY = event.getY();

            if (Math.abs(mouseX - tempStartX[0]) < Math.abs(mouseX - tempEndX[0])) {
                line.setStartX(mouseX);
                line.setStartY(mouseY);
                association.setStart(mouseX, mouseY);
            } else {
                line.setEndX(mouseX);
                line.setEndY(mouseY);
                association.setEnd(mouseX, mouseY);
            }
        });

        canvasContainer.getChildren().add(line);
        canvasContainer.setOnMouseDragged(event -> {
            double x = event.getX();
            double y = event.getY();

            if (selectedActor != null) {
                moveActor(selectedActor, x, y);
            } else if (selectedUseCase != null) {
                moveUseCase(selectedUseCase, x, y);
            }
        });
        canvasContainer.setOnMouseClicked(event -> {
            if (event.getTarget() == canvasContainer) {
                selectedActor = null;
                selectedUseCase = null;
                clearHighlights();
            }
        });

    }


    private void moveActor(Actor actor, double x, double y) {
        actor.setX(x);
        actor.setY(y);

        // Update graphical elements
        actor.getHead().setCenterX(x);
        actor.getHead().setCenterY(y - 30);
        actor.getBody().setStartX(x);
        actor.getBody().setStartY(y - 20);
        actor.getBody().setEndX(x);
        actor.getBody().setEndY(y + 20);
        actor.getLeftArm().setStartX(x);
        actor.getLeftArm().setStartY(y - 10);
        actor.getLeftArm().setEndX(x - 20);
        actor.getLeftArm().setEndY(y + 10);
        actor.getRightArm().setStartX(x);
        actor.getRightArm().setStartY(y - 10);
        actor.getRightArm().setEndX(x + 20);
        actor.getRightArm().setEndY(y + 10);
        actor.getLeftLeg().setStartX(x);
        actor.getLeftLeg().setStartY(y + 20);
        actor.getLeftLeg().setEndX(x - 15);
        actor.getLeftLeg().setEndY(y + 40);
        actor.getRightLeg().setStartX(x);
        actor.getRightLeg().setStartY(y + 20);
        actor.getRightLeg().setEndX(x + 15);
        actor.getRightLeg().setEndY(y + 40);
    }
    private void moveUseCase(UseCase useCase, double x, double y) {
        useCase.setX(x);
        useCase.setY(y);

        useCase.getShape().setCenterX(x);
        useCase.getShape().setCenterY(y);
        useCase.getText().setX(x - 30);
        useCase.getText().setY(y + 5);
    }
    private void moveAssociation(Association association, double startX, double startY, double endX, double endY) {
        association.setStart(startX, startY);
        association.setEnd(endX, endY);
    }




    // Classes for Actor, UseCase, and Association (no changes)
    private class Actor {
        private double x, y;
        private Circle head;
        private Line body, leftArm, rightArm, leftLeg, rightLeg;

        public Actor(double x, double y) {
            this.x = x;
            this.y = y;
            this.head = new Circle(x, y - 30, 10, Color.TRANSPARENT);
            this.body = new Line(x, y - 20, x, y + 20);
            this.leftArm = new Line(x, y - 10, x - 20, y + 10);
            this.rightArm = new Line(x, y - 10, x + 20, y + 10);
            this.leftLeg = new Line(x, y + 20, x - 15, y + 40);
            this.rightLeg = new Line(x, y + 20, x + 15, y + 40);
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public void setX(double x) {
            this.x = x;
        }

        public void setY(double y) {
            this.y = y;
        }

        public Circle getHead() {
            return head;
        }

        public Line getBody() {
            return body;
        }

        public Line getLeftArm() {
            return leftArm;
        }

        public Line getRightArm() {
            return rightArm;
        }

        public Line getLeftLeg() {
            return leftLeg;
        }

        public Line getRightLeg() {
            return rightLeg;
        }
    }

    private class UseCase {
        private double x, y;
        private Ellipse shape;
        private Text text;

        public UseCase(double x, double y, String name) {
            this.x = x;
            this.y = y;
            this.shape = new Ellipse(x, y, 50, 25);
            this.shape.setFill(Color.TRANSPARENT);
            this.shape.setStroke(Color.BLACK);
            this.text = new Text(x - 30, y + 5, name);
        }

        public double getX() {
            return x;
        }

        public double getY() {
            return y;
        }

        public void setX(double x) {
            this.x = x;
        }

        public void setY(double y) {
            this.y = y;
        }

        public Ellipse getShape() {
            return shape;
        }

        public Text getText() {
            return text;
        }
    }
    public class Association {
        private double startX, startY, endX, endY;

        public Association(double startX, double startY, double endX, double endY) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
        }

        public void setStart(double startX, double startY) {
            this.startX = startX;
            this.startY = startY;
        }

        public void setEnd(double endX, double endY) {
            this.endX = endX;
            this.endY = endY;
        }

        public double getStartX() { return startX; }
        public double getStartY() { return startY; }
        public double getEndX() { return endX; }
        public double getEndY() { return endY; }
    }

}

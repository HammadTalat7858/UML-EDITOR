package com.example.UseCaseDiagram;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Optional;

public class UseCaseController {

    @FXML
    private Button actorButton;
    @FXML
    private Pane canvasContainer;
    @FXML
    private VBox toolboxVBox;

    @FXML
    private VBox propertiesPanel;
    @FXML
    private MenuItem jpegMenuItem;

    @FXML
    private MenuItem pngMenuItem;

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
    private LineConnection selectedLine = null;

    @FXML
    private Button associationButton;
    @FXML
    private Button subjectButton;



    private boolean isDrawingAssociation = false; // State for association drawing
    private double startX, startY, endX, endY;    // Line coordinates
    private Actor startActor = null;
    private UseCase startUseCase = null;
    private List<LineConnection> associations = new ArrayList<>();
    private int startConnectionIndex = -1;
    private List<UseCaseSubject> subjects = new ArrayList<>();
    private UseCaseSubject selectedSubject = null;
    private boolean isResizingSubject = false;
    private double resizeOffsetX, resizeOffsetY;
    @FXML
    private Button includeButton;

    // Keep track of whether we are drawing an include relation
    private boolean isDrawingInclude = false;
    @FXML
    private Button extendButton;

    private boolean isDrawingExtend = false;
    @FXML
    private Button deleteButton;
    @FXML
    private MenuItem SaveAs;

    @FXML
    private MenuItem Load;

    @FXML
    private MenuItem Close;
    @FXML
    private MenuItem loadusecase;
    @FXML
    private MenuItem loadClass;


    @FXML
    public void initialize() {
        // Create a new canvas and add it to the canvasContainer
        Canvas canvas = new Canvas(910, 780); // Initial size
        GraphicsContext gc = canvas.getGraphicsContext2D();
        canvasContainer.getChildren().add(canvas);
        // Add mouse event listeners to the canvas
        canvas.setOnMousePressed(event -> onMousePressed(event, gc));
        canvas.setOnMouseDragged(event -> onMouseDragged(event, gc));
        canvas.setOnMouseReleased(event -> onMouseReleased(event, gc));


        // Handle actor button clicks
        actorButton.setOnAction(event -> handleActorButtonClick(gc));
        useCaseButton.setOnAction(event -> handleUseCaseButtonClick(gc));
        associationButton.setOnAction(event -> handleAssociationButtonClick());
        subjectButton.setOnAction(actionEvent -> handleSubjectButtonClick(gc));
        includeButton.setOnAction(actionEvent -> handleIncludeButtonClick(gc));
        extendButton.setOnAction(event -> handleExtendButtonClick(gc));
        deleteButton.setOnAction(event -> handleDeleteAction(gc));

        canvasContainer.setFocusTraversable(true); // Make it focusable

        canvasContainer.setOnMouseClicked(event -> {
            // Check if a TextField exists
            boolean hasTextField = canvasContainer.getChildren().stream()
                    .anyMatch(node -> node instanceof TextField);

            if (!hasTextField) {
                // Delay focus request to avoid stealing focus during editing
                Platform.runLater(canvasContainer::requestFocus);
            }
        });




        // Add listeners to ensure the grid is redrawn when the container size changes
        canvasContainer.widthProperty().addListener((observable, oldValue, newValue) -> {
            canvas.setWidth(newValue.doubleValue());
            drawGrid(gc);
            redrawCanvas(gc);
        });

        canvasContainer.heightProperty().addListener((observable, oldValue, newValue) -> {
            canvas.setHeight(newValue.doubleValue());
            drawGrid(gc);
            redrawCanvas(gc);
        });
        canvasContainer.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE) {
                handleDeleteAction(gc);
            }
        });


        // Initial grid drawing
        drawGrid(gc);
        jpegMenuItem.setOnAction(event -> exportAsJPEG());
        pngMenuItem.setOnAction(event -> exportAsPNG());
        // Set up event listeners for actor creation
        actorButton.setOnAction(event -> handleActorButtonClick(gc));
        canvas.setOnMouseClicked(event -> {

            if (activeButton == actorButton) {
                createActor(gc, event.getX(), event.getY());


            } else if (activeButton == useCaseButton) {
                createUseCase(gc, event.getX(), event.getY());

            } else if (activeButton == subjectButton) {
                createUseCaseSubject(gc, event.getX(), event.getY());
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
    @FXML
    private void loadUseCaseDiagram() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Switch to Use Case Diagram? Unsaved changes will be lost.", ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Confirm Switch");
        confirmation.setHeaderText(null);

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/UseCaseDiagram/Use-Case.fxml"));
                Parent useCaseRoot = loader.load();
                Stage currentStage = (Stage) canvasContainer.getScene().getWindow();
                currentStage.setScene(new Scene(useCaseRoot));
                currentStage.setTitle("Use Case Diagram");
            } catch (IOException e) {
                e.printStackTrace();
                showError("Failed to load Use Case Diagram: " + e.getMessage());
            }
        }
    }
    @FXML
    private void loadClassDiagram() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Switch to Class Diagram? Unsaved changes will be lost.", ButtonType.YES, ButtonType.NO);
        confirmation.setTitle("Confirm Switch");
        confirmation.setHeaderText(null);

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ClassDiagram/class_diagram.fxml"));
                Parent classDiagramRoot = loader.load();
                Stage currentStage = (Stage) canvasContainer.getScene().getWindow();
                currentStage.setScene(new Scene(classDiagramRoot));
                currentStage.setTitle("Class Diagram");
            } catch (IOException e) {
                e.printStackTrace();
                showError("Failed to load Class Diagram: " + e.getMessage());
            }
        }
    }


    private void handleExtendButtonClick(GraphicsContext gc) {
        if (activeButton == extendButton) {
            deselectActiveButton();
            deselectActiveElement(gc);
        } else {
            activateButton(extendButton);
        }
    }
    @FXML
    private void exportAsJPEG() {
        saveCanvasToFile("jpeg");
    }

    @FXML
    private void exportAsPNG() {
        saveCanvasToFile("png");
    }
    @FXML
    private void handleCloseAction() {
        // Create a confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Confirmation");
        alert.setHeaderText("You are about to close the application.");
        alert.setContentText("Are you sure you want to exit?");

        // Wait for the user's response
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Exit the application
            Platform.exit();
        } else {
            // If the user cancels, do nothing
            alert.close();
        }
    }


    private void saveCanvasToFile(String format) {
        Canvas canvas = (Canvas) canvasContainer.getChildren().get(0);

        // Take a snapshot of the canvas
        WritableImage writableImage = new WritableImage((int) canvas.getWidth(), (int) canvas.getHeight());
        canvas.snapshot(null, writableImage);

        // Open a file chooser to save the file
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Diagram as " + format.toUpperCase());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(format.toUpperCase() + " Files", "*." + format));
        File file = fileChooser.showSaveDialog(canvasContainer.getScene().getWindow());

        if (file != null) {
            System.out.println("Saving to file: " + file.getAbsolutePath());
            try {
                // Convert WritableImage to BufferedImage
                BufferedImage bufferedImage = convertToBufferedImage(writableImage);

                // Handle JPEG alpha channel issue
                if ("jpeg".equalsIgnoreCase(format)) {
                    bufferedImage = removeAlphaChannel(bufferedImage);
                }

                // Save the image to the file
                boolean result = ImageIO.write(bufferedImage, format, file);
                if (!result) {
                    System.err.println("ImageIO.write() failed for format: " + format);
                } else {
                    System.out.println("Image successfully saved to: " + file.getAbsolutePath());
                }
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error saving file: " + e.getMessage());
            }
        } else {
            System.err.println("File selection cancelled.");
        }
    }

    private BufferedImage convertToBufferedImage(WritableImage writableImage) {
        int width = (int) writableImage.getWidth();
        int height = (int) writableImage.getHeight();

        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        PixelReader pixelReader = writableImage.getPixelReader();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixelReader.getArgb(x, y);
                bufferedImage.setRGB(x, y, argb);
            }
        }

        return bufferedImage;
    }

    private BufferedImage removeAlphaChannel(BufferedImage originalImage) {
        BufferedImage rgbImage = new BufferedImage(
                originalImage.getWidth(),
                originalImage.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = rgbImage.createGraphics();
        graphics.drawImage(originalImage, 0, 0, java.awt.Color.WHITE, null); // Use white as the background color
        graphics.dispose();
        return rgbImage;
    }




    private void deselectActiveElement(GraphicsContext gc) {
        // Reset selected elements
        selectedActor = null;
        selectedUseCase = null;

        // Redraw canvas
        redrawCanvas(gc);
    }
    private void handleDeleteAction(GraphicsContext gc) {
        if (selectedLine != null) {
            // Remove the selected line
            associations.remove(selectedLine);
            selectedLine = null; // Clear the selected line
        } else if (selectedActor != null) {
            // Remove associated lines
            associations.removeIf(line -> line.startElement == selectedActor || line.endElement == selectedActor);
            // Remove the actor
            actors.remove(selectedActor);
            selectedActor = null;
        } else if (selectedUseCase != null) {
            // Remove associated lines
            associations.removeIf(line -> line.startElement == selectedUseCase || line.endElement == selectedUseCase);
            // Remove from the global useCases list
            useCases.remove(selectedUseCase);

            // Check if the use case is inside a subject and remove it from there as well
            for (UseCaseSubject subject : subjects) {
                subject.containedUseCases.remove(selectedUseCase);
            }

            selectedUseCase = null;
        } else if (selectedSubject != null) {

            subjects.remove(selectedSubject);
            selectedSubject = null;
        }

        // Redraw the canvas to reflect deletions
        redrawCanvas(gc);
    }
    @FXML
    private void loadDiagramFromFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Diagram");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Diagram Files", "*.diagram"));
        File file = fileChooser.showOpenDialog(canvasContainer.getScene().getWindow());

        if (file != null) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                // Load actors, use cases, subjects, and connections from file
                actors = (ArrayList<Actor>) ois.readObject();
                useCases = (ArrayList<UseCase>) ois.readObject();
                subjects = (ArrayList<UseCaseSubject>) ois.readObject();
                associations = (ArrayList<LineConnection>) ois.readObject();

                // Redraw the canvas with the loaded data
                GraphicsContext gc = ((Canvas) canvasContainer.getChildren().get(0)).getGraphicsContext2D();
                gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
                redrawCanvas(gc);

                showInfo("Diagram loaded successfully from " + file.getName());
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                showError("Error loading diagram: " + e.getMessage());
            }
        }
    }

    @FXML
    private void saveDiagramToFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Diagram");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Diagram Files", "*.diagram"));
        File file = fileChooser.showSaveDialog(canvasContainer.getScene().getWindow());

        if (file != null) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
                // Save actors, use cases, subjects, and connections to file
                oos.writeObject(new ArrayList<>(actors));
                oos.writeObject(new ArrayList<>(useCases));
                oos.writeObject(new ArrayList<>(subjects));
                oos.writeObject(new ArrayList<>(associations));

                showInfo("Diagram saved successfully to " + file.getName());
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error saving diagram: " + e.getMessage());
            }
        }
    }
    private void showInfo(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void handleIncludeButtonClick(GraphicsContext gc) {
        if (activeButton == includeButton) {
            deselectActiveButton();
            deselectActiveElement(gc);

        } else {
            activateButton(includeButton);
        }
    }


    private void onMousePressed(MouseEvent event, GraphicsContext gc) {
        double mouseX = event.getX();
        double mouseY = event.getY();
        // Reset dragging offsets
        dragOffsetX = 0;
        dragOffsetY = 0;

        // 1. Handle Association line drawing
        if (activeButton == associationButton) {
            startActor = null;
            startUseCase = null;
            deselectActiveElement(gc);
            startConnectionIndex = -1;

            // Check for actor connection points
            for (Actor actor : actors) {
                for (int i = 0; i < actor.getConnectionPoints().length; i++) {
                    double[] point = actor.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        startX = point[0];
                        startY = point[1];
                        startActor = actor;
                        startConnectionIndex = i;
                        isDrawingAssociation = true;
                        return;
                    }
                }
            }

            // Check for use case connection points
            for (UseCase useCase : useCases) {
                for (int i = 0; i < useCase.getConnectionPoints().length; i++) {
                    double[] point = useCase.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        startX = point[0];
                        startY = point[1];
                        startUseCase = useCase;
                        startConnectionIndex = i;
                        isDrawingAssociation = true;
                        return;
                    }
                }
            }

            showError("Start a line from a valid connection point.");
            return;
        }

        // 2. Handle Include line drawing
       else if (activeButton == includeButton) {
            startActor = null;
            startUseCase = null;
            deselectActiveElement(gc);
            startConnectionIndex = -1;

            // Check for UseCase connection points
            for (UseCase useCase : useCases) {
                for (int i = 0; i < useCase.getConnectionPoints().length; i++) {
                    double[] point = useCase.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        startX = point[0];
                        startY = point[1];
                        startUseCase = useCase;
                        startConnectionIndex = i;
                        isDrawingInclude = true;
                        return;
                    }
                }
            }

            // Check for Actor connection points
            for (Actor actor : actors) {
                for (int i = 0; i < actor.getConnectionPoints().length; i++) {
                    double[] point = actor.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        startX = point[0];
                        startY = point[1];
                        startActor = actor;
                        startConnectionIndex = i;
                        isDrawingInclude = true;
                        return;
                    }
                }
            }

            showError("Start an include line from a valid connection point.");
            return;
        }

        // 3. Handle Extend line drawing
       else if (activeButton == extendButton) {
            startActor = null; // Extend lines cannot connect to actors
            startUseCase = null;
            deselectActiveElement(gc);
            startConnectionIndex = -1;

            // Check for UseCase connection points only
            for (UseCase useCase : useCases) {
                for (int i = 0; i < useCase.getConnectionPoints().length; i++) {
                    double[] point = useCase.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        startX = point[0];
                        startY = point[1];
                        startUseCase = useCase;
                        startConnectionIndex = i;
                        isDrawingExtend = true;
                        return;
                    }
                }
            }
            for (Actor actor : actors) {
                for (int i = 0; i < actor.getConnectionPoints().length; i++) {
                    double[] point = actor.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        startX = point[0];
                        startY = point[1];
                        startActor = actor;
                        startConnectionIndex = i;
                        isDrawingExtend = true;
                        return;
                    }
                }
            }

            showError("Start an extend line from a valid use case connection point.");
            return;
        }

        // 4. Check for items inside subjects (UseCases or Actors)
        for (UseCaseSubject subject : subjects) {
            if (subject.isMouseOver(mouseX, mouseY)) {
                // Check for UseCases inside this subject
                for (UseCase useCase : useCases) {
                    if (subject.contains(useCase.getX(), useCase.getY()) && isMouseOverUseCase(mouseX, mouseY, useCase)) {
                        selectedUseCase = useCase;
                        selectedActor = null;
                        selectedSubject = null;
                        selectedLine = null;

                        // Handle double-click for editing text
                        if (event.getClickCount() == 2) {
                            editUseCaseText(useCase, gc);
                            return;
                        }

                        // Set drag offsets
                        dragOffsetX = mouseX - useCase.getX();
                        dragOffsetY = mouseY - useCase.getY();

                        // Highlight the selected use case
                        redrawCanvas(gc);
                        highlightUseCase(gc, useCase);
                        return;
                    }
                }

                // Check for actors inside this subject
                for (Actor actor : actors) {
                    if (subject.contains(actor.getX(), actor.getY()) && isMouseOverActor(mouseX, mouseY, actor)) {
                        selectedActor = actor;
                        selectedUseCase = null;
                        selectedSubject = null;
                        selectedLine = null;


                        // Handle double-click for editing text
                        if (event.getClickCount() == 2) {
                            editActorText(actor, gc);
                            return;
                        }

                        // Calculate drag offsets
                        dragOffsetX = mouseX - actor.getX();
                        dragOffsetY = mouseY - actor.getY();

                        // Highlight the selected actor
                        redrawCanvas(gc);
                        highlightActor(gc, actor);
                        return;
                    }
                }
            }
        }
        for (LineConnection line : associations) {
            if (isMouseNearLine(mouseX, mouseY, line)) {
                selectedLine = line; // Set the clicked line as selected
                selectedActor = null;
                selectedUseCase = null;
                selectedSubject = null;
                redrawCanvas(gc); // Highlight the selected line
                return;
            }
        }

        // 5. Handle resizing logic for subjects
        for (UseCaseSubject subject : subjects) {
            if (subject.isMouseOverBorder(mouseX, mouseY)) {
                selectedSubject = subject;
                selectedActor=null;
                selectedUseCase=null;
                selectedLine=null;
                isResizingSubject = true;

                // Set resize offsets
                resizeOffsetX = mouseX;
                resizeOffsetY = mouseY;
                return;
            }
        }

        // 6. Handle subject dragging
        for (UseCaseSubject subject : subjects) {
            if (subject.isMouseOver(mouseX, mouseY)) {
                selectedSubject = subject;
                selectedActor=null;
                selectedUseCase=null;
                selectedLine=null;
                isResizingSubject = false;

                // Handle double-click for editing heading
                if (event.getClickCount() == 2) {
                    editSubjectHeading(subject, gc);
                    return;
                }

                // Set drag offsets
                dragOffsetX = mouseX - subject.x;
                dragOffsetY = mouseY - subject.y;

                // Highlight the subject
                redrawCanvas(gc);
                highlightUseCaseSubject(gc, subject);
                return;
            }
        }

        // 7. Handle actor selection logic
        for (Actor actor : actors) {
            if (isMouseOverActor(mouseX, mouseY, actor)) {
                selectedActor = actor;
                selectedUseCase = null;
                selectedSubject = null;
                selectedLine = null;


                // Handle double-click for editing text
                if (event.getClickCount() == 2) {
                    editActorText(actor, gc);
                    return;
                }

                // Calculate drag offsets
                dragOffsetX = mouseX - actor.getX();
                dragOffsetY = mouseY - actor.getY();

                // Highlight the selected actor
                redrawCanvas(gc);
                highlightActor(gc, actor);
                return;
            }
        }

        // 8. Handle UseCase selection logic
        for (UseCase useCase : useCases) {
            if (isMouseOverUseCase(mouseX, mouseY, useCase)) {
                selectedUseCase = useCase;
                selectedActor = null;
                selectedSubject = null;
                selectedLine = null;


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

        // 9. If nothing is clicked, deselect everything
        selectedActor = null;
        selectedUseCase = null;
        selectedSubject = null;
        selectedLine=null;
        redrawCanvas(gc);
    }
    private boolean isMouseNearLine(double mouseX, double mouseY, LineConnection line) {
        double[] start = line.getStartPoint();
        double[] end = line.getEndPoint();

        // Calculate the distance from the mouse point to the line
        double distance = Math.abs((end[1] - start[1]) * mouseX - (end[0] - start[0]) * mouseY + end[0] * start[1] - end[1] * start[0])
                / Math.sqrt(Math.pow(end[1] - start[1], 2) + Math.pow(end[0] - start[0], 2));

        double tolerance = 5.0; // Allowable distance to consider a click on the line
        return distance <= tolerance;
    }


    private void editSubjectHeading(UseCaseSubject subject, GraphicsContext gc) {
        // Create a TextField for editing the subject's name
        TextField textField = new TextField(subject.name);

        // Use a Text object to calculate the width of the current text
        javafx.scene.text.Text textHelper = new javafx.scene.text.Text(subject.name);
        textHelper.setFont(Font.font("Arial", 14)); // Match the drawing font
        double textWidth = textHelper.getBoundsInLocal().getWidth();

        // Center the TextField horizontally and align it with the top heading position
        textField.setPrefWidth(subject.width - 20); // Constrain to the subject width
        double textFieldX = subject.x + (subject.width / 2) - (textField.getPrefWidth() / 2);
        double textFieldY = subject.y + 10; // Slightly below the top edge for alignment

        textField.setLayoutX(textFieldX);
        textField.setLayoutY(textFieldY);
        textField.setStyle("-fx-border-color: lightblue; -fx-font-size: 14px;");

        // Add the TextField to the canvas container
        canvasContainer.getChildren().add(textField);
        textField.requestFocus();

        // Commit changes on Enter key press
        textField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                subject.name = textField.getText().trim();
                canvasContainer.getChildren().remove(textField); // Remove the TextField
                redrawCanvas(gc); // Redraw to update the heading
            }
        });

        // Commit changes on focus loss
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                subject.name = textField.getText().trim();
                canvasContainer.getChildren().remove(textField); // Remove the TextField
                redrawCanvas(gc); // Redraw to update the heading
            }
        });
    }

    private boolean isNear(double mouseX, double mouseY, double pointX, double pointY) {
        double tolerance = 10.0; // Snap radius
        return Math.abs(mouseX - pointX) < tolerance && Math.abs(mouseY - pointY) < tolerance;
    }

    private void handleSubjectButtonClick(GraphicsContext gc) {
        if (activeButton == subjectButton) {
            deselectActiveButton();
            deselectActiveElement(gc);

        } else {
            activateButton(subjectButton);
        }
    }

    private void createUseCaseSubject(GraphicsContext gc, double x, double y) {
        UseCaseSubject subject = new UseCaseSubject(x, y);
        subjects.add(subject);
        drawUseCaseSubject(gc, subject);
    }

    private void drawUseCaseSubject(GraphicsContext gc, UseCaseSubject subject) {
        // Draw the rectangle for the subject
        gc.setFill(Color.WHITE);
        gc.fillRect(subject.x, subject.y, subject.width, subject.height);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(subject.x, subject.y, subject.width, subject.height);

        // Calculate the position for horizontally centered, top-aligned text
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 14));

        javafx.scene.text.Text textHelper = new javafx.scene.text.Text(subject.name);
        textHelper.setFont(gc.getFont());
        double textWidth = textHelper.getBoundsInLocal().getWidth();

        double textX = subject.x + (subject.width / 2) - (textWidth / 2); // Center horizontally
        double textY = subject.y + 20; // Position slightly below the top edge

        // Draw the subject's name
        gc.fillText(subject.name, textX, textY);
    }


    private void highlightUseCaseSubject(GraphicsContext gc, UseCaseSubject subject) {
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(3);
        gc.strokeRect(subject.x - 2, subject.y - 2, subject.width + 4, subject.height + 4);
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

        // Check if it's inside any subject
        for (UseCaseSubject subject : subjects) {
            if (subject.isMouseOver(x, y)) {
                subject.addUseCase(useCase);
                useCase.x = Math.max(subject.x + 10, Math.min(x, subject.x + subject.width - 10));
                useCase.y = Math.max(subject.y + 30, Math.min(y, subject.y + subject.height - 10));
                break;
            }
        }

        useCases.add(useCase); // Add to the global list
        redrawCanvas(gc); // Redraw to reflect the changes
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
        javafx.scene.text.Text textHelper = new javafx.scene.text.Text(useCase.getName());
        textHelper.setFont(gc.getFont());
        double textWidth = textHelper.getBoundsInLocal().getWidth();
        double textHeight = textHelper.getBoundsInLocal().getHeight();

        gc.fillText(useCase.getName(), x - textWidth / 2, y + textHeight / 4); // Center the text
    }


    private void editUseCaseText(UseCase useCase, GraphicsContext gc) {
        // Create a TextField for editing the use case name
        TextField textField = new TextField(useCase.getName());
        textField.setLayoutX(useCase.getX() - useCase.getWidth() / 2 + 10); // Center horizontally
        textField.setLayoutY(useCase.getY() - 10); // Center vertically
        textField.setPrefWidth(100);
        textField.setStyle("-fx-border-color: lightblue; -fx-font-size: 12px;");

        // Add the TextField to the canvas container
        canvasContainer.getChildren().add(textField);
        textField.requestFocus();

        // Commit changes on Enter key press
        textField.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                useCase.setName(textField.getText().trim(), gc);
                canvasContainer.getChildren().remove(textField);
                redrawCanvas(gc);
            }
        });

        // Commit changes on focus loss
        textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                useCase.setName(textField.getText().trim(), gc);
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

        // Handle Association Line Preview
        if (activeButton == associationButton && isDrawingAssociation) {
            // Clear the canvas and redraw all elements except the association
            gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
            redrawCanvas(gc); // Redraw other elements

            // Draw the temporary association line
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(1);
            gc.strokeLine(startX, startY, mouseX, mouseY);
            return;
        }

        // Handle Include Line Preview
        else if (activeButton == includeButton && isDrawingInclude) {
            gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
            redrawCanvas(gc); // Redraw other elements

            // Draw the temporary dotted line
            gc.setLineDashes(5); // Set dotted style
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(1);
            gc.strokeLine(startX, startY, mouseX, mouseY);
            gc.setLineDashes(0); // Reset to solid lines
            return;
        }

        // Handle Extend Line Preview
        else if (activeButton == extendButton && isDrawingExtend) {
            gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
            redrawCanvas(gc); // Redraw other elements

            // Draw the temporary dotted line with a distinct style for extend
            gc.setLineDashes(5); // Set dotted style
            gc.setStroke(Color.DARKBLUE); // Use a distinct color for extend
            gc.setLineWidth(1);
            gc.strokeLine(startX, startY, mouseX, mouseY);
            gc.setLineDashes(0); // Reset to solid lines
            return;
        }

        // Dragging a UseCaseSubject
        else if (selectedSubject != null && !isResizingSubject) {
            // Update subject position while respecting canvas boundaries
            double newX = Math.max(0, Math.min(mouseX - dragOffsetX, canvasContainer.getWidth() - selectedSubject.width));
            double newY = Math.max(0, Math.min(mouseY - dragOffsetY, canvasContainer.getHeight() - selectedSubject.height));

            selectedSubject.x = newX;
            selectedSubject.y = newY;

            // Redraw the canvas with the updated position
            redrawCanvas(gc);
            highlightUseCaseSubject(gc, selectedSubject);
            return;
        }

        // Resizing a UseCaseSubject
        else if (selectedSubject != null && isResizingSubject) {
            // Calculate new width and height while respecting boundaries
            double newWidth = Math.max(50, mouseX - selectedSubject.x); // Minimum width = 50
            double newHeight = Math.max(50, mouseY - selectedSubject.y); // Minimum height = 50

            selectedSubject.width = Math.min(newWidth, canvasContainer.getWidth() - selectedSubject.x);
            selectedSubject.height = Math.min(newHeight, canvasContainer.getHeight() - selectedSubject.y);

            // Redraw the canvas with the updated size
            redrawCanvas(gc);
            highlightUseCaseSubject(gc, selectedSubject);
            return;
        }

        // Handle dragging other elements (e.g., actors, use cases, etc.)
        if (selectedActor != null) {
            double newX = Math.max(20, Math.min(mouseX - dragOffsetX, canvasContainer.getWidth() - 20));
            double newY = Math.max(40, Math.min(mouseY - dragOffsetY, canvasContainer.getHeight() - 40));
            selectedActor.x = newX;
            selectedActor.y = newY;

            redrawCanvas(gc);
            highlightActor(gc, selectedActor);
        }

        if (selectedUseCase != null) {
            double newX = Math.max(selectedUseCase.getWidth() / 2,
                    Math.min(mouseX - dragOffsetX, canvasContainer.getWidth() - selectedUseCase.getWidth() / 2));
            double newY = Math.max(selectedUseCase.getHeight() / 2,
                    Math.min(mouseY - dragOffsetY, canvasContainer.getHeight() - selectedUseCase.getHeight() / 2));
            selectedUseCase.x = newX;
            selectedUseCase.y = newY;

            redrawCanvas(gc);
            highlightUseCase(gc, selectedUseCase);
        }
    }


    private void drawArrow(GraphicsContext gc, double[] start, double[] end) {
        double arrowLength = 15; // Length of the arrowhead
        double arrowWidth = 7;   // Width of the arrowhead

        // Calculate the angle of the line
        double angle = Math.atan2(end[1] - start[1], end[0] - start[0]);

        // Calculate the two points of the arrowhead
        double x1 = end[0] - arrowLength * Math.cos(angle - Math.PI / 6);
        double y1 = end[1] - arrowLength * Math.sin(angle - Math.PI / 6);

        double x2 = end[0] - arrowLength * Math.cos(angle + Math.PI / 6);
        double y2 = end[1] - arrowLength * Math.sin(angle + Math.PI / 6);

        // Draw the arrowhead
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(end[0], end[1], x1, y1); // Left side of the arrowhead
        gc.strokeLine(end[0], end[1], x2, y2); // Right side of the arrowhead
    }

    private void onMouseReleased(MouseEvent event, GraphicsContext gc) {
        double mouseX = event.getX();
        double mouseY = event.getY();

        // Handle Association Line
        if (activeButton == associationButton && isDrawingAssociation) {
            boolean validConnection = false;

            // Check for snapping to an actor's connection point
            for (Actor actor : actors) {
                for (int i = 0; i < actor.getConnectionPoints().length; i++) {
                    double[] point = actor.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        // Create a solid association line
                        associations.add(new LineConnection(
                                startActor != null ? startActor : startUseCase, // Start element
                                startConnectionIndex,                          // Start connection point
                                actor,                                         // End element
                                i,                                             // End connection point index
                                "association"                                  // Line type
                        ));
                        validConnection = true;
                        break;
                    }
                }
            }

            // Check for snapping to a use case's connection point
            if (!validConnection) {
                for (UseCase useCase : useCases) {
                    for (int i = 0; i < useCase.getConnectionPoints().length; i++) {
                        double[] point = useCase.getConnectionPoints()[i];
                        if (isNear(mouseX, mouseY, point[0], point[1])) {
                            // Create a solid association line
                            associations.add(new LineConnection(
                                    startActor != null ? startActor : startUseCase, // Start element
                                    startConnectionIndex,                          // Start connection point
                                    useCase,                                       // End element
                                    i,                                             // End connection point index
                                    "association"                                  // Line type
                            ));
                            validConnection = true;
                            break;
                        }
                    }
                }
            }

            if (!validConnection) {
                showError("Cannot draw association line. No valid connection point reached.");
            }

            isDrawingAssociation = false; // Reset association line drawing state
            redrawCanvas(gc);
        }

        // Handle Include Line
        else if (activeButton == includeButton && isDrawingInclude) {
            boolean validConnection = false;

            // Check for snapping to a use case's connection point
            for (UseCase useCase : useCases) {
                for (int i = 0; i < useCase.getConnectionPoints().length; i++) {
                    double[] point = useCase.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        // Create a dotted include line
                        associations.add(new LineConnection(
                                startActor != null ? startActor : startUseCase, // Start element
                                startConnectionIndex,                           // Start connection point
                                useCase,                                        // End element
                                i,                                              // End connection point index
                                "include"                                       // Line type
                        ));
                        validConnection = true;
                        break;
                    }
                }
            }

            // Check for snapping to an actor's connection point
            if (!validConnection) {
                for (Actor actor : actors) {
                    for (int i = 0; i < actor.getConnectionPoints().length; i++) {
                        double[] point = actor.getConnectionPoints()[i];
                        if (isNear(mouseX, mouseY, point[0], point[1])) {
                            // Create a dotted include line
                            associations.add(new LineConnection(
                                    startActor != null ? startActor : startUseCase, // Start element
                                    startConnectionIndex,                           // Start connection point
                                    actor,                                          // End element
                                    i,                                              // End connection point index
                                    "include"                                       // Line type
                            ));
                            validConnection = true;
                            break;
                        }
                    }
                }
            }

            if (!validConnection) {
                showError("Cannot draw include line. No valid connection point reached.");
            }

            isDrawingInclude = false; // Reset include line drawing state
            redrawCanvas(gc);
        }

        // Handle Extend Line
        else if (activeButton == extendButton && isDrawingExtend) {
            boolean validConnection = false;

            // Check for snapping to an actor's connection point
            for (Actor actor : actors) {
                for (int i = 0; i < actor.getConnectionPoints().length; i++) {
                    double[] point = actor.getConnectionPoints()[i];
                    if (isNear(mouseX, mouseY, point[0], point[1])) {
                        // Create a solid association line
                        associations.add(new LineConnection(
                                startActor != null ? startActor : startUseCase, // Start element
                                startConnectionIndex,                          // Start connection point
                                actor,                                         // End element
                                i,                                             // End connection point index
                                "extend"                                  // Line type
                        ));
                        validConnection = true;
                        break;
                    }
                }
            }

            // Check for snapping to a use case's connection point
            if (!validConnection) {
                for (UseCase useCase : useCases) {
                    for (int i = 0; i < useCase.getConnectionPoints().length; i++) {
                        double[] point = useCase.getConnectionPoints()[i];
                        if (isNear(mouseX, mouseY, point[0], point[1])) {
                            // Create a solid association line
                            associations.add(new LineConnection(
                                    startActor != null ? startActor : startUseCase, // Start element
                                    startConnectionIndex,                          // Start connection point
                                    useCase,                                       // End element
                                    i,                                             // End connection point index
                                    "extend"                                  // Line type
                            ));
                            validConnection = true;
                            break;
                        }
                    }
                }
            }

            if (!validConnection) {
                showError("Cannot draw extend line. No valid connection point reached.");
            }

            isDrawingExtend = false; // Reset association line drawing state
            redrawCanvas(gc);
        }

        // Finalize resizing of UseCaseSubject
        else if (selectedSubject != null && isResizingSubject) {
            isResizingSubject = false;
            redrawCanvas(gc);
            highlightUseCaseSubject(gc, selectedSubject);
            return;
        }

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
    }

// --- Drawing and Helper Methods ---

    private void redrawCanvas(GraphicsContext gc) {
        // Clear the canvas and redraw the grid
        gc.clearRect(0, 0, canvasContainer.getWidth(), canvasContainer.getHeight());
        drawGrid(gc);

        // Draw subjects first (background elements)
        for (UseCaseSubject subject : subjects) {
            drawUseCaseSubject(gc, subject);
        }

        // Draw contained use cases inside subjects
        for (UseCaseSubject subject : subjects) {
            for (UseCase useCase : subject.containedUseCases) {
                drawUseCase(gc, useCase);
            }
        }

        // Draw other elements (use cases, actors, and lines)
        for (UseCase useCase : useCases) {
            if (!isUseCaseInSubject(useCase)) { // Skip use cases already drawn within subjects
                drawUseCase(gc, useCase);
            }
        }
        for (Actor actor : actors) {
            drawActor(gc, actor);
        }
        for (LineConnection line : associations) {
            drawAssociationLine(gc, line);
        }
        if (selectedLine != null) {
            gc.setStroke(Color.web("#5DADE2"));
            gc.setLineWidth(3);
            double[] start = selectedLine.getStartPoint();
            double[] end = selectedLine.getEndPoint();
            gc.strokeLine(start[0], start[1], end[0], end[1]);
        }
    }

    private boolean isUseCaseInSubject(UseCase useCase) {
        for (UseCaseSubject subject : subjects) {
            if (subject.containedUseCases.contains(useCase)) {
                return true;
            }
        }
        return false;
    }

    private void drawAssociationLine(GraphicsContext gc, LineConnection line) {
        double[] start = line.getStartPoint();
        double[] end = line.getEndPoint();

        if ("include".equals(line.getType())) {
            gc.setLineDashes(5); // Dotted line for <<include>>
        }
       else if ("extend".equals(line.getType())) {
            gc.setLineDashes(5); // Dotted line for <<extend>>
        }
        else {
            gc.setLineDashes(0); // Solid line for association
        }

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeLine(start[0], start[1], end[0], end[1]);
        gc.setLineDashes(0); // Reset line style

        if ("include".equals(line.getType())) {
            // Draw <<include>> label
            double midX = (start[0] + end[0]) / 2;
            double midY = (start[1] + end[1]) / 2;
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 12));
            gc.fillText("<<include>>", midX, midY - 5);

            // Draw arrow for <<include>>
            drawArrow(gc, start, end);
        }
        else if ("extend".equals(line.getType())) {
            // Draw <<extend>> label
            double midX = (start[0] + end[0]) / 2;
            double midY = (start[1] + end[1]) / 2;
            gc.setFill(Color.BLACK);
            gc.setFont(Font.font("Arial", 12));
            gc.fillText("<<extend>>", midX, midY - 5);

            // Draw arrow for <<extend>>
            drawArrow(gc, start, end);
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
            // Remove the "tool-button-selected" class and reset it to "tool-button"
            activeButton.getStyleClass().removeAll("tool-button-selected");
            if (!activeButton.getStyleClass().contains("tool-button")) {
                activeButton.getStyleClass().add("tool-button");
            }
            activeButton = null; // Clear the active button
        }

        // Clear any drawing or selection state
        isDrawingAssociation = false;
        isDrawingInclude = false;
        isDrawingExtend = false;

        // Deselect any active elements
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
        // Reset styles for all buttons
        resetAllButtonStyles();

        // Add the "tool-button-selected" class to the active button
        activeButton = button;
        button.getStyleClass().add("tool-button-selected");
    }



    private void resetAllButtonStyles() {
        actorButton.getStyleClass().removeAll("tool-button-selected", "tool-button");
        actorButton.getStyleClass().add("tool-button");

        useCaseButton.getStyleClass().removeAll("tool-button-selected", "tool-button");
        useCaseButton.getStyleClass().add("tool-button");

        associationButton.getStyleClass().removeAll("tool-button-selected", "tool-button");
        associationButton.getStyleClass().add("tool-button");

        subjectButton.getStyleClass().removeAll("tool-button-selected", "tool-button");
        subjectButton.getStyleClass().add("tool-button");
        includeButton.getStyleClass().removeAll("tool-button-selected", "tool-button");
        includeButton.getStyleClass().add("tool-button");
        extendButton.getStyleClass().removeAll("tool-button-selected", "tool-button");
        extendButton.getStyleClass().add("tool-button");
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
        gc.strokeLine(x, y - 15, x - 20, y - 15); // Left arm
        gc.strokeLine(x, y - 15, x + 20, y - 15); // Right arm

        // Draw connection points
        gc.setFill(Color.RED);
        for (double[] point : actor.getConnectionPoints()) {
            gc.fillOval(point[0] - 3, point[1] - 3, 6, 6); // Draw small red circles
        }

        // Draw actor label centered horizontally below the figure
        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", 12));
        javafx.scene.text.Text textHelper = new javafx.scene.text.Text(actor.getName());
        textHelper.setFont(gc.getFont());
        double textWidth = textHelper.getBoundsInLocal().getWidth();

        // Position the text centered horizontally relative to the actor figure
        gc.fillText(actor.getName(), x - textWidth / 2, y + 40); // 40px below the actor's head
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

        public void setName(String name, GraphicsContext gc) {
            this.name = name;
            adjustSize(gc);
        }

        // Adjust the size based on the text dimensions
        public void adjustSize(GraphicsContext gc) {
            gc.setFont(Font.font("Arial", 12));
            javafx.scene.text.Text textHelper = new javafx.scene.text.Text(name);
            textHelper.setFont(gc.getFont());

            double textWidth = textHelper.getBoundsInLocal().getWidth();
            double textHeight = textHelper.getBoundsInLocal().getHeight();

            this.width = Math.max(120, textWidth + 20); // Minimum width 120
            this.height = Math.max(60, textHeight + 20); // Minimum height 60
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
    private static class LineConnection implements Serializable {
        private Object startElement; // Actor or UseCase
        private Object endElement;   // Actor or UseCase
        private int startConnectionIndex;
        private int endConnectionIndex;
        private String type; // "association" or "include"

        public LineConnection(Object startElement, int startConnectionIndex, Object endElement, int endConnectionIndex, String type) {
            this.startElement = startElement;
            this.startConnectionIndex = startConnectionIndex;
            this.endElement = endElement;
            this.endConnectionIndex = endConnectionIndex;
            this.type = type; // Type of the connection (e.g., "association", "include")
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

        public String getType() {
            return type;
        }
    }

    private static class UseCaseSubject implements Serializable {
        private double x, y; // Top-left corner
        private double width, height;
        private static int count = 0; // Counter for unique subject names
        private String name;
        private List<UseCase> containedUseCases = new ArrayList<>(); // Use cases inside the subject

        public UseCaseSubject(double x, double y) {
            this.x = x;
            this.y = y;
            this.width = 200; // Default size
            this.height = 150; // Default size
            this.name = "Subject" + (++count);
        }

        public boolean isMouseOver(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        public boolean isMouseOverBorder(double mouseX, double mouseY) {
            double tolerance = 10.0; // Border thickness for resizing
            return (Math.abs(mouseX - x) < tolerance || Math.abs(mouseX - (x + width)) < tolerance ||
                    Math.abs(mouseY - y) < tolerance || Math.abs(mouseY - (y + height)) < tolerance);
        }

        public void addUseCase(UseCase useCase) {
            containedUseCases.add(useCase);
        }
        public boolean contains(double itemX, double itemY) {
            return itemX >= x && itemX <= x + width && itemY >= y && itemY <= y + height;
        }
    }

}
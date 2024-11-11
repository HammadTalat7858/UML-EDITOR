package com.example.scd_project;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
 enum ArrowType {
    ASSOCIATION, INHERITANCE
}
public class Arrow {
    private Line line;
    private Polygon arrowhead;

    public Arrow(double startX, double startY, double endX, double endY, ArrowType type) {
        // Draw the main line
        line = new Line(startX, startY, endX, endY);

        // Draw the appropriate arrowhead based on the type
        arrowhead = createArrowhead(endX, endY, type);

        // Customize line and arrowhead colors
        line.setStroke(Color.BLACK);
        arrowhead.setFill(Color.BLACK);
    }

    private Polygon createArrowhead(double endX, double endY, ArrowType type) {
        Polygon triangle = new Polygon();

        if (type == ArrowType.ASSOCIATION) {
            triangle.getPoints().addAll(0.0, 0.0, -10.0, 5.0, -10.0, -5.0); // Simple association
        } else if (type == ArrowType.INHERITANCE) {
            triangle.getPoints().addAll(0.0, 0.0, -15.0, 10.0, -15.0, -10.0); // Inheritance
        }

        triangle.setTranslateX(endX);
        triangle.setTranslateY(endY);
        return triangle;
    }

    public Line getLine() {
        return line;
    }

    public Polygon getArrowhead() {
        return arrowhead;
    }
}

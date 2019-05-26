package com.tycho.bbf.layout;

import com.tycho.bbf.ContentFinder;
import com.tycho.bbf.LineContentFinder;
import com.tycho.bbf.Utils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ContentAreaLayout {

    @FXML
    private StackPane root;

    @FXML
    private BorderPane borderPane;

    @FXML
    private Canvas background_canvas;

    @FXML
    private Label top;

    @FXML
    private Label left;

    @FXML
    private Label right;

    @FXML
    private Label bottom;

    private Color color = Color.RED;

    @FXML
    private void initialize() {
        System.out.println("Initialize ContentAreaLayout!");
        background_canvas.widthProperty().bind(root.widthProperty());
        background_canvas.heightProperty().bind(root.heightProperty());
    }

    public void setMargins(final int top, final int left, final int right, final int bottom){
        this.top.setText(String.valueOf(top));
        this.left.setText(String.valueOf(left));
        this.right.setText(String.valueOf(right));
        this.bottom.setText(String.valueOf(bottom));

        final double t = (top / 1080f) * root.getHeight();
        final double l = (left / 1920f) * root.getHeight();
        final double r = (right / 1920f) * root.getHeight();
        final double b = (bottom / 1080f) * root.getHeight();

        final GraphicsContext gc = background_canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, background_canvas.getWidth(), background_canvas.getHeight());

        gc.setFill(this.color);
        gc.fillRect(l, t, background_canvas.getWidth() - r - l, background_canvas.getHeight() - b - t);
    }

    public void setColor(final Color color){
        this.color = color;
    }
}

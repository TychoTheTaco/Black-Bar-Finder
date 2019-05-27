package com.tycho.bbf.layout;

import com.tycho.bbf.ContentFinder;
import com.tycho.bbf.LineContentFinder;
import com.tycho.bbf.Utils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

    private int maxContentWidth = 0;
    private int maxContentHeight = 0;

    private Color color = Color.RED;

    private final Rectangle rectangle =  new Rectangle();

    @FXML
    private void initialize() {
        System.out.println("Initialize ContentAreaLayout!");
        background_canvas.widthProperty().bind(root.widthProperty());
        background_canvas.heightProperty().bind(root.heightProperty());
        background_canvas.widthProperty().addListener((observable, oldValue, newValue) -> draw());
        background_canvas.heightProperty().addListener((observable, oldValue, newValue) -> draw());
    }

    public void setContentSize(final int width, final int height){
        maxContentWidth = width;
        maxContentHeight = height;
    }

    public void setMargins(final int top, final int left, final int right, final int bottom){
        System.out.println("Set margins on canvas size: " + background_canvas.getWidth() + " by " + background_canvas.getHeight());
        //System.out.println("Set margins on root size: " + root.getWidth() + " by " + root.getHeight());
        this.top.setText(String.valueOf(top));
        this.left.setText(String.valueOf(left));
        this.right.setText(String.valueOf(right));
        this.bottom.setText(String.valueOf(bottom));

        final double t = (float) top / maxContentHeight;
        final double l = (float) left / maxContentWidth;
        final double r = (float) right / maxContentWidth;
        final double b = (float) bottom / maxContentHeight;
        System.out.println("T: " + t);
        System.out.println("L: " + l);
        System.out.println("R: " + r);
        System.out.println("B: " + b);

        this.rectangle.setX(l);
        this.rectangle.setY(t);
        this.rectangle.setWidth(1.0 - r - l);
        this.rectangle.setHeight(1.0 - b - t);

        draw();
    }

    private void draw(){
        System.out.println("Draw: " + this.rectangle);
        System.out.println("Canvas size: " + background_canvas.getWidth() + " by " + background_canvas.getHeight());
        final GraphicsContext gc = background_canvas.getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, background_canvas.getWidth(), background_canvas.getHeight());

        gc.setFill(this.color);
        gc.fillRect(
                rectangle.getX() * background_canvas.getWidth(),
                rectangle.getY() * background_canvas.getHeight(),
                rectangle.getWidth() * background_canvas.getWidth(),
                rectangle.getHeight() * background_canvas.getHeight()
        );
    }

    public void setColor(final Color color){
        this.color = color;
    }
}

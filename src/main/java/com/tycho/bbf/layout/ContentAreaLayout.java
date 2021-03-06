package com.tycho.bbf.layout;

import com.tycho.bbf.Utils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class ContentAreaLayout {

    @FXML
    private Canvas background_canvas;

    @FXML
    private BorderPane border_pane;

    @FXML
    private Label top;

    @FXML
    private Label left;

    @FXML
    private Label content_size;

    @FXML
    private Label right;

    @FXML
    private Label bottom;

    private int maxContentWidth = 0;
    private int maxContentHeight = 0;

    private Color color = Color.RED;

    private final Rectangle rectangle = new Rectangle();

    @FXML
    private void initialize() {
        border_pane.setMinWidth(background_canvas.getWidth());
        border_pane.setMaxWidth(background_canvas.getWidth());
        border_pane.setPrefWidth(background_canvas.getWidth());
        background_canvas.widthProperty().addListener((observable, oldValue, newValue) -> {
            border_pane.setMinWidth((double) newValue);
            border_pane.setMaxWidth((double) newValue);
            border_pane.setPrefWidth((double) newValue);
            draw();
        });
        background_canvas.heightProperty().addListener((observable, oldValue, newValue) -> {
            border_pane.setMinHeight((double) newValue);
            border_pane.setMaxHeight((double) newValue);
            border_pane.setPrefHeight((double) newValue);
            draw();
        });

        Utils.drawCheckerboard(background_canvas, 16);
    }

    public void setCanvasSize(final double width, final double height) {
        this.background_canvas.setWidth(width);
        this.background_canvas.setHeight(height);
    }

    public void setMaxContentSize(final int width, final int height) {
        maxContentWidth = width;
        maxContentHeight = height;
    }

    public void setContentSize(final int width, final int height) {
        this.content_size.setText("(" + width + " x " + height + ")");
    }

    public void setMargins(final int top, final int left, final int right, final int bottom) {
        this.top.setText(String.valueOf(top));
        this.left.setText(String.valueOf(left));
        this.right.setText(String.valueOf(right));
        this.bottom.setText(String.valueOf(bottom));

        final double t = (float) top / maxContentHeight;
        final double l = (float) left / maxContentWidth;
        final double r = (float) right / maxContentWidth;
        final double b = (float) bottom / maxContentHeight;

        this.rectangle.setX(l);
        this.rectangle.setY(t);
        this.rectangle.setWidth(1.0 - r - l);
        this.rectangle.setHeight(1.0 - b - t);

        draw();
    }

    private void draw() {
        final GraphicsContext gc = background_canvas.getGraphicsContext2D();

        //Resize
        final double ratio = (double) maxContentWidth / maxContentHeight;
        final double w = Math.min((int) (background_canvas.getHeight() * ratio), background_canvas.getWidth());
        final double h = Math.min((int) (background_canvas.getWidth() * (1 / ratio)), background_canvas.getHeight());
        border_pane.setPrefSize(w, h);
        border_pane.setMinSize(w, h);
        border_pane.setMaxSize(w, h);

        Utils.drawCheckerboard(background_canvas, 16);

        gc.save();
        gc.translate((background_canvas.getWidth() - w) / 2, (background_canvas.getHeight() - h) / 2);
        gc.scale(w / background_canvas.getWidth(), h / background_canvas.getHeight());

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, background_canvas.getWidth(), background_canvas.getHeight());

        gc.setFill(this.color);
        gc.fillRect(
                rectangle.getX() * background_canvas.getWidth(),
                rectangle.getY() * background_canvas.getHeight(),
                rectangle.getWidth() * background_canvas.getWidth(),
                rectangle.getHeight() * background_canvas.getHeight()
        );

        gc.restore();
    }

    public void setColor(final Color color) {
        this.color = color;
    }
}

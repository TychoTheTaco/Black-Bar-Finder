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
    private Label top;

    @FXML
    private Label left;

    @FXML
    private Label right;

    @FXML
    private Label bottom;

    @FXML
    private void initialize() {
        System.out.println("Initialize ContentAreaLayout!");
    }

    public void setMargins(final int top, final int left, final int right, final int bottom){
        this.top.setText(String.valueOf(top));
        this.left.setText(String.valueOf(left));
        this.right.setText(String.valueOf(right));
        this.bottom.setText(String.valueOf(bottom));

        StackPane.setMargin(borderPane, new Insets((top / 1080f) * root.getHeight(), (right / 1920f) * root.getHeight(), (bottom / 1080f) * root.getHeight(), (left / 1920f) * root.getHeight()));
        final Insets margins = StackPane.getMargin(borderPane);
        System.out.println("Margins: " + margins);
        System.out.println("Margin total: " + (margins.getTop() + margins.getBottom()));
        System.out.println("Pane size: " + root.getWidth() + " by " + root.getHeight());
    }

    public void setStyle(final String style){
        borderPane.setStyle(style);
    }
}

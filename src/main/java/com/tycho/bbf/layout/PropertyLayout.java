package com.tycho.bbf.layout;

import com.tycho.bbf.ContentFinder;
import com.tycho.bbf.Utils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PropertyLayout {

    @FXML
    private Label title;

    @FXML
    private Label value_label;

    @FXML
    private Slider slider;

    @FXML
    private void initialize() {
        slider.valueProperty().addListener((observable, oldValue, newValue) -> value_label.setText(String.valueOf(newValue)));
    }

    public void setProperty(final ContentFinder.RangedProperty property){
        title.setText(property.getTag());
        value_label.setText(String.valueOf(property.getValue()));
        slider.setMin(property.getMin().doubleValue());
        slider.setMax(property.getMax().doubleValue());
    }

    public Slider getSlider() {
        return slider;
    }
}

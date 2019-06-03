package com.tycho.bbf.layout;

import com.tycho.bbf.ContentFinder;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class PropertyLayout {

    @FXML
    private Label title;

    @FXML
    private Label value_label;

    @FXML
    private Slider slider;

    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("#0.0000");

    @FXML
    private void initialize() {
        NUMBER_FORMAT.setMinimumFractionDigits(4);
    }

    public void setProperty(final ContentFinder.RangedProperty property){
        title.setText(property.getName());

        //Set slider properties
        slider.setMin(property.getMin().doubleValue());
        slider.setMax(property.getMax().doubleValue());
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (property.getValue() instanceof Integer){
                value_label.setText(String.valueOf(newValue.intValue()));
            }else{
                value_label.setText(NUMBER_FORMAT.format(newValue));
            }
        });
        slider.setValue(property.getValue().doubleValue());

        if (property.getValue() instanceof Integer){
            slider.setMajorTickUnit(1);
            slider.setMinorTickCount(0);
            slider.setBlockIncrement(1.0);
        }else{
            slider.setBlockIncrement(0.01);
        }
    }

    public Slider getSlider() {
        return slider;
    }
}

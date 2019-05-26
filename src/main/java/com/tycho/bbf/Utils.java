package com.tycho.bbf;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;

public class Utils {

    public static boolean pom(double a, double b, double margin){
        return Math.abs(a - b) <= margin;
    }

    public static boolean almostEqual(final Color a, final Color b, final double sens){
        if (!pom(a.getRed(), b.getRed(), sens)){
            return false;
        }
        if (!pom(a.getGreen(), b.getGreen(), sens)){
            return false;
        }
        if (!pom(a.getBlue(), b.getBlue(), sens)){
            return false;
        }
        return true;
    }

    private static final ImageView imageView = new ImageView();

    public static Image scale(Image source, int targetWidth, int targetHeight, boolean preserveRatio) {
        imageView.setImage(source);
        imageView.setPreserveRatio(preserveRatio);
        imageView.setFitWidth(targetWidth);
        imageView.setFitHeight(targetHeight);
        return imageView.snapshot(null, null);
    }
}

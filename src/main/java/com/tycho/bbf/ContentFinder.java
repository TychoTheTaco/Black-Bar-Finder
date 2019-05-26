package com.tycho.bbf;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;

public abstract class ContentFinder {

    //Debug points
    //public Point2D a = new Point2D(0, 0);
    //public Point2D b = new Point2D(0, 0);

    public abstract Rectangle findContent(final Image frame);
}

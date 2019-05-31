package com.tycho.bbf;

import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.tycho.bbf.Utils.almostEqual;

public class DefaultContentFinder extends ContentFinder{

    //Decrease for higher accuracy, Increase for more speed
    private static final int MAX_X_SKIP = 4;
    private static final int MAX_Y_SKIP = 4;

    //Maximum difference allowed between neighboring pixels before it is considered part of the content area.
    private static final float THRESHOLD = 0.02f;

    @Override
    public Rectangle findContent(Image frame) {
        final int WIDTH = (int) frame.getWidth();
        final int HEIGHT = (int) frame.getHeight();

        final PixelReader pixelReader = frame.getPixelReader();

        int minX = WIDTH;
        int minY = HEIGHT;
        Color initialColor = pixelReader.getColor(0, 0);
        boolean hit = false;
        int xSkip = MAX_X_SKIP;
        int ySkip = MAX_Y_SKIP;

        //Search from top-left
        for (int y = 0; y < HEIGHT; y += ySkip){
            for (int x = 0; x < minX; x += xSkip){
                final Color color = pixelReader.getColor(x, y);
                if (!almostEqual(initialColor, color, THRESHOLD)){

                    if (ySkip > 1){
                        y -= ySkip;
                        if (y < 0) y = 0;
                        ySkip = 1;
                        break;
                    }

                    if (xSkip > 1){
                        x -= xSkip;
                        if (x < 0) x = 0;
                        xSkip = 1;
                        continue;
                    }

                    //New minimum found
                    minX = x;
                    if (y < minY) minY = y;
                    xSkip = MAX_X_SKIP;
                    ySkip = MAX_Y_SKIP;

                    //Debug
                    /*if (!hit){
                        a = new Point2D(x, y);
                        hit = true;
                    }*/
                }
            }
        }

        int maxX = 0;
        int maxY = 0;
        initialColor = pixelReader.getColor(WIDTH - 1, HEIGHT - 1);
        hit = false;
        xSkip = MAX_X_SKIP;
        ySkip = MAX_Y_SKIP;

        //Search from bottom-right
        for (int y = HEIGHT - 1; y >= 0; y -= ySkip){
            for (int x = WIDTH - 1; x >= maxX; x -= xSkip){
                final Color color = pixelReader.getColor(x, y);
                if (!almostEqual(initialColor, color, THRESHOLD)){

                    if (ySkip > 1){
                        y += ySkip;
                        if (y > HEIGHT - 1) y = HEIGHT - 1;
                        ySkip = 1;
                        break;
                    }

                    if (xSkip > 1){
                        x += xSkip;
                        if (x > WIDTH) x = WIDTH - 1;
                        xSkip = 1;
                        continue;
                    }

                    if (x > maxX) maxX = x;
                    if (y > maxY) maxY = y;
                    xSkip = MAX_X_SKIP;
                    ySkip =  MAX_Y_SKIP;

                    //Debug
                    /*if (!hit){
                        b = new Point2D(x, y);
                        hit = true;
                    }*/
                }
            }
        }

        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
}

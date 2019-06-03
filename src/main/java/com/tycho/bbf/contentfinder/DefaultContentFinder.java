package com.tycho.bbf.contentfinder;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.tycho.bbf.Utils.almostEqual;

public class DefaultContentFinder extends ContentFinder{

    public DefaultContentFinder(){
        //Decrease for higher accuracy, Increase for more speed
        addProperty("max_x_skip", new RangedProperty("Max X skip", 4, 1, 64));
        addProperty("max_y_skip", new RangedProperty("Max Y skip", 4, 1, 64));

        //Maximum difference allowed between neighboring pixels before it is considered part of the content area.
        addProperty("threshold", new RangedProperty("Color Threshold", 0.02f, 0, 1));
    }

    @Override
    public Rectangle findContent(Image frame) {
        final int WIDTH = (int) frame.getWidth();
        final int HEIGHT = (int) frame.getHeight();

        final PixelReader pixelReader = frame.getPixelReader();

        //Read properties
        final int MAX_X_SKIP = ((Number) getProperties().get("max_x_skip").getValue()).intValue();
        final int MAX_Y_SKIP = ((Number) getProperties().get("max_y_skip").getValue()).intValue();
        final float THRESHOLD = ((Number) getProperties().get("threshold").getValue()).floatValue();

        int minX = WIDTH - 1;
        int minY = HEIGHT - 1;
        Color initialColor = pixelReader.getColor(0, 0);
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
                }
            }
        }

        int maxX = 0;
        int maxY = 0;
        initialColor = pixelReader.getColor(WIDTH - 1, HEIGHT - 1);
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
                }
            }
        }

        if (maxX - minY + 1 <= 0){
            return new Rectangle();
        }
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
}

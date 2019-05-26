package com.tycho.bbf;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

import static com.tycho.bbf.Utils.almostEqual;

public class LineAverageContentFinder extends ContentFinder {

    //Decrease for higher accuracy, Increase for more speed
    private static final int MAX_X_SKIP = 16;
    private static final int MAX_Y_SKIP = 16;

    //Maximum difference allowed between neighboring pixels before it is considered part of the content area.
    private static final float THRESHOLD = 0.01f;

    //Percentage of pixels per line that need to be considered 'content' before the line is considered content.
    private static final double AVG_THRESHOLD = 0.2;

    @Override
    public Rectangle findContent(Image frame) {
        final int WIDTH = (int) frame.getWidth();
        final int HEIGHT = (int) frame.getHeight();

        final PixelReader pixelReader = frame.getPixelReader();

        final List<Boolean> colors = new ArrayList<>();

        int top = 0;
        int bottom = HEIGHT;
        int left = 0;
        int right = WIDTH;

        int xSkip = 1;
        int ySkip = MAX_Y_SKIP;

        //Search from top to bottom
        for (int y = -1; y < HEIGHT - ySkip; y += ySkip){
            colors.clear();
            for (int x = 0; x < WIDTH; x += xSkip){
                final Color a = y < 0 ? Color.BLACK : pixelReader.getColor(x, y);
                final Color b = pixelReader.getColor(x, y + ySkip);
                colors.add(avg(a) < avg(b));
            }
            final double average = average(colors);
            //if (average > 0) System.out.println("Average difference of (" + y + " vs " + (y + ySkip) + "): " + average + " (" + colors.size() + " items)");
            if (average >= AVG_THRESHOLD){

                if (ySkip > 1){
                    y -= ySkip;
                    if (y < -1) y = -2;
                    ySkip = 1;
                    continue;
                }

                top = y + ySkip;
                break;
            }
        }

        xSkip = 1;
        ySkip = MAX_Y_SKIP;

        //Search from bottom to top
        for (int y = HEIGHT; y > ySkip; y -= ySkip){
            colors.clear();
            for (int x = 0; x < WIDTH; x += xSkip){
                final Color a = y >= HEIGHT ? Color.BLACK : pixelReader.getColor(x, y);
                final Color b = pixelReader.getColor(x, y - ySkip);
                colors.add(avg(a) < avg(b));
            }
            final double average = average(colors);
            //if (average > 0) System.out.println("Average difference of line (" + y + " vs " + (y - ySkip) + "): " + average + " (" + colors.size() + " items)");
            if (average >= AVG_THRESHOLD){

                if (ySkip > 1){
                    y += ySkip;
                    if (y > HEIGHT) y = HEIGHT + 1;
                    ySkip = 1;
                    continue;
                }

                bottom = y - ySkip;
                break;
            }
        }

        xSkip = MAX_X_SKIP;
        ySkip = 1;

        //Search from left to right
        for (int x = -1; x < WIDTH - xSkip; x += xSkip){
            colors.clear();
            for (int y = 0; y < HEIGHT; y += ySkip){
                final Color a = x < 0 ? Color.BLACK : pixelReader.getColor(x, y);
                final Color b = pixelReader.getColor(x + xSkip, y);
                colors.add(avg(a) < avg(b));
            }
            final double average = average(colors);
            //if (average > 0) System.out.println("Average difference of (" + x + " vs " + (x + xSkip) + "): " + average + " (" + colors.size() + " items)");
            if (average >= AVG_THRESHOLD){

                if (xSkip > 1){
                    x -= xSkip;
                    if (x < -1) x = -2;
                    xSkip = 1;
                    continue;
                }

                left = x + xSkip;
                break;
            }
        }

        xSkip = MAX_X_SKIP;
        ySkip = 1;

        //Search from right to left
        for (int x = WIDTH; x > xSkip; x -= xSkip){
            colors.clear();
            for (int y = 0; y < HEIGHT; y += ySkip){
                final Color a = x >= WIDTH ? Color.BLACK : pixelReader.getColor(x, y);
                final Color b = pixelReader.getColor(x - xSkip, y);
                colors.add(avg(a) < avg(b));
            }
            final double average = average(colors);
            //if (average > 0) System.out.println("Average of column " + x + " is " + average);
            if (average >= AVG_THRESHOLD){

                if (xSkip > 1){
                    x += xSkip;
                    if (x > WIDTH) x = WIDTH + 1;
                    xSkip = 1;
                    continue;
                }

                right = x - xSkip;
                break;
            }
        }

        return new Rectangle(left, top, right - left + 1, bottom - top + 1);
    }

    private double avg(final Color color){
        return (color.getRed() + color.getGreen() + color.getBlue()) / 3;
    }

    private static double average(final boolean[] numbers){
        double total = 0;
        for (boolean n : numbers){
            total += n ? 1 : 0;
        }
        return total / numbers.length;
    }

    private static double average(final List<Boolean> numbers){
        double total = 0;
        for (boolean n : numbers){
            total += n ? 1 : 0;
        }
        return total / numbers.size();
    }
}

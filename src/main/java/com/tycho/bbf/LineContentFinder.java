package com.tycho.bbf;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Effect;
import javafx.scene.image.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

import static com.tycho.bbf.Utils.almostEqual;

public class LineContentFinder extends ContentFinder {

    //Decrease for higher accuracy, Increase for more speed
    private static final int MAX_X_SKIP = 16;
    private static final int MAX_Y_SKIP = 4;

    //Maximum difference allowed between neighboring pixels before it is considered part of the content area.
    private static final float THRESHOLD = 0.003f;

    //Percentage of pixels per line that need to be considered 'content' before the line is considered content.
    private static final double AVG_THRESHOLD = 0.2;

    private static final double LINE_LENGTH_THRESHOLD = 0.015;

    public List<Line> debug_lines = new ArrayList<>();

    private Image threshold = null;

    @Override
    public Rectangle findContent(Image frame) {
        final int WIDTH = (int) frame.getWidth();
        final int HEIGHT = (int) frame.getHeight();
        debug_lines.clear();

        int top = 0;
        int bottom = HEIGHT;
        int left = 0;
        int right = WIDTH;

        int ySkip = MAX_Y_SKIP;

        final PixelReader pixelReader = frame.getPixelReader();

        //Search from top to bottom
        for (int y = 0; y < HEIGHT - ySkip; y += ySkip){
            int xSkip = MAX_X_SKIP;
            int start = 0;
            boolean started = false;
            for (int x = 0; x < WIDTH - 1; x += xSkip){
                if ((avg(pixelReader.getColor(x, y)) > IMG_THRESHOLD)){
                    if (!started){
                        start = x;
                        started = true;
                    }
                }else{
                    if (started){

                        /*if (ySkip > 1){
                            y -= ySkip;
                            if (y < 0) y = 0;
                            ySkip = 1;
                            continue;
                        }*/

                        if (xSkip > 1){
                            x = start;
                            if (x < 0) x = 0;
                            xSkip = 1;
                            continue;
                        }

                        final Line line = new Line(start, y, x, y);
                        debug_lines.add(line);

                        if (line.length() >= WIDTH * LINE_LENGTH_THRESHOLD){
                            System.out.println("Hit threshold at "  + x + ", " + y + " with length " + line.length());
                            top = y;
                            break;
                        }

                        start = x + 1;
                        started = false;
                    }
                }
                if (top > 0) break;
            }
        }

        ySkip = MAX_Y_SKIP;

        //Search from bottom to top
        for (int y = HEIGHT - 1; y > ySkip; y -= ySkip){
            int start = 0;
            boolean started = false;
            int xSkip = MAX_X_SKIP;
            for (int x = 0; x < WIDTH - 1; x += xSkip){
                if ((avg(pixelReader.getColor(x, y)) > IMG_THRESHOLD)){
                    if (!started){
                        start = x;
                        started = true;
                    }
                }else{
                    if (started){

                        if (xSkip > 1){
                            x = start;
                            if (x < 0) x = 0;
                            xSkip = 1;
                            continue;
                        }

                        final Line line = new Line(start, y, x, y);
                        debug_lines.add(line);

                        if (line.length() >= WIDTH * LINE_LENGTH_THRESHOLD){
                            System.out.println("Hit threshold at "  + x + ", " + y + " with length " + line.length());
                            bottom = y;
                            break;
                        }

                        start = x + 1;
                        started = false;
                    }
                }
                if (bottom < HEIGHT) break;
            }
        }

        int xSkip = MAX_Y_SKIP;

        //Search from left to right
        for (int x = 0; x < WIDTH - xSkip; x += xSkip){
            int start = 0;
            boolean started = false;
            ySkip = MAX_X_SKIP;
            final List<Line> lines = new ArrayList<>();
            for (int y = 0; y < HEIGHT - 1; y += ySkip){
                if ((avg(pixelReader.getColor(x, y)) > IMG_THRESHOLD)){
                    if (!started){
                        start = y;
                        started = true;
                    }
                }else{
                    if (started){

                        if (ySkip > 1){
                            y = start;
                            if (y < 0) y = 0;
                            ySkip = 1;
                            continue;
                        }

                        lines.add(new Line(x, start, x, y));
                        debug_lines.add(new Line(x, start, x, y));
                        start = y + 1;
                        started = false;
                    }
                }
            }
            if (started){
                lines.add(new Line(x, start, x, HEIGHT - 1));
                debug_lines.add(new Line(x, start, x, HEIGHT - 1));
            }
            final Line largest = findLargest(lines);
            if (largest != null){
                if (largest.length() >= HEIGHT * LINE_LENGTH_THRESHOLD){

                    if (xSkip > 1){
                        x -= xSkip;
                        if (x < 0) x = 0;
                        xSkip = 1;
                        continue;
                    }

                    left = x;
                    break;
                }
            }
        }

        xSkip = MAX_Y_SKIP;

        //Search from left to right
        for (int x = WIDTH - 1; x > xSkip; x -= xSkip){
            int start = 0;
            boolean started = false;
            ySkip = MAX_X_SKIP;
            final List<Line> lines = new ArrayList<>();
            for (int y = 0; y < HEIGHT - 1; y += ySkip){
                if ((avg(pixelReader.getColor(x, y)) > IMG_THRESHOLD)){
                    if (!started){
                        start = y;
                        started = true;
                    }
                }else{
                    if (started){

                        if (ySkip > 1){
                            y = start;
                            if (y < 0) y = 0;
                            ySkip = 1;
                            continue;
                        }

                        lines.add(new Line(x, start, x, y));
                        debug_lines.add(new Line(x, start, x, y));
                        start = y + 1;
                        started = false;
                    }
                }
            }
            if (started){
                lines.add(new Line(x, start, x, HEIGHT - 1));
                debug_lines.add(new Line(x, start, x, HEIGHT - 1));
            }
            final Line largest = findLargest(lines);
            if (largest != null){
                if (largest.length() >= HEIGHT * LINE_LENGTH_THRESHOLD){

                    if (xSkip > 1){
                        x += xSkip;
                        if (x >= WIDTH) x = WIDTH - 1;
                        xSkip = 1;
                        continue;
                    }

                    right = x;
                    break;
                }
            }
        }

        return new Rectangle(left, top, right - left + 1, bottom - top + 1);
    }

    private double avg(final Color color){
        return (color.getRed() + color.getGreen() + color.getBlue()) / 3;
    }

    public Line findLargest(final List<Line> lines){
        Line largest = null;
        for (Line line : lines){
            if (largest == null || line.length() > largest.length()){
                largest = line;
            }
        }
        return largest;
    }

    public static class Line{
        public int x1, y1;
        public int x2, y2;

        public Line(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        public double length(){
            return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
        }
    }

    private static double IMG_THRESHOLD = 0.01;

    public void drawDebug(final GraphicsContext gc, boolean drawLines){
        boolean alternate = true;

        if (threshold != null) gc.drawImage(this.threshold, 0, 0);

        if (drawLines){
            for (Line line : debug_lines){
                gc.setStroke(alternate ? Color.RED: Color.LIGHTGREEN);
                if (line.length() >= 1080 * LINE_LENGTH_THRESHOLD) gc.setStroke(Color.YELLOW);
                gc.strokeLine(line.x1 + 0.5, line.y1 + 0.5, line.x2 + 0.5, line.y2 + 0.5);
                alternate = !alternate;
            }
        }
    }

    private Image threshold(final Image image, final double threshold){
        final WritableImage writableImage = new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());
        final PixelReader pixelReader = writableImage.getPixelReader();
        final PixelWriter pixelWriter = writableImage.getPixelWriter();
        for (int y = 0; y < writableImage.getHeight(); ++y) {
            for (int x = 0; x < writableImage.getWidth(); ++x) {
                final Color color = pixelReader.getColor(x, y);
                pixelWriter.setColor(x, y, avg(color) <= threshold ? Color.BLACK : Color.WHITE);
            }
        }
        return writableImage;
    }
}

package com.tycho.bbf.contentfinder;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class StrictLineContentFinder extends ContentFinder implements Debuggable{

    //Decrease for higher accuracy, Increase for more speed
    private static final int MAX_X_SKIP = 8;
    private static final int MAX_Y_SKIP = 4;

    /**
     * The minimum value for a pixel to be considered 'content'. This value is compared against the average of a pixel's RGB values.
     */
    private static final double IMG_THRESHOLD = 0.01;

    /**
     * Minimum line length for a piece of content to be considered important. This value represents a percentage of the source width.
     */
    private static final double LINE_LENGTH_THRESHOLD = 0.015;

    private static final int REQUIRED_CONSECUTIVE = 5;

    private List<Line> debug_lines = new ArrayList<>();

    private Image image = null;

    @Override
    public Rectangle findContent(Image frame) {
        final int WIDTH = (int) frame.getWidth();
        final int HEIGHT = (int) frame.getHeight();
        debug_lines.clear();

        image = frame;

        int ySkip = MAX_Y_SKIP;

        final PixelReader pixelReader = frame.getPixelReader();

        //System.out.println("----------- Find Content -----------");

        //Search from top to bottom
        int lines = 0;
        for (int y = 0; y < HEIGHT - ySkip; y += ySkip){
            int start = 0;
            boolean started = false;
            int xSkip = MAX_X_SKIP;
            boolean hit = false;
            for (int x = 0; x < WIDTH - 1; x += xSkip){
                if (started){
                    final Line line = new Line(start, y, x, y);
                    if (line.length() >= WIDTH * LINE_LENGTH_THRESHOLD){
                        if (xSkip > 1){
                            x = start;
                            if (x < 0) x = 0;
                            xSkip = 1;
                            continue;
                        }
                        //System.out.println("Hit TOP threshold at "  + x + ", " + y + " with length " + line.length());
                        debug_lines.add(line);
                        lines++;
                        started = false;
                        hit = true;
                        break;
                    }
                }
                if ((avg(pixelReader.getColor(x, y)) > IMG_THRESHOLD)){
                    if (!started){
                        start = x;
                        started = true;
                    }
                }else{
                    if (started){
                        start = x + 1;
                        started = false;
                    }
                }
            }
            if (started){
                final Line line = new Line(start, y, WIDTH - 1, y);
                if (line.length() >= WIDTH * LINE_LENGTH_THRESHOLD){
                    //System.out.println("Hit TOP threshold at "  + (WIDTH - 1) + ", " + y + " with length " + line.length());
                    debug_lines.add(line);
                    lines++;
                    hit = true;
                }
            }
            if (!hit){
                for (int i = 0; i < lines; ++i){
                    debug_lines.remove(debug_lines.size() - 1);
                }
                lines = 0;
            }
            if (lines >= REQUIRED_CONSECUTIVE) break;
        }
        if (lines < REQUIRED_CONSECUTIVE){
            for (int i = 0; i < lines; ++i){
                debug_lines.remove(debug_lines.size() - 1);
            }
        }

       ySkip = MAX_Y_SKIP;

        //Search from bottom to top
        lines = 0;
        for (int y = HEIGHT - 1; y > ySkip; y -= ySkip){
            int start = 0;
            boolean started = false;
            int xSkip = MAX_X_SKIP;
            boolean hit = false;
            for (int x = 0; x < WIDTH - 1; x += xSkip){
                if (started){
                    final Line line = new Line(start, y, x, y);
                    if (line.length() >= WIDTH * LINE_LENGTH_THRESHOLD){
                        if (xSkip > 1){
                            x = start;
                            if (x < 0) x = 0;
                            xSkip = 1;
                            continue;
                        }
                        //System.out.println("Hit BOTTOM threshold at "  + x + ", " + y + " with length " + line.length());
                        debug_lines.add(line);
                        lines++;
                        hit = true;
                        started = false;
                        break;
                    }
                }
                if ((avg(pixelReader.getColor(x, y)) > IMG_THRESHOLD)){
                    if (!started){
                        start = x;
                        started = true;
                    }
                }else{
                    if (started){
                        start = x + 1;
                        started = false;
                    }
                }
            }
            if (started){
                final Line line = new Line(start, y, WIDTH - 1, y);
                if (line.length() >= WIDTH * LINE_LENGTH_THRESHOLD){
                    //System.out.println("Hit BOTTOM threshold at "  + (WIDTH - 1) + ", " + y + " with length " + line.length());
                    debug_lines.add(line);
                    lines++;
                }
            }
            if (!hit){
                for (int i = 0; i < lines; ++i){
                    debug_lines.remove(debug_lines.size() - 1);
                }
                lines = 0;
            }
            if (lines >= REQUIRED_CONSECUTIVE) break;
        }

        int xSkip = MAX_Y_SKIP;

        //Search from left to right
        lines = 0;
        for (int x = 0; x < WIDTH - xSkip; x += xSkip){
            int start = 0;
            boolean started = false;
            ySkip = MAX_X_SKIP;
            boolean hit = false;
            for (int y = 0; y < HEIGHT - 1; y += ySkip){
                if (started){
                    final Line line = new Line(x, start, x, y);
                    if (line.length() >= HEIGHT * LINE_LENGTH_THRESHOLD){
                        if (ySkip > 1){
                            y = start;
                            if (y < 0) y = 0;
                            ySkip = 1;
                            continue;
                        }
                       // System.out.println("Hit LEFT threshold at "  + x + ", " + y + " with length " + line.length());
                        debug_lines.add(line);
                        lines++;
                        hit = true;
                        started = false;
                        break;
                    }
                }
                if ((avg(pixelReader.getColor(x, y)) > IMG_THRESHOLD)){
                    if (!started){
                        start = y;
                        started = true;
                    }
                }else{
                    if (started){
                        start = y + 1;
                        started = false;
                    }
                }
            }
            if (started){
                final Line line = new Line(x, start, x, HEIGHT - 1);
                if (line.length() >= HEIGHT * LINE_LENGTH_THRESHOLD){
                    //System.out.println("Hit LEFT threshold at "  + x + ", " + (HEIGHT - 1) + " with length " + line.length());
                    debug_lines.add(line);
                    lines++;
                }
            }
            if (!hit){
                for (int i = 0; i < lines; ++i){
                    debug_lines.remove(debug_lines.size() - 1);
                }
                lines = 0;
            }
            if (lines >= REQUIRED_CONSECUTIVE) break;
        }

        xSkip = MAX_Y_SKIP;

        //Search from right to left
        lines = 0;
        for (int x = WIDTH - 1; x > xSkip; x -= xSkip){
            int start = 0;
            boolean started = false;
            ySkip = MAX_X_SKIP;
            boolean hit = false;
            for (int y = 0; y < HEIGHT - 1; y += ySkip){
                if (started){
                    final Line line = new Line(x, start, x, y);
                    if (line.length() >= HEIGHT * LINE_LENGTH_THRESHOLD){
                        if (ySkip > 1){
                            y = start;
                            if (y < 0) y = 0;
                            ySkip = 1;
                            continue;
                        }
                        //System.out.println("Hit RIGHT threshold at "  + x + ", " + y + " with length " + line.length());
                        debug_lines.add(line);
                        lines++;
                        hit = true;
                        started = false;
                        break;
                    }
                }
                if ((avg(pixelReader.getColor(x, y)) > IMG_THRESHOLD)){
                    if (!started){
                        start = y;
                        started = true;
                    }
                }else{
                    if (started){
                        start = y + 1;
                        started = false;
                    }
                }
            }
            if (!hit){
                for (int i = 0; i < lines; ++i){
                    debug_lines.remove(debug_lines.size() - 1);
                }
                lines = 0;
            }
            if (started){
                final Line line = new Line(x, start, x, HEIGHT - 1);
                if (line.length() >= HEIGHT * LINE_LENGTH_THRESHOLD){
                    //System.out.println("Hit RIGHT threshold at "  + x + ", " + (HEIGHT - 1) + " with length " + line.length());
                    debug_lines.add(line);
                    lines++;
                }
            }
            if (lines >= REQUIRED_CONSECUTIVE) break;
        }

        if (debug_lines.size() < 3){
            return new Rectangle();
        }else{
            return boundingBox(debug_lines);
        }
    }

    private Rectangle boundingBox(final List<Line> lines){
        int minX = -1;
        int minY = -1;
        int maxX = -1;
        int maxY = -1;
        for (Line line : lines){
            if (minX == -1 || line.x1 < minX) minX = line.x1;
            if (line.x1 > maxX) maxX = line.x1;
            if (maxX == -1 || line.x2 < minX) minX = line.x2;
            if (line.x2 > maxX) maxX = line.x2;

            if (minY == -1 || line.y1 < minY) minY = line.y1;
            if (line.y1 > maxY) maxY = line.y1;
            if (maxY == -1 || line.y2 < minY) minY = line.y2;
            if (line.y2 > maxY) maxY = line.y2;
        }
        return new Rectangle(minX, minY, maxX -  minX + 1, maxY - minY + 1);
    }

    private double avg(final Color color){
        return (color.getRed() + color.getGreen() + color.getBlue()) / 3;
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

        @Override
        public String toString() {
            return "(" + x1 + ", " + y1 + ") -> (" + x2 + ", " + y2 + ") Length: " + length();
        }
    }

    @Override
    public void drawDebug(final GraphicsContext gc){
       // if (image != null) gc.drawImage(threshold(image, IMG_THRESHOLD), 0, 0);

        final boolean drawLines = true;
        if (drawLines){
            for (Line line : debug_lines){
                //System.out.println("Line: " + line);
                gc.setStroke( Color.RED);
                gc.strokeLine(line.x1 + 0.5, line.y1 + 0.5, line.x2 + 0.5, line.y2 + 0.5);
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

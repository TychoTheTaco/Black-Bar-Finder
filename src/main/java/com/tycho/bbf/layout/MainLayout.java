package com.tycho.bbf.layout;

import com.tycho.bbf.*;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class MainLayout {

    @FXML
    private Label frame_count_label;

    @FXML
    private Label processing_time_label;

    @FXML
    public Canvas video_canvas;

    @FXML
    public BorderPane content_size_pane;

    @FXML
    public BorderPane largest_content_size_pane;

    @FXML
    public BorderPane estimated_video_area_pane;

    private GraphicsContext gc;

    private final ContentFinder contentFinder = new LineContentFinder();

    private boolean overlay = true;
    private boolean forceSymmetrical = true;
    private boolean debug = true;

    private Rectangle maxContentBounds = null;
    private Rectangle videoBoundary = null;

    private Image image;

    @FXML
    private void initialize() {
        gc = video_canvas.getGraphicsContext2D();

        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, video_canvas.getWidth(), video_canvas.getHeight());

        video_canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                System.out.println("Click: " + event.getX() + " " + event.getY());
            }
        });
    }

    public void setFrame(final Image image) {
        this.image = image;

        //Scale image to fit canvas
        Image scaledImage = image;
        if (image.getWidth() != video_canvas.getWidth() || image.getHeight() != video_canvas.getHeight()) {
            System.out.println("Resizing image...");
            scaledImage = Utils.scale(image, (int) video_canvas.getWidth(), (int) video_canvas.getHeight(), true);
            System.out.println("Resized to " + image.getWidth() + " by " + image.getHeight());
        }

        //Clear canvas
        gc.clearRect(0, 0, video_canvas.getWidth(), video_canvas.getHeight());

        //Draw image
        gc.drawImage(scaledImage, 0, 0);

        //Find content bounds
        final long start = System.currentTimeMillis();
        final Rectangle contentBounds = contentFinder.findContent(image);
        System.out.println("Content: " + contentBounds);
        final long elapsed = System.currentTimeMillis() - start;
        //frameTimes.add(elapsed);

        setProcessingTime(elapsed);

        //GridPane.setMargin(content_size_pane, new Insets((contentBounds.getY() / image.getHeight()) * content_size_pane.getHeight(), 0, 0, (contentBounds.getX() * image.getWidth()) * content_size_pane.getHeight()));

        if (overlay) {
            //Scale to fit canvas
            gc.save();
            gc.scale(scaledImage.getWidth() / image.getWidth(), scaledImage.getHeight() / image.getHeight());

            //Draw content overlay
            gc.setFill(Color.RED);
            gc.setGlobalAlpha(0.25);
            gc.fillRect(contentBounds.getX(), contentBounds.getY(), contentBounds.getWidth(), contentBounds.getHeight());
            gc.setGlobalAlpha(1);

            gc.setLineWidth(1);

            //Draw maximum content bounds
            if (maxContentBounds == null) maxContentBounds = contentBounds;
            setIfLarger(contentBounds, maxContentBounds);
            gc.setStroke(Color.GREEN);
            gc.strokeRect(maxContentBounds.getX() + 0.5, maxContentBounds.getY() + 0.5, maxContentBounds.getWidth() - 1, maxContentBounds.getHeight() - 1);

            //Draw estimated video area
            if (videoBoundary == null) videoBoundary = new Rectangle(maxContentBounds.getX(), maxContentBounds.getY(), maxContentBounds.getWidth(), maxContentBounds.getHeight());
            setIfLarger(maxContentBounds, videoBoundary);

            //Make video area symmetrical
            if (forceSymmetrical){
                final double left = videoBoundary.getX();
                final double right = image.getWidth() - (videoBoundary.getX() + videoBoundary.getWidth());
                final double top = videoBoundary.getY();
                final double bottom = image.getHeight() - (videoBoundary.getY() + videoBoundary.getHeight());
                final double xDif = Math.abs(left - right);
                final double yDif = Math.abs(top - bottom);
                videoBoundary.setX(Math.min(left, right));
                videoBoundary.setWidth(videoBoundary.getWidth() + xDif);
                videoBoundary.setY(Math.min(top, bottom));
                videoBoundary.setHeight(videoBoundary.getHeight() + yDif);
            }
            gc.setStroke(Color.CYAN);
            gc.strokeRect(videoBoundary.getX() + 0.5, videoBoundary.getY() + 0.5, videoBoundary.getWidth() - 1, videoBoundary.getHeight() - 1);

            //Debug points
            //gc.setFill(Color.WHITE);
            //gc.fillRect(contentFinder.a.getX(), contentFinder.a.getY() - 5, 1, 10);
            //gc.fillRect(contentFinder.b.getX(), contentFinder.b.getY() - 5, 1, 10);

            gc.restore();
        }

        if (debug){
            if (contentFinder instanceof LineContentFinder){
                //Scale to fit canvas
                gc.save();
                gc.scale(scaledImage.getWidth() / image.getWidth(), scaledImage.getHeight() / image.getHeight());

                ((LineContentFinder) contentFinder).drawDebug(gc, overlay);

                //Restore scaling
                gc.restore();
            }
        }
    }

    private void setIfLarger(final Rectangle a, final Rectangle b){
        if (a.getX() < b.getX()) b.setX(a.getX());
        if (a.getY() < b.getY()) b.setY(a.getY());
        if (a.getX() + a.getWidth() > b.getX() + b.getWidth()) b.setWidth(a.getX() + a.getWidth() - b.getX());
        if (a.getY() + a.getHeight() > b.getY() + b.getHeight()) b.setHeight(a.getY() + a.getHeight() - b.getY());
    }

    public boolean getOverlay(){
        return this.overlay;
    }

    public void setOverlay(final boolean overlay){
        this.overlay = overlay;
        setFrame(this.image);
    }

    public boolean getDebug(){
        return this.debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        setFrame(this.image);
    }

    public void setFrameCount(final int frameNumber) {
        frame_count_label.setText("Frame: " + frameNumber);
    }

    public void setProcessingTime(final long processingTime) {
        processing_time_label.setText("Processed in " + processingTime + " ms.");
    }
}

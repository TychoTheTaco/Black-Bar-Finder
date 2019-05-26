package com.tycho.bbf.layout;

import com.tycho.bbf.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
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
    public StackPane content_size_pane;

    @FXML
    public StackPane largest_content_size_pane;

    @FXML
    public StackPane estimated_video_area_pane;

    @FXML
    private ContentAreaLayout content_size_paneController;

    @FXML
    private ContentAreaLayout largest_content_size_paneController;

    @FXML
    private ContentAreaLayout estimated_video_area_paneController;

    @FXML
    private Label content_size_label;

    @FXML
    private Label largest_content_size_label;

    @FXML
    private Label estimated_video_area_label;

    @FXML
    private Label cursor_position_label;

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
        video_canvas.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                final int x = (int) (image == null ? event.getX() : ((event.getX() / video_canvas.getWidth()) * image.getWidth()));
                final int y = (int) (image == null ? event.getY() : ((event.getY() / video_canvas.getHeight()) * image.getHeight()));
                cursor_position_label.setText("Position: (" + x + ", " + y + ")");
            }
        });

        System.out.println("Current size: " + content_size_pane.getWidth() + " by " + content_size_pane.getHeight());
        int w = (int) (video_canvas.getWidth() / 8);
        int h = (int) ((9f / 16) * w);
        System.out.println("Calculated size: " + w + " by " + h);
        setPrefAndMax(content_size_pane, w, h);
        setPrefAndMax(largest_content_size_pane, w, h);
        setPrefAndMax(estimated_video_area_pane, w, h);

        content_size_paneController.setColor(Color.RED);
        largest_content_size_paneController.setColor(Color.GREEN);
        estimated_video_area_paneController.setColor(Color.BLUE);
    }

    private void setPrefAndMax(final Pane pane, final int width, final int height){
        pane.setPrefWidth(width);
        pane.setMaxWidth(width);
        pane.setPrefHeight(height);
        pane.setMaxHeight(height);
    }

    public void setFrame(final Image image) {
        this.image = image;

        System.out.println("New size: " + content_size_pane.getWidth() + " by " + content_size_pane.getHeight());

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

        //Calculate maximum content bounds
        if (maxContentBounds == null) maxContentBounds = contentBounds;
        setIfLarger(contentBounds, maxContentBounds);

        //Calculate video boundary
        if (videoBoundary == null) videoBoundary = new Rectangle(maxContentBounds.getX(), maxContentBounds.getY(), maxContentBounds.getWidth(), maxContentBounds.getHeight());
        setIfLarger(maxContentBounds, videoBoundary);
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
            gc.setStroke(Color.GREEN);
            gc.strokeRect(maxContentBounds.getX() + 0.5, maxContentBounds.getY() + 0.5, maxContentBounds.getWidth() - 1, maxContentBounds.getHeight() - 1);

            //Draw video boundary
            gc.setStroke(Color.CYAN);
            gc.strokeRect(videoBoundary.getX() + 0.5, videoBoundary.getY() + 0.5, videoBoundary.getWidth() - 1, videoBoundary.getHeight() - 1);

            gc.restore();
        }

        content_size_label.setText("Content size: " + (int) contentBounds.getWidth() + " by " + (int) contentBounds.getHeight());
        content_size_paneController.setMargins(
                (int) contentBounds.getY(),
                (int) contentBounds.getX(),
                (int) (image.getWidth() - (contentBounds.getX() + contentBounds.getWidth())),
                (int) (image.getHeight() - (contentBounds.getY() + contentBounds.getHeight()))
        );
        if (maxContentBounds != null){
            largest_content_size_label.setText("Largest content size: " + (int) maxContentBounds.getWidth() + " by " + (int) maxContentBounds.getHeight());
            largest_content_size_paneController.setMargins(
                    (int) maxContentBounds.getY(),
                    (int) maxContentBounds.getX(),
                    (int) (image.getWidth() - (maxContentBounds.getX() + maxContentBounds.getWidth())),
                    (int) (image.getHeight() - (maxContentBounds.getY() + maxContentBounds.getHeight()))
            );
        }
        if (videoBoundary != null){
            estimated_video_area_label.setText("Video boundary: " + (int) videoBoundary.getWidth() + " by " + (int) videoBoundary.getHeight());
            estimated_video_area_paneController.setMargins(
                    (int) videoBoundary.getY(),
                    (int) videoBoundary.getX(),
                    (int) (image.getWidth() - (videoBoundary.getX() + videoBoundary.getWidth())),
                    (int) (image.getHeight() - (videoBoundary.getY() + videoBoundary.getHeight()))
            );
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
        if (a.getWidth() + a.getHeight() == 0) return;
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

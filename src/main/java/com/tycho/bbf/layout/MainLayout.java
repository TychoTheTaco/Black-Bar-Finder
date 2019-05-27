package com.tycho.bbf.layout;

import com.tycho.bbf.*;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
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
    public Pane content_size_pane;

    @FXML
    public Pane largest_content_size_pane;

    @FXML
    public Pane estimated_video_area_pane;

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

    @FXML
    private CheckBox overlay_checkbox;

    @FXML
    private CheckBox debug_checkbox;

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

        //Set up video canvas
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, video_canvas.getWidth(), video_canvas.getHeight());
        video_canvas.setOnMouseMoved(event -> {
            final int x = (int) (image == null ? event.getX() : ((event.getX() / video_canvas.getWidth()) * image.getWidth()));
            final int y = (int) (image == null ? event.getY() : ((event.getY() / video_canvas.getHeight()) * image.getHeight()));
            cursor_position_label.setText("Position: (" + x + ", " + y + ")");
        });

        //Set up
        content_size_paneController.setColor(Color.RED);
        largest_content_size_paneController.setColor(Color.GREEN);
        estimated_video_area_paneController.setColor(Color.BLUE);

        //Set up flags
        overlay_checkbox.setSelected(overlay);
        debug_checkbox.setSelected(debug);
    }

    private void resizeCanvas(final Image image) {
        int w = (int) (video_canvas.getWidth() / 8);
        int h = (int) ((image.getHeight() / image.getWidth()) * w);
        content_size_paneController.setCanvasSize(w, h);
        largest_content_size_paneController.setCanvasSize(w, h);
        estimated_video_area_paneController.setCanvasSize(w, h);
    }

    public void setFrame(final Image image) {
        this.image = image;
        resizeCanvas(image);

        //Scale image to fit canvas
        Image scaledImage = image;
        if (image.getWidth() != video_canvas.getWidth() || image.getHeight() != video_canvas.getHeight()) {
            final long start = System.currentTimeMillis();
            scaledImage = Utils.scale(image, (int) video_canvas.getWidth(), (int) video_canvas.getHeight(), true);
            System.out.println("Resized to " + image.getWidth() + " by " + image.getHeight() + " in " + (System.currentTimeMillis() - start) + " ms.");
        }

        //Clear canvas
        gc.clearRect(0, 0, video_canvas.getWidth(), video_canvas.getHeight());

        //Draw image
        gc.drawImage(scaledImage, 0, 0);

        //Find content bounds
        final long start = System.currentTimeMillis();
        final Rectangle contentBounds = contentFinder.findContent(image);
        final long elapsed = System.currentTimeMillis() - start;
        //frameTimes.add(elapsed);

        processing_time_label.setText("Processed in " + elapsed + " ms.");

        //Calculate maximum content bounds
        if (maxContentBounds == null) maxContentBounds = contentBounds;
        setIfLarger(contentBounds, maxContentBounds);

        //Calculate video boundary
        if (videoBoundary == null) videoBoundary = new Rectangle(maxContentBounds.getX(), maxContentBounds.getY(), maxContentBounds.getWidth(), maxContentBounds.getHeight());
        setIfLarger(maxContentBounds, videoBoundary);
        if (forceSymmetrical) {
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
        content_size_paneController.setContentSize((int) image.getWidth(), (int) image.getHeight());
        content_size_paneController.setMargins(
                (int) contentBounds.getY(),
                (int) contentBounds.getX(),
                (int) (image.getWidth() - (contentBounds.getX() + contentBounds.getWidth())),
                (int) (image.getHeight() - (contentBounds.getY() + contentBounds.getHeight()))
        );
        if (maxContentBounds != null) {
            largest_content_size_label.setText("Largest content size: " + (int) maxContentBounds.getWidth() + " by " + (int) maxContentBounds.getHeight());
            largest_content_size_paneController.setContentSize((int) image.getWidth(), (int) image.getHeight());
            largest_content_size_paneController.setMargins(
                    (int) maxContentBounds.getY(),
                    (int) maxContentBounds.getX(),
                    (int) (image.getWidth() - (maxContentBounds.getX() + maxContentBounds.getWidth())),
                    (int) (image.getHeight() - (maxContentBounds.getY() + maxContentBounds.getHeight()))
            );
        }
        if (videoBoundary != null) {
            estimated_video_area_label.setText("Video boundary: " + (int) videoBoundary.getWidth() + " by " + (int) videoBoundary.getHeight());
            estimated_video_area_paneController.setContentSize((int) image.getWidth(), (int) image.getHeight());
            estimated_video_area_paneController.setMargins(
                    (int) videoBoundary.getY(),
                    (int) videoBoundary.getX(),
                    (int) (image.getWidth() - (videoBoundary.getX() + videoBoundary.getWidth())),
                    (int) (image.getHeight() - (videoBoundary.getY() + videoBoundary.getHeight()))
            );
        }

        if (debug) {
            if (contentFinder instanceof LineContentFinder) {
                //Scale to fit canvas
                gc.save();
                gc.scale(scaledImage.getWidth() / image.getWidth(), scaledImage.getHeight() / image.getHeight());

                ((LineContentFinder) contentFinder).drawDebug(gc, overlay);

                //Restore scaling
                gc.restore();
            }
        }
    }

    private void setIfLarger(final Rectangle a, final Rectangle b) {
        if (a.getWidth() + a.getHeight() == 0) return;
        if (a.getX() < b.getX()) b.setX(a.getX());
        if (a.getY() < b.getY()) b.setY(a.getY());
        if (a.getX() + a.getWidth() > b.getX() + b.getWidth()) b.setWidth(a.getX() + a.getWidth() - b.getX());
        if (a.getY() + a.getHeight() > b.getY() + b.getHeight()) b.setHeight(a.getY() + a.getHeight() - b.getY());
    }

    public boolean getOverlay() {
        return this.overlay;
    }

    public void setOverlay(final boolean overlay) {
        this.overlay = overlay;
        overlay_checkbox.setSelected(overlay);
        setFrame(this.image);
    }

    public boolean getDebug() {
        return this.debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
        debug_checkbox.setSelected(debug);
        setFrame(this.image);
    }

    public void setFrameCount(final int frameNumber) {
        frame_count_label.setText("Frame: " + frameNumber);
    }
}

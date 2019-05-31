package com.tycho.bbf.layout;

import com.sun.jndi.toolkit.url.Uri;
import com.tycho.bbf.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;

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
    private Label cursor_position_label;

    @FXML
    private Label content_finder_label;

    @FXML
    private CheckBox overlay_checkbox;

    @FXML
    private CheckBox debug_checkbox;

    @FXML
    private VBox properties;

    private GraphicsContext gc;

    private ContentFinder contentFinder;

    private boolean overlay = true;
    private boolean forceSymmetrical = true;
    private boolean debug = true;

    private Rectangle maxContentBounds = new Rectangle();
    private Rectangle videoBoundary = new Rectangle();

    private Image image;
    private Image scaledImage;

    @FXML
    private void initialize() {
        gc = video_canvas.getGraphicsContext2D();

        //Set up video canvas
        Utils.drawCheckerboard(video_canvas, 32);
        video_canvas.setOnMouseMoved(event -> {
            if (scaledImage != null) {
                int x = (int) (event.getX() - ((video_canvas.getWidth() - scaledImage.getWidth()) / 2));
                int y = (int) (event.getY() - ((video_canvas.getHeight() - scaledImage.getHeight()) / 2));
                x = (int) ((x / scaledImage.getWidth()) * image.getWidth());
                y = (int) ((y / scaledImage.getHeight()) * image.getHeight());
                if (x < 0 || x > image.getWidth() || y < 0 || y > image.getHeight()){
                    cursor_position_label.setText("");
                }else{
                    cursor_position_label.setText("Position: (" + x + ", " + y + ")");
                }
            }
        });

        //Set up
        content_size_paneController.setColor(Color.RED);
        largest_content_size_paneController.setColor(Color.GREEN);
        estimated_video_area_paneController.setColor(Color.BLUE);

        //Set up flags
        overlay_checkbox.setSelected(overlay);
        debug_checkbox.setSelected(debug);

       /* slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            slider_value.setText("Value: " + newValue);

            if (contentFinder instanceof DefaultContentFinder){
                contentFinder.getProperties().get("threshold").setValue(newValue.floatValue());
            }

            setFrame(image);
        });*/
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
        //resizeCanvas(image);

        //Scale image to fit canvas
        scaledImage = image;
        if (image.getWidth() != video_canvas.getWidth() || image.getHeight() != video_canvas.getHeight()) {
            final long start = System.currentTimeMillis();
            scaledImage = Utils.scale(image, (int) video_canvas.getWidth(), (int) video_canvas.getHeight(), true);
            //System.out.println("Resized to " + image.getWidth() + " by " + image.getHeight() + " in " + (System.currentTimeMillis() - start) + " ms.");
        }

        //Draw checkerboard pattern
        Utils.drawCheckerboard(video_canvas, 32);

        gc.save();
        gc.translate((video_canvas.getWidth() - scaledImage.getWidth()) / 2, (video_canvas.getHeight() - scaledImage.getHeight()) / 2);

        //Draw image
        gc.drawImage(scaledImage, 0, 0);

        //Find content bounds
        final long start = System.currentTimeMillis();
        final Rectangle contentBounds = contentFinder.findContent(image);
        final long elapsed = System.currentTimeMillis() - start;
        processing_time_label.setText("Processed in " + elapsed + " ms.");

        if (contentBounds.getWidth() + contentBounds.getHeight() > 0) {
            //Calculate maximum content bounds
            if (maxContentBounds == null) maxContentBounds = new Rectangle(contentBounds.getX(), contentBounds.getY(), contentBounds.getWidth(), contentBounds.getHeight());
            setIfLarger(contentBounds, maxContentBounds);

            //Calculate video boundary
            if (videoBoundary == null) videoBoundary = new Rectangle(maxContentBounds.getX(), maxContentBounds.getY(), maxContentBounds.getWidth(), maxContentBounds.getHeight());
            final Rectangle before = new Rectangle(videoBoundary.getX(), videoBoundary.getY(), videoBoundary.getWidth(), videoBoundary.getHeight());
            setIfLarger(maxContentBounds, videoBoundary);
            if (forceSymmetrical && (videoBoundary.getWidth() + videoBoundary.getHeight() > 0)) {
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
            final Rectangle after = new Rectangle(videoBoundary.getX(), videoBoundary.getY(), videoBoundary.getWidth(), videoBoundary.getHeight());
            if (before.getX() != after.getX() || before.getY() != after.getY() || before.getWidth() != after.getWidth() || before.getHeight() != after.getHeight()) {
                System.out.println("Border changed!");
            }
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

        if (debug) {
            if (contentFinder instanceof Debuggable) {
                //Scale to fit canvas
                gc.save();
                gc.scale(scaledImage.getWidth() / image.getWidth(), scaledImage.getHeight() / image.getHeight());

                ((Debuggable) contentFinder).drawDebug(gc);

                //Restore scaling
                gc.restore();
            }
        }
        gc.restore();

        content_size_paneController.setMaxContentSize((int) image.getWidth(), (int) image.getHeight());
        content_size_paneController.setContentSize((int) contentBounds.getWidth(), (int) contentBounds.getHeight());
        content_size_paneController.setMargins(
                (int) contentBounds.getY(),
                (int) contentBounds.getX(),
                (int) (image.getWidth() - (contentBounds.getX() + contentBounds.getWidth())),
                (int) (image.getHeight() - (contentBounds.getY() + contentBounds.getHeight()))
        );
        largest_content_size_paneController.setMaxContentSize((int) image.getWidth(), (int) image.getHeight());
        largest_content_size_paneController.setContentSize((int) maxContentBounds.getWidth(), (int) maxContentBounds.getHeight());
        largest_content_size_paneController.setMargins(
                (int) maxContentBounds.getY(),
                (int) maxContentBounds.getX(),
                (int) (image.getWidth() - (maxContentBounds.getX() + maxContentBounds.getWidth())),
                (int) (image.getHeight() - (maxContentBounds.getY() + maxContentBounds.getHeight()))
        );
        estimated_video_area_paneController.setMaxContentSize((int) image.getWidth(), (int) image.getHeight());
        estimated_video_area_paneController.setContentSize((int) videoBoundary.getWidth(), (int) videoBoundary.getHeight());
        estimated_video_area_paneController.setMargins(
                (int) videoBoundary.getY(),
                (int) videoBoundary.getX(),
                (int) (image.getWidth() - (videoBoundary.getX() + videoBoundary.getWidth())),
                (int) (image.getHeight() - (videoBoundary.getY() + videoBoundary.getHeight()))
        );

    }

    private void setIfLarger(final Rectangle a, final Rectangle b) {
        if (a.getWidth() + a.getHeight() == 0) return;
        if (a.getX() < b.getX() || (b.getWidth() + b.getHeight() == 0)) b.setX(a.getX());
        if (a.getY() < b.getY() || (b.getWidth() + b.getHeight() == 0)) b.setY(a.getY());
        if (a.getX() + a.getWidth() > b.getX() + b.getWidth() || (b.getWidth() + b.getHeight() == 0)) b.setWidth(a.getX() + a.getWidth() - b.getX());
        if (a.getY() + a.getHeight() > b.getY() + b.getHeight() || (b.getWidth() + b.getHeight() == 0)) b.setHeight(a.getY() + a.getHeight() - b.getY());
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

    public void setContentFinder(ContentFinder contentFinder) {
        this.contentFinder = contentFinder;
        this.content_finder_label.setText("Algorithm: " + contentFinder.getClass().getSimpleName());

        properties.getChildren().clear();
        for (ContentFinder.Property property : contentFinder.getProperties().values()){
            try {
                final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/property_layout.fxml"));
                properties.getChildren().add(loader.load());
                final PropertyLayout propertyLayout = loader.getController();
                propertyLayout.setProperty((ContentFinder.RangedProperty) property);
                propertyLayout.getSlider().valueProperty().addListener((observable, oldValue, newValue) -> {
                    property.setValue(newValue);
                    setFrame(image);
                });
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public void reset() {
        this.maxContentBounds = new Rectangle();
        this.videoBoundary = new Rectangle();
    }
}

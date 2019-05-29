package com.tycho.bbf;

import com.tycho.bbf.layout.MainLayout;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.JavaFXFrameConverter;

import java.io.File;

public class Main extends Application {

    /**
     * FrameGrabber used to extract frames from source video.
     */
    private FFmpegFrameGrabber grabber;

    /**
     * The current frame being displayed.
     */
    private Frame frame;

    private int frameNumber = -1;
    private int prevFrameNumber = 0;

    private static final ContentFinder[] contentFinders = new ContentFinder[]{new DefaultContentFinder(), new LineContentFinder()};
    private int contentFinderIndex = 0;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Black Bar Finder");
        primaryStage.setResizable(false);

        //Load main UI
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/main_layout.fxml"));
        final Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/styles/default.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.sizeToScene(); // Bug workaround
        primaryStage.show();

        final File file = new File("src/main/resources/blessings.mp4");
        //final File file = new File("src/main/resources/la_vibes.mp4");
        //final File file = new File("src/main/resources/limitless.mp4");
        //final File file = new File("src/main/resources/subtitle3.png");
        grabber = new FFmpegFrameGrabber(file);
        grabber.start();
        //frameNumber = 7420 - 1;
        //grabber.setVideoFrameNumber(frameNumber);

        final MainLayout mainLayout = loader.getController();
        mainLayout.setContentFinder(contentFinders[contentFinderIndex]);

        scene.setOnKeyPressed(event -> {
            final Frame previousFrame = frame;
            switch (event.getCode().getName()){
                case "D":
                    frame = getFrame(++frameNumber);
                    if (frame == previousFrame) --frameNumber;
                    break;

                case "A":
                    frame = getFrame(--frameNumber);
                    if (frame == previousFrame) ++frameNumber;
                    if (frameNumber < 0) frameNumber = 0;
                    break;

                case "E":
                    mainLayout.setOverlay(!mainLayout.getOverlay());
                    break;

                case "Q":
                    mainLayout.setDebug(!mainLayout.getDebug());
                    return;

                case "W":
                    contentFinderIndex++;
                    if (contentFinderIndex >= contentFinders.length) contentFinderIndex = 0;
                    mainLayout.setContentFinder(contentFinders[contentFinderIndex]);
                    break;

                case "S":
                    contentFinderIndex--;
                    if (contentFinderIndex < 0) contentFinderIndex = contentFinders.length - 1;
                    mainLayout.setContentFinder(contentFinders[contentFinderIndex]);
                    break;

                case "R":
                    mainLayout.reset();
                    break;
            }

            final Image image = new JavaFXFrameConverter().convert(frame);
            mainLayout.setFrame(image);
            mainLayout.setFrameCount(frameNumber + 1);
        });

        //Start on first frame
        //frame = getFrame(++frameNumber);
        //mainLayout.setFrame(new JavaFXFrameConverter().convert(frame));
        //mainLayout.setFrameCount(frameNumber);
    }

    private Frame getFrame(final int frameNumber){
        try {
            Frame frame;
            if (frameNumber == prevFrameNumber + 1){
                do{
                    frame = grabber.grab();
                }while (frame.imageWidth == 0);
            }else{
                grabber.setVideoFrameNumber(frameNumber);
                frame = grabber.grab();
            }
            prevFrameNumber = frameNumber;
            return frame.clone();
        }catch (NullPointerException e){
            //Ignore
        }catch (FrameGrabber.Exception e){
            e.printStackTrace();
        }
        return this.frame;
    }
}

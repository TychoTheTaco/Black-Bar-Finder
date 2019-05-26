package com.tycho.bbf;

import com.tycho.bbf.layout.MainLayout;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.JavaFXFrameConverter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    private FFmpegFrameGrabber grabber;

    private Frame frame;

    private int frameCount = 0;
    private int prevFrameNumber = 0;

    private final List<Long> frameTimes = new ArrayList<>();

    private MainLayout mainLayout;

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
        //final File file = new File("src/main/resources/limitless.mp4");
        //final File file = new File("src/main/resources/subtitle3.png");
        grabber = new FFmpegFrameGrabber(file);
        grabber.start();
        //frameCount = 1800;
        //grabber.setVideoFrameNumber(frameCount);

        mainLayout = loader.getController();

        scene.setOnKeyPressed(event -> {
            switch (event.getCode().getName()){
                case "D":
                    frame = getFrame(++frameCount);
                    break;

                case "A":
                    frame = getFrame(--frameCount);
                    if (frameCount < 1) frameCount = 1;
                    break;

                case "E":
                    mainLayout.setOverlay(!mainLayout.getOverlay());
                    break;

                case "V":
                    System.out.println("Average: " + average(frameTimes));
                    return;

                case "B":
                    mainLayout.setDebug(!mainLayout.getDebug());
                    return;
            }

            final Image image = new JavaFXFrameConverter().convert(frame);
            mainLayout.setFrame(image);
            mainLayout.setFrameCount(frameCount);
        });

        //Start on first frame
        frame = getFrame(++frameCount);
        mainLayout.setFrame(new JavaFXFrameConverter().convert(frame));
        mainLayout.setFrameCount(frameCount);
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
            return frame;
        }catch (FrameGrabber.Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private static long average(final List<Long> list){
        long total = 0;
        for (long n : list){
            total += n;
        }
        return total / list.size();
    }
}

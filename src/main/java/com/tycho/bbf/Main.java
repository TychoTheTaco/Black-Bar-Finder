package com.tycho.bbf;

import com.tycho.bbf.layout.MainLayout;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.bytedeco.javacv.JavaFXFrameConverter;

import java.io.File;

public class Main extends Application {

    private static final ContentFinder[] contentFinders = new ContentFinder[]{new DefaultContentFinder(), new LineContentFinder(), new StrictLineContentFinder()};
    private int contentFinderIndex = 0;

    private boolean running = false;

    private static final Object LOCK = new Object();

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
        final FrameExtractor frameExtractor = new FrameExtractor(file);
        //frameNumber = 7420 - 1;
        //grabber.setVideoFrameNumber(frameNumber);

        final MainLayout mainLayout = loader.getController();
        mainLayout.setContentFinder(contentFinders[contentFinderIndex]);

        scene.setOnKeyPressed(event -> {
            switch (event.getCode().getName()){
                case "D":
                    frameExtractor.nextFrame();
                    break;

                case "A":
                    frameExtractor.previousFrame();
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

                case "P":
                    running = !running;
                    /*if (running){
                        new Thread(() -> {
                            while (running){
                                //Get the next frame from the source video
                                final Frame frame = getFrame(++frameNumber);
                                if (frame == previousFrame){
                                    --frameNumber;
                                    running = false;
                                    break;
                                }
                                this.frame = frame;

                                //Convert frame to image
                                final Image image = new JavaFXFrameConverter().convert(frame);

                                //Update UI
                                Platform.runLater(() -> {
                                    mainLayout.setFrame(image);
                                    mainLayout.setFrameCount(frameNumber + 1);

                                    synchronized (LOCK){
                                        LOCK.notifyAll();
                                    }
                                });

                                try {
                                    synchronized (LOCK){
                                        LOCK.wait();
                                    }
                                }catch (InterruptedException e){
                                    e.printStackTrace();
                                }
                            }
                        }).start();
                    }*/
                    return;
            }

            final Image image = new JavaFXFrameConverter().convert(frameExtractor.getFrame());
            mainLayout.setFrame(image);
            mainLayout.setFrameCount(frameExtractor.getFrameNumber() + 1);
        });

        //Start on first frame
        //frame = getFrame(++frameNumber);
        //mainLayout.setFrame(new JavaFXFrameConverter().convert(frame));
        //mainLayout.setFrameCount(frameNumber);
    }
}

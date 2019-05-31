package com.tycho.bbf;

import com.tycho.bbf.layout.MainLayout;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.JavaFXFrameConverter;

import java.io.File;

public class BlackBarFinder extends Application {

    //Available content finder algorithms
    private static final ContentFinder[] contentFinders = new ContentFinder[]{new DefaultContentFinder(), new LineContentFinder(), new StrictLineContentFinder()};
    private int contentFinderIndex = 0;

    //True if the auto-player is running
    private boolean running = false;

    private static final Object LOCK = new Object();

    private MainLayout mainLayout;

    private FrameExtractor frameExtractor;

    @Override
    public void start(Stage primaryStage) throws Exception {
        //Setup menu bar
        final MenuBar menuBar = new MenuBar();
        final Menu fileMenu = new Menu("File");
        final MenuItem openMenuItem = new MenuItem("Open");
        openMenuItem.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            final File file = fileChooser.showOpenDialog(primaryStage);
            if (file != null && !openFile(file)){
                final Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to open file!");
                alert.setContentText("Failed to open the file '"  + file.getAbsolutePath() + "'.");
                alert.show();
            }
        });
        fileMenu.getItems().add(openMenuItem);
        menuBar.getMenus().add(fileMenu);

        //Setup UI
        final BorderPane borderPane = new BorderPane();
        borderPane.setTop(menuBar);

        //Load main layout
        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/main_layout.fxml"));
        borderPane.setCenter(loader.load());
        mainLayout = loader.getController();
        mainLayout.setContentFinder(contentFinders[contentFinderIndex]);

        //Setup scene and stage
        final Scene scene = new Scene(borderPane);
        scene.getStylesheets().add(getClass().getResource("/styles/default.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Black Bar Finder");
        primaryStage.setResizable(false);
        primaryStage.sizeToScene(); // Bug workaround
        primaryStage.show();

        final File file = new File("src/main/resources/blessings.mp4");
        //final File file = new File("src/main/resources/la_vibes.mp4");
        //final File file = new File("src/main/resources/limitless.mp4");
        //final File file = new File("src/main/resources/small.jpg");
        //final File file = new File("src/main/resources/subtitle3.png");
        openFile(file);

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
                    if (running){
                        new Thread(() -> {
                            Frame previousFrame = null;
                            while (running){
                                final long start = System.currentTimeMillis();

                                //Get the next frame from the source video
                                frameExtractor.nextFrame();
                                if (previousFrame == frameExtractor.getFrame()){
                                    return;
                                }

                                //Convert frame to image
                                final Image image = new JavaFXFrameConverter().convert(frameExtractor.getFrame());

                                //Update UI
                                Platform.runLater(() -> {
                                    mainLayout.setFrame(image);
                                    mainLayout.setFrameCount(frameExtractor.getFrameNumber() + 1);

                                    synchronized (LOCK){
                                        LOCK.notifyAll();
                                    }
                                });

                                try {
                                    synchronized (LOCK){
                                        LOCK.wait();
                                    }

                                    //Maintain frame rate
                                    Thread.sleep((long) Math.max(0, ((1000 / frameExtractor.getFrameRate()) - (2 * (System.currentTimeMillis() - start)))));
                                }catch (InterruptedException e){
                                    e.printStackTrace();
                                }

                                previousFrame = frameExtractor.getFrame();
                            }
                        }).start();
                    }
                    return;
            }

            //Process frame
            final Image image = new JavaFXFrameConverter().convert(frameExtractor.getFrame());
            mainLayout.setFrame(image);
            mainLayout.setFrameCount(frameExtractor.getFrameNumber() + 1);
        });
    }

    private boolean openFile(final File file){
        if (file == null) return false;

        this.frameExtractor = new FrameExtractor(file);

        try {
            frameExtractor.start();
        }catch (FrameGrabber.Exception e){
            e.printStackTrace();
            return false;
        }

        //Start on first frame
        frameExtractor.nextFrame();
        mainLayout.reset();
        mainLayout.setFrame(new JavaFXFrameConverter().convert(frameExtractor.getFrame()));
        mainLayout.setFrameCount(frameExtractor.getFrameNumber() + 1);

        return true;
    }
}

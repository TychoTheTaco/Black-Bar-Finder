package com.tycho.bbf;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import java.io.File;

public class FrameExtractor {

    private final File file;

    private final FFmpegFrameGrabber frameGrabber;

    private Frame currentFrame;

    private int frameNumber = -1;
    private int prevFrameNumber = -1;

    public FrameExtractor(final File file){
        this.file = file;
        this.frameGrabber = new FFmpegFrameGrabber(file);

        try {
            this.frameGrabber.start();
        }catch (FrameGrabber.Exception e){
            e.printStackTrace();
        }
    }

    public synchronized Frame nextFrame(){
        final Frame frame = getFrame(++frameNumber);
        if (frame == null){
            --frameNumber;
            return this.currentFrame;
        }
        return this.currentFrame = frame;
    }

    public synchronized Frame previousFrame(){
        final Frame frame = getFrame(--frameNumber);
        if (frame == null){
            ++frameNumber;
            return this.currentFrame;
        }
        return this.currentFrame = frame;
    }

    public synchronized Frame getFrame(){
        return this.currentFrame;
    }

    public synchronized int getFrameNumber() {
        return frameNumber;
    }

    private Frame getFrame(final int frameNumber){
        if (frameNumber < 0){
            return null;
        }
        try {
            Frame frame;
            if (frameNumber == prevFrameNumber + 1){
                do{
                    frame = frameGrabber.grab();
                }while (frame.imageWidth == 0);
            }else{
                frameGrabber.setVideoFrameNumber(frameNumber);
                frame = frameGrabber.grab();
            }
            prevFrameNumber = frameNumber;
            return frame == this.currentFrame ? null : frame.clone();
        }catch (NullPointerException e){
            //Ignore
        }catch (FrameGrabber.Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

package cn.hisdar.file.share.tool.common;

import android.util.Log;

import java.util.ArrayList;

public class FileShareTimer extends Thread {

    private final static String TAG = "FileShareTool";

    private long totalTime;
    private int timerStep;
    private boolean exitTimer;
    private ListenerMap<FileShareTimerListener> timerListener;

    public FileShareTimer(long ms, int step) {
        exitTimer = false;
        totalTime = ms;
        timerStep = step;
        timerListener = new ListenerMap<>();
    }

    public void addListener(FileShareTimerListener listener) {
        timerListener.addListener(listener);
    }

    public void removeListener(FileShareTimerListener listener) {
        timerListener.removeListener(listener);
    }

    public void startTimer() {
        exitTimer = false;
        start();
    }

    public void stopTimer() {
        exitTimer = true;
        interrupt();
    }

    @Override
    public void run() {
        ArrayList<FileShareTimerListener> listeners = timerListener.getAllListeners();
        long startTime = System.currentTimeMillis();
        long endTime = startTime;
        int step = 0;

        while (endTime - startTime < totalTime && !exitTimer) {
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).timerStepEvent(step);
            }
            try {
                sleep(timerStep);
            } catch (InterruptedException e) {}
            step++;

            endTime = System.currentTimeMillis();
        }

        for (int i = 0; i < listeners.size(); i++) {
            listeners.get(i).timerDone();
        }
    }
}

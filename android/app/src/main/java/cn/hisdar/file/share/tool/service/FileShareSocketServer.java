package cn.hisdar.file.share.tool.service;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class FileShareSocketServer extends Thread {

    private static String TAG = "FileShareSocketServer";

    private static FileShareSocketServer socketServer;


    private boolean isExit;
    private boolean isServerRunning;
    private FileShareSocketServer() {
        isExit = false;
        isServerRunning = false;
    }

    synchronized private static FileShareSocketServer getInstanceSync() {
        if (socketServer == null) {
            socketServer = new FileShareSocketServer();
        }

        return socketServer;
    }

    public static FileShareSocketServer getInstance() {
        if (socketServer != null) {
            return socketServer;
        }

        return getInstanceSync();
    }

    synchronized public void startServer() {
        isExit = false;
        if (isServerRunning) {
            return;
        }

        start();
    }

    synchronized public void stopServer() {
        isExit = true;
        interrupt();
    }

    public void run() {
        isServerRunning = true;
        ServerSocket commandServerSocket = null;
        ServerSocket dataServerSocket = null;
        try {
            commandServerSocket = new ServerSocket(5299);
            dataServerSocket = new ServerSocket(5300);
        } catch (IOException e) {
            commandServerSocket = null;
            e.printStackTrace();
        }

        while (!isExit && commandServerSocket != null) {
            Socket commandClientSocket = null;
            Socket dataClientSocket = null;
            try {
                Log.i(TAG, "wait for socket connect ...");
                commandClientSocket = commandServerSocket.accept();
                dataClientSocket = dataServerSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }

            FileShareSocket fileShareSocket = new FileShareSocket(commandClientSocket, dataClientSocket);
        }

        isServerRunning = false;
    }
}

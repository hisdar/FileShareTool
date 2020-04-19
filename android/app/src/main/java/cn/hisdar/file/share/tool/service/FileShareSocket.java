package cn.hisdar.file.share.tool.service;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import cn.hisdar.file.share.tool.command.Command;
import cn.hisdar.file.share.tool.command.CommandDispatcher;

public class FileShareSocket {

    private final static String TAG = "FileShareSocket";

    private boolean isExit;

    private Socket commandSocket;
    private Socket dataSocket;
    private InputStream in;
    private OutputStream commandOut;
    private OutputStream dataOut;
    private FileShareSocketWorker worker;

    public FileShareSocket(Socket commandSocket, Socket dataSocket) {
        this.commandSocket = commandSocket;
        this.dataSocket = dataSocket;

        in = null;
        dataOut = null;
        commandOut = null;
        isExit = false;
        worker = new FileShareSocketWorker();
        worker.start();
    }

    public void stopServer() {
        isExit = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void FileShareSocketWorkFunc() {
        if (commandSocket == null) {
            return;
        }

        try {
            in = commandSocket.getInputStream();
            commandOut = commandSocket.getOutputStream();
            dataOut = dataSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String lineString = null;
        while (!isExit) {
            try {
                lineString = reader.readLine();
                if (lineString == null) {
                    continue;
                }

                if (!lineString.trim().equals("<HisdarSocketCommand>")) {
                    Log.i(TAG, "" + lineString);
                    continue;
                }

                StringBuffer commandStringBuffer = new StringBuffer();
                while (true) {
                    lineString = reader.readLine();
                    if (lineString.trim().equals("</HisdarSocketCommand>")) {
                        break;
                    }

                    commandStringBuffer.append(lineString);
                    commandStringBuffer.append("\n");
                }

                Command command = new Command();
                command.parseCommand(commandStringBuffer);
                CommandDispatcher.getInstance().dispatch(command, commandOut, dataOut);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }

    }

    private class FileShareSocketWorker extends Thread {
        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void run() {
            FileShareSocketWorkFunc();
        }
    }
}

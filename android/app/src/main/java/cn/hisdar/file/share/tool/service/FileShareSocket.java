package cn.hisdar.file.share.tool.service;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import cn.hisdar.file.share.tool.command.CommandUtil;

public class FileShareSocket {

    private final static String TAG = "FileShareSocket";

    private boolean isExit;

    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private FileShareSocketWorker worker;



    public FileShareSocket(Socket socket) {
        socket = socket;

        in = null;
        out = null;
        isExit = false;
        worker = new FileShareSocketWorker();
        worker.start();
    }

    public void stopServer() {
        isExit = true;
    }

    private void FileShareSocketWorkFunc() {
        if (socket == null) {
            return;
        }

        try {
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        byte[] readBuffer = new byte[1024];
        while (!isExit) {
            try {
                // read command head
                int commandHead = in.read();
                if (commandHead == -1) {
                    continue;
                }

                if (readBuffer[0] != 0x1E) {
                    continue;
                }

                // read command type:
                int readLen = in.read(readBuffer, 0, CommandUtil.COMMAND_TYPE_SIZE);
                if (readLen != CommandUtil.COMMAND_TYPE_SIZE) {
                    continue;
                }
                int commandType = CommandUtil.decodeInt(readBuffer);

                // read command
                readLen = in.read(readBuffer, 0, CommandUtil.COMMAND_SIZE);
                if (readLen != CommandUtil.COMMAND_SIZE) {
                    continue;
                }
                int command = CommandUtil.decodeInt(readBuffer);

                // read data type
                int dataType = in.read();
                if (dataType == -1) {
                    continue;
                }

                // read data length
                readLen = in.read(readBuffer, 0, CommandUtil.DATA_LENGTH_SIZE);
                if (readLen != CommandUtil.DATA_LENGTH_SIZE) {
                    continue;
                }
                int dataLen = CommandUtil.decodeInt(readBuffer);

                // read data
                while (dataLen > 0) {
                    int dataReadLen = dataLen;
                    if (dataLen >= 1024) {
                        dataReadLen = 1024;
                    }

                    readLen = in.read(readBuffer, 0, dataReadLen);
                    dataLen -= readLen;
                }

                Log.i(TAG, "commandType:" + commandType +
                                ", command:" + command +
                                ", dataType:" + dataType +
                                ", dataLen:" + dataLen);
            } catch (IOException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    private class FileShareSocketWorker extends Thread {
        public void run() {
            FileShareSocketWorkFunc();
        }
    }
}

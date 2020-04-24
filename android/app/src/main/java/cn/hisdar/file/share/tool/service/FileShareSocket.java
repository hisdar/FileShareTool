package cn.hisdar.file.share.tool.service;

import android.content.Context;
import android.os.Build;
import android.renderscript.ScriptGroup;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import cn.hisdar.file.share.tool.command.Command;
import cn.hisdar.file.share.tool.command.GetChildFilesCommand;
import cn.hisdar.file.share.tool.command.GetDeviceInfoCommand;

public class FileShareSocket {

    private final static String TAG = "FileShareSocket";

    private boolean isExit;

    private Context context;
    private Socket cmdSocket;
    private Socket dataSocket;
    private InputStream cmdInputStream;
    private InputStream dataInputStream;
    private OutputStream cmdOutputStream;
    private OutputStream dataOutputStream;
    private FileShareSocketWorker worker;

    public FileShareSocket(Context context, Socket cmdSocket, Socket dataSocket) {
        this.context = context;
        this.cmdSocket = cmdSocket;
        this.dataSocket = dataSocket;

        try {
            cmdInputStream = cmdSocket.getInputStream();
            cmdOutputStream = cmdSocket.getOutputStream();
            dataInputStream = dataSocket.getInputStream();
            dataOutputStream = new BufferedOutputStream(dataSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        isExit = false;
        worker = new FileShareSocketWorker();
        worker.start();
    }

    public void stopServer() {
        isExit = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void FileShareSocketWorkFunc() {
        if (cmdSocket == null) {
            return;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(cmdInputStream, StandardCharsets.UTF_8));
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
                dispatch(command, cmdOutputStream, dataOutputStream);
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

    public void dispatch(Command command, OutputStream commandOut, OutputStream dataOut) {
        if (command.getCommandType().equals(Command.COMMAND_TYPE_REQUEST)) {
            if (command.getCommand().equals(Command.COMMAND_GET_DEVICE_INFO)) {
                responseGetDeviceInfo();
                return;
            }

            if (command.getCommand().equals(Command.COMMAND_GET_CHILD_FILES)) {
                responseGetChildFiles(command);
                return;
            }

            if (command.getCommand().equals(Command.COMMAND_GET_FILE)) {
                responseGetFile(command);
                return;
            }

            if (command.getCommand().equals(Command.COMMAND_PUT_FILE)) {
                responsePutFile(command);
                return;
            }
        }
    }

    public void responseGetDeviceInfo() {
        GetDeviceInfoCommand command = new GetDeviceInfoCommand(context);
        String cmdStr = command.generateCommand(Command.COMMAND_TYPE_RESPONSE, Command.COMMAND_GET_DEVICE_INFO);
        writeCommand(cmdStr.getBytes());
    }

    public void responseGetChildFiles(Command srcCmd) {
        String path = srcCmd.getCommandItem("DirectoryPath");
        GetChildFilesCommand childFiles = new GetChildFilesCommand();
        String result = childFiles.generateCommand(path);

        String response = srcCmd.getFormatedCommandType(Command.COMMAND_TYPE_RESPONSE);
        response += srcCmd.getFormatedCommand(srcCmd.getCommand());
        if (result != null) {
            response += Command.COMMAND_EXEC_RESULT_SUCCESS;
            response += result;
        } else {
            response += Command.COMMAND_EXEC_RESULT_FAIL;
        }

        response = srcCmd.addCommandHeadAndTail(response);
        writeCommand(response.getBytes());
    }

    public void responseGetFile(Command srcCmd) {
        String srcPath = srcCmd.getCommandItem("SrcPath");

        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            Log.i(TAG, "file not exist:" + srcPath);
            return;
        }

        String resultString = srcCmd.getFormatedCommandType(Command.COMMAND_TYPE_RESPONSE);
        resultString += srcCmd.getFormatedCommand(Command.COMMAND_GET_FILE);
        resultString += "<FileLength>" + srcFile.length() + "</FileLength>\n";

        resultString += Command.COMMAND_EXEC_RESULT_SUCCESS;
        resultString = srcCmd.addCommandHeadAndTail(resultString);

        writeCommand(resultString.getBytes());

        // start a new socket to transfer byte data
        long totalData = srcFile.length();
        byte[] buf = new byte[4096];
        try {
            FileInputStream fileIn = new FileInputStream(srcFile);
            int readLen = fileIn.read(buf);
            while (readLen > 0) {
                dataOutputStream.write(buf, 0, readLen);
                dataOutputStream.flush();
                totalData -= readLen;
                Log.i(TAG, "write length:" + readLen + ", left:" +  totalData);
                readLen = fileIn.read(buf);
            }
            Log.i(TAG, "file write finished");
            fileIn.close();
            dataOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void responsePutFile(Command srcCmd) {
        String savePath = srcCmd.getCommandItem("TagPath");
        String fileLengthString = srcCmd.getCommandItem("FileLength");
        Log.i(TAG, "savePath:" + savePath);
        Log.i(TAG, "fileLengthString:" + fileLengthString);

        long fileLength = 0;
        FileOutputStream fileOutputStream = null;
        try {
            fileLength = Long.parseLong(fileLengthString);
            fileOutputStream = new FileOutputStream(new File(savePath));
        } catch (Exception e) {
            e.printStackTrace();
            responseError(srcCmd);
            return;
        }

        responseSuccess(srcCmd);
        Log.i(TAG, "start to read file");
        byte[] readBuffer = new byte[1024 * 8];
        try {
            while (fileLength > 0) {
                int readLength = dataInputStream.read(readBuffer);
                Log.i(TAG, "read data length:" + readLength);
                fileOutputStream.write(readBuffer, 0, readLength);
                fileLength -= readLength;
            }

            fileOutputStream.flush();
            fileOutputStream.close();
        } catch ( IOException e) {
            e.printStackTrace();
            return;
        }

        Log.i(TAG, "save file finished");
    }

    public void responseError(Command srcCmd) {
        Command responseCmd = new Command();
        String command = responseCmd.getFormatedCommandType(Command.COMMAND_TYPE_RESPONSE);
        command += responseCmd.getFormatedCommand(srcCmd.getCommand());
        command += Command.COMMAND_EXEC_RESULT_FAIL;
        command = responseCmd.addCommandHeadAndTail(command);
        writeCommand(command.getBytes());
    }

    public void responseSuccess(Command srcCmd) {
        Command responseCmd = new Command();
        String command = responseCmd.getFormatedCommandType(Command.COMMAND_TYPE_RESPONSE);
        command += responseCmd.getFormatedCommand(srcCmd.getCommand());
        command += Command.COMMAND_EXEC_RESULT_SUCCESS;
        command = responseCmd.addCommandHeadAndTail(command);
        writeCommand(command.getBytes());
    }


    public boolean writeCommand(byte[] command) {
        try {
            cmdOutputStream.write(command);
            cmdOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }
}

package cn.hisdar.file.share.tool.command;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ServerSocket;

import cn.hisdar.file.share.tool.common.CommandUtil;

public class CommandDispatcher {

    private static String TAG = "FileShareCommandDispatcher";
    private static CommandDispatcher commandDispatcher = null;

    private CommandDispatcher() {

    }

    public static CommandDispatcher getInstance() {
        if (commandDispatcher == null) {
            synchronized (CommandDispatcher.class) {
                if (commandDispatcher == null) {
                    commandDispatcher = new CommandDispatcher();
                }
            }
        }

        return commandDispatcher;
    }

    public void dispatch(Command command, OutputStream commandOut, OutputStream dataOut) {
        if (command.getCommandType().equals(Command.COMMAND_TYPE_REQUEST)) {
            if (command.getCommand().equals(Command.COMMAND_GET_DEVICE_INFO)) {
                responseGetDeviceInfo(commandOut);
                return;
            }


            if (command.getCommand().equals(Command.COMMAND_GET_CHILD_FILES)) {
                responseGetChildFiles(commandOut, command);
                return;
            }

            if (command.getCommand().equals(Command.COMMAND_GET_FILE)) {
                Log.i(TAG, "handle get file command");
                responseGetFile(commandOut, dataOut, command);
                return;
            }

        } else if (command.getCommandType().equals(Command.COMMAND_TYPE_RESPONSE)) {

        } else {

        }
    }

    public boolean writeCommand(OutputStream out, byte[] command) {
        try {
            out.write(command);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void responseError(Command srcCmd, OutputStream out) {
        Command responseCmd = new Command();
        String command = responseCmd.getFormatedCommandType(Command.COMMAND_TYPE_RESPONSE);
        command += responseCmd.getFormatedCommand(srcCmd.getCommand());
        command += Command.COMMAND_EXEC_RESULT_FAIL;
        writeCommand(out, command.getBytes());
    }

    public void responseGetDeviceInfo(OutputStream out) {
        GetDeviceInfoCommand command = new GetDeviceInfoCommand();
        String cmdStr = command.generateCommand(Command.COMMAND_TYPE_RESPONSE, Command.COMMAND_GET_DEVICE_INFO);
        writeCommand(out, cmdStr.getBytes());
    }

    public void responseGetChildFiles(OutputStream out, Command srcCmd) {
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
        writeCommand(out, response.getBytes());
    }

    public void responseGetFile(OutputStream commandOut, OutputStream dataOut, Command srcCmd) {
        String srcPath = srcCmd.getCommandItem("SrcPath");
        String savePath = srcCmd.getCommandItem("SavePath");

        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            Log.i(TAG, "file not exist:" + srcPath);
            return;
        }

        String resultString = srcCmd.getFormatedCommandType(Command.COMMAND_TYPE_RESPONSE);
        resultString += srcCmd.getFormatedCommand(Command.COMMAND_GET_FILE);
        resultString += "<SavePath>" + savePath + "</SavePath>";
        resultString += "<FileLength>" + srcFile.length() + "</FileLength>\n";


        resultString += Command.COMMAND_EXEC_RESULT_SUCCESS;
        resultString = srcCmd.addCommandHeadAndTail(resultString);

        writeCommand(commandOut, resultString.getBytes());

        // start a new socket to transfer byte data
        byte[] buf = new byte[1024];
        try {
            FileInputStream fileIn = new FileInputStream(srcFile);
            int readLen = fileIn.read(buf);
            while (readLen > 0) {
                dataOut.write(buf, 0, readLen);
                readLen = fileIn.read(buf);
            }

            fileIn.close();
            dataOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

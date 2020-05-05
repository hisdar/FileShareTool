package cn.hisdar.file.share.tool.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import cn.hisdar.file.share.tool.command.Command;
import cn.hisdar.file.share.tool.common.FileShareTimer;
import cn.hisdar.file.share.tool.common.FileShareTimerListener;

public class FileShareSocketServer extends Thread implements FileShareTimerListener {

    private static String TAG = "FileShareSocketServer";
    private static FileShareSocketServer socketServer;

    private final static int BroadcastMessagePort = 5298;

    private final static long MESSAGE_SEND_TIMER_TOTAL_TIME = 20000;
    private final static int  MESSAGE_SEND_TIMER_STEP       = 4000;

    private boolean isExit;
    private boolean isServerRunning;
    private Context context;
    private FileShareTimer messageSendTimer;

    private FileShareSocketServer() {
        isExit = false;
        context = null;
        isServerRunning = false;
    }

    synchronized public void startServer(Context context) {
        isExit = false;
        this.context = context;
        if (isServerRunning) {
            return;
        }

        start();
        messageSendTimer = new FileShareTimer(MESSAGE_SEND_TIMER_TOTAL_TIME, MESSAGE_SEND_TIMER_STEP);
        messageSendTimer.addListener(this);
        messageSendTimer.startTimer();
    }

    synchronized public void stopServer() {
        messageSendTimer.stopTimer();

        isExit = true;
        interrupt();
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

    private String ipAddressToString(int ip) {
        return (ip & 0xFF ) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                ((ip >> 24) & 0xFF) ;
    }

    private void sendOnlineMessage() {

        // check wifi connection
        ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (!mWifiInfo.isConnected()) {
            return;
        }

        // get local ip address
        WifiManager wifiManager =(WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();

        // generate broadcast ip address
        int broadcastAddress = ipAddress | 0xFF000000;
        String broadcastAddressStr = ipAddressToString(broadcastAddress) ;

        Log.i(TAG, "broadcastAddress:" + broadcastAddressStr);

        try {
            String cmd = Command.getFormatedCommandType(Command.COMMAND_TYPE_REQUEST);
            cmd += Command.getFormatedCommand(Command.COMMAND_LET_ME_HEAR_YOU);
            cmd += "<IPAddress>" + ipAddressToString(ipAddress) + "</IPAddress>\n";
            cmd = Command.addCommandHeadAndTail(cmd);
            byte[] messageBytes = cmd.getBytes();
            InetAddress inetAddress = InetAddress.getByName(broadcastAddressStr);
            DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length, inetAddress, 5298);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void parseBroadcastMessage(byte[] msgArray) {
        String msgStr = new String(msgArray, StandardCharsets.UTF_8);

        Command command = new Command();
        command.parseCommand(new StringBuffer(msgStr));
        msgStr = command.getCommandItem(Command.COMMAND_SHELL);
        if (msgStr == null) {
            // not my message ignore
            return;
        }

        command.clear();
        command.parseCommand(new StringBuffer(msgStr));
        String cmdType = command.getCommandItem(Command.COMMAND_TYPE_KEY);
        if (cmdType.equals(Command.COMMAND_TYPE_REQUEST)) {
            return;
        } else if (cmdType.equals(Command.COMMAND_TYPE_RESPONSE)) {
            Log.i(TAG, msgStr);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void run() {
        try {
            isExit = false;
            byte[] arr = new byte[1024 * 8];
            DatagramSocket serverSocket = new DatagramSocket(BroadcastMessagePort);
            DatagramPacket packet = new DatagramPacket(arr, arr.length);

            while (!isExit) {
                serverSocket.receive(packet);
                parseBroadcastMessage(packet.getData());
            }
            serverSocket.close();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        /*
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

            FileShareSocket fileShareSocket = new FileShareSocket(context, commandClientSocket, dataClientSocket);
        }

        Log.i(TAG, "server exit");

        isServerRunning = false;

         */
    }

    @Override
    public void timerStepEvent(int step) {
        sendOnlineMessage();
    }

    @Override
    public void timerDone() {

    }
}

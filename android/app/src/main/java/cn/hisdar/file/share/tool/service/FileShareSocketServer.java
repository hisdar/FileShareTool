package cn.hisdar.file.share.tool.service;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class FileShareSocketServer extends Thread {

    private static String TAG = "FileShareSocketServer";
    private static FileShareSocketServer socketServer;
    private boolean isExit;
    private boolean isServerRunning;
    private Context context;
    private FileShareSocketServer() {
        isExit = false;
        context = null;
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

    synchronized public void startServer(Context context) {
        isExit = false;
        this.context = context;
        if (isServerRunning) {
            return;
        }

        start();
    }

    synchronized public void stopServer() {
        isExit = true;
        interrupt();
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
        String ipAddressStr = (broadcastAddress & 0xFF ) + "." +
                                ((broadcastAddress >> 8) & 0xFF) + "." +
                                ((broadcastAddress >> 16) & 0xFF) + "." +
                                ((broadcastAddress >> 24) & 0xFF) ;

        Log.i(TAG, "broadcastAddress:" + ipAddressStr);

        try {
            String message = "connect me";
            byte[] messageBytes = message.getBytes();
            InetAddress inetAddress = InetAddress.getByName(ipAddressStr);
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

    public void run() {
        isServerRunning = true;

        // sendOnlineMessage();

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

        isServerRunning = false;
    }
}

package cn.hisdar.file.share.tool.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;

import cn.hisdar.file.share.tool.common.FileShareTimer;


public class FileShareService extends Service {

    private static final String TAG = "FileShareService";
    public static final int COMMAND_SEARCH_REMOTES = 0X1001;
    public static final int COMMAND_STOP_SERVER  = 0X1002;
    public static final int UPDATE_MASTER_LIST   = 0X1003;

    public static final String ACTION_START_GET_REMOTES_LIST = "get.remotes.list.start";
    public static final String ACTION_STOP_GET_REMOTES_LIST  = "get.remotes.list.stop";

    public static final String ACTION_GET_MASTER_LIST = "get.master.list";
    public static final String ACTION_UPDATE_MASTER_LIST = "update.master.list";

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    private LocalBroadcastManager localBroadcastManager;
    private ServiceBroadcastReceiver serviceBroadcastReceiver;

    public FileShareService() {

    }

    @Override
    public void onCreate() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        serviceBroadcastReceiver = new ServiceBroadcastReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_START_GET_REMOTES_LIST);
        intentFilter.addAction(ACTION_STOP_GET_REMOTES_LIST);
        localBroadcastManager.registerReceiver(serviceBroadcastReceiver, intentFilter);

        Log.i(TAG, "onStartCommand finished");
        FileShareSocketServer socketServer = FileShareSocketServer.getInstance();
        socketServer.startServer(getApplicationContext());
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    public class ServiceBroadcastReceiver  extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "message:" + intent.getAction());
            if (intent.getAction().equals(ACTION_START_GET_REMOTES_LIST)) {

                return;
            }

            if (intent.getAction().equals(ACTION_STOP_GET_REMOTES_LIST)) {
                return;
            }
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper){
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            Log.i(TAG, "handle message:" + msg.what);
            switch (msg.what) {
                case COMMAND_SEARCH_REMOTES:
                    //FileShareMaster master = new FileShareMaster();
                    //master.getMasters(getApplicationContext());
                    break;
                case COMMAND_STOP_SERVER:
                    stopSelf();
                    break;
                case UPDATE_MASTER_LIST:
                    //MasterSearcher masterSearcher = MasterSearcher.getInstance();
                    //masterSearcher.getMasters(getApplicationContext());
                    break;
            }
        }
    }
}

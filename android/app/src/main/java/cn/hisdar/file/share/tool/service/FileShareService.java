package cn.hisdar.file.share.tool.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
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

import android.os.SystemClock;
import android.util.Log;


public class FileShareService extends Service {

    private static final String TAG = "FileShareService";
    private static final int COMMAND_START_SERVER = 0X1001;
    private static final int COMMAND_STOP_SERVER  = 0X1002;
    private static final int UPDATE_MASTER_LIST   = 0X1003;
    public  static final String ACTION_GET_MASTER_LIST = "get.master.list";
    public  static final String ACTION_UPDATE_MASTER_LIST = "update.master.list";

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;


    private LocalBroadcastManager localBroadcastManager;
    private ServiceBroadcastReceiver serviceBroadcastReceiver;

    public FileShareService() {

    }

    @Override
    public void onCreate() {
        int priority = android.os.Process.THREAD_PRIORITY_BACKGROUND;
        HandlerThread thread = new HandlerThread("ServiceHandlerThread", priority);
        thread.start();

        // 获取工作线程的Looper
        mServiceLooper = thread.getLooper();

        // 创建工作线程的Handler
        mServiceHandler = new ServiceHandler(mServiceLooper);

        Message message = mServiceHandler.obtainMessage();
        message.what = UPDATE_MASTER_LIST;
        mServiceHandler.sendMessage(message);
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
        intentFilter.addAction(ACTION_GET_MASTER_LIST);
        localBroadcastManager.registerReceiver(serviceBroadcastReceiver, intentFilter);
        Log.i(TAG, "onStartCommand finished");

        FileShareSocketServer socketServer = FileShareSocketServer.getInstance();
        socketServer.startServer(getApplicationContext());
        flags = START_STICKY;
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(TAG, "onTaskRemoved");
        Intent restartService = new Intent(getApplicationContext(), this.getClass());
        restartService.setPackage(getPackageName());
        PendingIntent restartServicePI = PendingIntent.getService(
                getApplicationContext(),
                1,
                restartService,
                PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarmService.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartServicePI);
    }

    public class ServiceBroadcastReceiver  extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "message:" + ACTION_GET_MASTER_LIST);
            if (intent.getAction().equals(ACTION_GET_MASTER_LIST)) {

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
                case COMMAND_START_SERVER:
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

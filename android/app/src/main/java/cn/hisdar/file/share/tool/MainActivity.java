package cn.hisdar.file.share.tool;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import cn.hisdar.file.share.tool.common.FileShareTimer;
import cn.hisdar.file.share.tool.common.FileShareTimerAdapter;
import cn.hisdar.file.share.tool.service.FileShareService;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static String TAG = "FileShareTool";

    private final static long REMOTE_SEARCH_TOTAL_TIME = 20000;
    private final static int  REMOTE_SEARCH_TIME_STEP  = 1000;

    private Button searchButton;
    private boolean isLookingForRemotes;
    private FileShareTimer remoteSearchTimer;
    private LocalBroadcastManager localBroadcastManager;
    private RemoteSearchTimerEventHandler remoteSearchTimerEventHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());

        isLookingForRemotes = false;
        searchButton = findViewById(R.id.search_master_button);
        searchButton.setOnClickListener(this);

        Intent intent = new Intent(this, FileShareService.class);
        startService(intent);
    }

    public void stopSearchRemotes() {
        isLookingForRemotes = false;
        searchButton.setText("搜索");
        Intent intent = new Intent();
        intent.setAction(FileShareService.ACTION_STOP_GET_REMOTES_LIST);
        localBroadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == searchButton.getId()) {
            if (!isLookingForRemotes) {
                Log.i(TAG, "start search remotes");
                isLookingForRemotes = true;
                searchButton.setText("停止搜索");
                Intent intent = new Intent();
                intent.setAction(FileShareService.ACTION_START_GET_REMOTES_LIST);
                localBroadcastManager.sendBroadcast(intent);

                remoteSearchTimer = new FileShareTimer(REMOTE_SEARCH_TOTAL_TIME, REMOTE_SEARCH_TIME_STEP);
                remoteSearchTimerEventHandler = new RemoteSearchTimerEventHandler();
                remoteSearchTimer.addListener(remoteSearchTimerEventHandler);
                remoteSearchTimer.startTimer();
            } else {
                Log.i(TAG, "stop search remotes");
                remoteSearchTimer.stopTimer();
            }
        }
    }

    private class RemoteSearchTimerEventHandler extends FileShareTimerAdapter {
        @Override
        public void timerStepEvent(int step) {
            if (step * REMOTE_SEARCH_TIME_STEP == REMOTE_SEARCH_TOTAL_TIME) {
                stopSearchRemotes();
                return;
            }

            int leftStep = (int)(REMOTE_SEARCH_TOTAL_TIME / REMOTE_SEARCH_TIME_STEP) - step;
            searchButton.setText("停止搜索(" + leftStep + ")");
        }

        @Override
        public void timerDone() {
            stopSearchRemotes();
        }
    }

    private class ActiveBroadcastReceiver  extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}

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

import cn.hisdar.file.share.tool.service.FileShareService;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private Button searchButton;
    private static String TAG = "FileShareTool";
    private LocalBroadcastManager localBroadcastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        searchButton = findViewById(R.id.search_master_button);
        searchButton.setOnClickListener(this);

        Intent intent = new Intent(this, FileShareService.class);
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == searchButton.getId()) {
            Log.i(TAG, "send message:" + FileShareService.ACTION_GET_MASTER_LIST);
            Intent intent = new Intent();
            intent.setAction(FileShareService.ACTION_GET_MASTER_LIST);
            intent.putExtra("change", "yes");
            localBroadcastManager.sendBroadcast(intent);
        }
    }

    private class ActiveBroadcastReceiver  extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }
}

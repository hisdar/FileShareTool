package cn.hisdar.file.share.tool.command;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class GetDeviceInfoCommand extends Command {

    private final static String TAG = "FileShareTool";

    private Context mContext;

    public GetDeviceInfoCommand(Context context) {
        mContext = context;
    }

    public String generateCommand(String cmdType, String cmd) {
        String deviceName = Build.MODEL;
        String innerSDcard = getInnerSDcardPath();
        ArrayList<String> externalSDCards = getExternalSDCardPath(mContext);

        String cmdString = "<DeviceInfo>\n";
        cmdString += "<DeviceName>" + deviceName + "</DeviceName>\n";
        cmdString += "<InnerSdcardPath>" + innerSDcard + "</InnerSdcardPath>\n";
        cmdString += "<ExternalSdcardCount>" + externalSDCards.size() + "</ExternalSdcardCount>\n";

        int sdcardIndex = 0;
        for (int i = 0; i < externalSDCards.size(); i++) {
            String sdcardPath = externalSDCards.get(i);
            File sdcard = new File(sdcardPath);
            if (!(sdcard.exists() && sdcard.canRead())) {
                continue;
            }

            String key = "ExternalSdcardPath" + sdcardIndex;

            cmdString += "<" + key +">" + sdcardPath + "</" + key + ">\n";
            sdcardIndex++;
        }

        cmdString += "</DeviceInfo>\n";

        Log.i(TAG, cmdString);

        String basicData = getFormatedCommandType(cmdType);
        basicData += getFormatedCommand(cmd);
        basicData += Command.COMMAND_EXEC_RESULT_SUCCESS;
        cmdString = basicData + cmdString;
        cmdString = addCommandHeadAndTail(cmdString);

        return cmdString;
    }

    public String getInnerSDcardPath() {
        String storagePath = null;
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            storagePath = Environment.getExternalStorageDirectory().getPath();
        }

        return storagePath;
    }

    public ArrayList<String> getExternalSDCardPath(Context mContext) {
        String innerSDcardPath = getInnerSDcardPath();
        ArrayList<String> externalSDCards = new ArrayList<>();

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            //Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object resultObject = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(resultObject);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(resultObject, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                //boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (path.equals(innerSDcardPath)) {
                    continue;
                }
                externalSDCards.add(path);
                Log.i(TAG, path);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return externalSDCards;
    }
}

package cn.hisdar.file.share.tool.command;

import android.os.Build;
import android.os.Environment;

public class GetDeviceInfoCommand extends Command {
    public GetDeviceInfoCommand() {

    }

    public String generateCommand(String cmdType, String cmd) {
        String deviceName = Build.MODEL;

        String storagePath = "NULL";
        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            storagePath = Environment.getExternalStorageDirectory().getPath();
        }

        String cmdString = "<DeviceInfo>\n";
        cmdString += "<DeviceName>" + deviceName + "</DeviceName>\n";
        cmdString += "<InnerSdcardPath>" + storagePath + "</InnerSdcardPath>\n";
        cmdString += "</DeviceInfo>\n";

        String basicData = getFormatedCommandType(cmdType);
        basicData += getFormatedCommand(cmd);
        basicData += Command.COMMAND_EXEC_RESULT_SUCCESS;
        cmdString = basicData + cmdString;
        cmdString = addCommandHeadAndTail(cmdString);

        return cmdString;
    }
}

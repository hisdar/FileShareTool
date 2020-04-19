package cn.hisdar.file.share.tool.command;

import android.util.Log;

import java.util.HashMap;

public class Command {

    public static String COMMAND_EXEC_RESULT_SUCCESS = "<Result>Success</Result>\n";
    public static String COMMAND_EXEC_RESULT_FAIL    = "<Result>Fail</Result>\n";

    public static String COMMAND_TYPE_KEY = "CommandType";
    public static String COMMAND_KEY      = "Command";

    public static String COMMAND_TYPE_REQUEST  = "Request";
    public static String COMMAND_TYPE_RESPONSE = "Response";

    public static String COMMAND_GET_DEVICE_INFO = "GetDeviceInfo";
    public static String COMMAND_GET_CHILD_FILES = "GetChildFiles";
    public static String COMMAND_GET_FILE        = "GetFile";

    private String TAG = "FileShareCommand";
    private HashMap<String, String> cmdItems;

    public Command() {
        cmdItems = new HashMap<>();
    }

    public String getCommandType() {
        return cmdItems.get(COMMAND_TYPE_KEY);
    }

    public String getCommand() {
        return cmdItems.get(COMMAND_KEY);
    }

    public String getCommandItem(String key) {
        return cmdItems.get(key);
    }

    public boolean parseCommand(StringBuffer stringBuffer) {
        int startIndex = 0;
        int endIndex = 0;
        while (true) {
            startIndex = stringBuffer.indexOf("<", endIndex) + 1;
            if (startIndex < 0) {
                break;
            }

            endIndex = stringBuffer.indexOf(">", startIndex);
            if (endIndex < 0) {
                break;
            }

            String key = stringBuffer.substring(startIndex, endIndex);
            String endString = "</" + key + ">";

            // get the value
            startIndex = endIndex + 1;
            endIndex = stringBuffer.indexOf(endString);
            if (endIndex < 0) {
                break;
            }

            String value = stringBuffer.substring(startIndex, endIndex);
            cmdItems.put(key, value);

            endIndex += endString.length();
        }

        return true;
    }


    protected String addCommandHeadAndTail(String commandData) {
        String result = "<HisdarSocketCommand>\n" + commandData + "</HisdarSocketCommand>\n";
        return result;
    }

    protected String getFormatedCommandType(String commandType) {
        String result = "<CommandType>" + commandType + "</CommandType>\n";
        return result;
    }

    protected String getFormatedCommand(String command) {
        String result = "<Command>" + command + "</Command>\n";
        return result;
    }
}

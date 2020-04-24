package cn.hisdar.file.share.tool.command;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import cn.hisdar.lib.log.HLog;

public class Command {
	public final static int COMMAND_ERROR_IO = 0x4001;

	private HashMap<String, String> cmdItems;
	
	public static String COMMAND_TYPE_REQUEST = "Request";
	public static String COMMAND_TYPE_RESOPNSE = "Response";
	
	public static String COMMAND_GET_DEVICE_INFO = "GetDeviceInfo";
	public static String COMMAND_GET_CHILD_FILES = "GetChildFiles";
	public static String COMMAND_GET_FILE        = "GetFile";
	public static String COMMAND_PUT_FILE        = "PutFile";
	
	public Command() {
		cmdItems = new HashMap<>();
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
	
	public boolean parseCommand(StringBuffer stringBuffer) {
		int startIndex = 0;
        int endIndex = 0;
        while (true) {
            startIndex = stringBuffer.indexOf("<", endIndex);
            if (startIndex < 0) {
            	break;
            }
            
            startIndex += 1; // skip "<"

            endIndex = stringBuffer.indexOf(">", startIndex);
            if (endIndex < 0) {
            	HLog.el("no > found");
                break;
            }

            String key = stringBuffer.substring(startIndex, endIndex);
            String endString = "</" + key + ">";

            // get the value
            startIndex = endIndex + 1;
            endIndex = stringBuffer.indexOf(endString);
            if (endIndex < 0) {
            	HLog.el("no " + endString + " found");
                break;
            }

            String value = stringBuffer.substring(startIndex, endIndex);
            cmdItems.put(key, value);

            endIndex += endString.length();
        }
        
        return true;
	}
	
    public String getCommandItem(String key) {
        return cmdItems.get(key);
    }
    
    public HashMap<String, String> getCmdItems() {
    	return cmdItems;
    }
	
    public boolean isResponse() {
    	if (getCommandItem("CommandType").equals(COMMAND_TYPE_RESOPNSE)) {
    		return true;
    	}
    	return false;
    }
    
    public String getCommand() {
    	return getCommandItem("Command");
    }
    
	public String getCommandString() {
		return null;
	}

	@Override
	public String toString() {
		
		String result = "";
		Iterator<Entry<String, String>> iter = cmdItems.entrySet().iterator();
		while (iter.hasNext()) {
			HashMap.Entry<String, String> entry = (HashMap.Entry<String, String>) iter.next();
			String key = entry.getKey();
			String val = entry.getValue();
			result = result + key + ":" + val + "\n";
		}
		
		return result;
	}
	
	
}

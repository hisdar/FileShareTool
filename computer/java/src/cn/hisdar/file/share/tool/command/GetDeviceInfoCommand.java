package cn.hisdar.file.share.tool.command;

import com.sun.jndi.toolkit.ctx.StringHeadTail;

public class GetDeviceInfoCommand extends Command {

	public GetDeviceInfoCommand() {
		
	}

	public String getDeviceName() {
		return getCommandItem("DeviceName");
	}
	
	public String getInnerSdcardPath() {
		return getCommandItem("InnerSdcardPath");
	}
	
	@Override
	public String getCommandString() {
		String command = "";
		command += getFormatedCommandType(COMMAND_TYPE_REQUEST);
		command += getFormatedCommand(COMMAND_GET_DEVICE_INFO);
		command = addCommandHeadAndTail(command);
		return command;
	}
}

package cn.hisdar.file.share.tool.command;

import cn.hisdar.file.share.tool.server.Device;
import cn.hisdar.lib.log.HLog;

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
	public int exec(Device dev) {
		
		String command = "";
		command += getFormatedCommandType(COMMAND_TYPE_REQUEST);
		command += getFormatedCommand(COMMAND_GET_DEVICE_INFO);
		command = addCommandHeadAndTail(command);
		
		Command response = dev.writeAndWaitResponse(command.getBytes());
		String deviceInfo = response.getCommandItem("DeviceInfo");
		parseCommand(new StringBuffer(deviceInfo));
		return 0;
	}
}

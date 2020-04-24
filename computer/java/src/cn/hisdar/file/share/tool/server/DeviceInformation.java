package cn.hisdar.file.share.tool.server;

import java.util.ArrayList;

import cn.hisdar.file.share.tool.command.Command;

public class DeviceInformation {

	private String deviceName;
	private String innersdcardPath;
	
	private int externalSdcardCount;
	private ArrayList<String> externalSdcards;
	
	public DeviceInformation() {
		deviceName = null;
		innersdcardPath = null;
		
		externalSdcardCount = 0;
		externalSdcards = new ArrayList<>();
	}
	
	public void parse(Command command) {
		String deviceInfo = command.getCommandItem("DeviceInfo");
		Command response = new Command();
		response.parseCommand(new StringBuffer(deviceInfo));
		deviceName = response.getCommandItem("DeviceName");
		innersdcardPath = response.getCommandItem("InnerSdcardPath");
		externalSdcardCount = getExternalSdcardCount(response);
		externalSdcards = getExternalSdcards(response);
	}
	
	public String getDeviceName() {
		return deviceName;
	}
	
	public String getInnerSDCardPath() {
		return innersdcardPath;
	}
	
	public int getExternalSdcardCount() {
		return externalSdcardCount;
	}
	
	public ArrayList<String> getExternalSdcards() {
		return externalSdcards;
	}
	
	//////////////////////////////////////////////////////////////////////////////
	private int getExternalSdcardCount(Command cmd) {
		String countString = cmd.getCommandItem("ExternalSdcardCount");
		try {
			return Integer.parseInt(countString);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
	private String getExternalSdcardPath(Command cmd) {
		return getExternalSdcardPath(cmd, 0);
	}
	
	private String getExternalSdcardPath(Command cmd, int index) {
		if (getExternalSdcardCount(cmd) > index) {
			String key = "ExternalSdcardPath" + index;
			return cmd.getCommandItem(key);
		}
		
		return null;
	}
	
	private ArrayList<String> getExternalSdcards(Command cmd) {
		ArrayList<String> externalSdcards = new ArrayList<>();
		
		int externalSdcardCount = getExternalSdcardCount(cmd);
		for (int i = 0; i < externalSdcardCount; i++) {
			externalSdcards.add(getExternalSdcardPath(cmd, i));
		}
		
		return externalSdcards;
	}
}

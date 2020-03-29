package cn.hisdar.file.share.tool.server;

public class DeviceDetails {

	private String ipAddress;
	private String name;
	private String platform;
	
	public DeviceDetails() {

	}
	
	public DeviceDetails(String ip, String name, String platform) {
		this.ipAddress = ip;
		this.name = name;
		this.platform = platform;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}
	

	
}

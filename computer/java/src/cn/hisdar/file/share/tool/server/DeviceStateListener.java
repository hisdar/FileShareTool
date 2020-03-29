package cn.hisdar.file.share.tool.server;

public interface DeviceStateListener {

	public void deviceOnline(String ipAddress);
	public void deviceOffline(String ipAddress);
}

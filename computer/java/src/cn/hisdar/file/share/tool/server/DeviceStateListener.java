package cn.hisdar.file.share.tool.server;

public interface DeviceStateListener {

	public void deviceOnline(Device dev);
	public void deviceOffline(Device dev);
	
	public void deviceConnected(Device dev);
	public void deviceDisconnected(Device dev);
}

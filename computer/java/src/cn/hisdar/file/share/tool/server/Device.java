package cn.hisdar.file.share.tool.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

import cn.hisdar.file.share.tool.command.Command;
import cn.hisdar.file.share.tool.command.GetChildFilesCommand;
import cn.hisdar.file.share.tool.command.GetDeviceInfoCommand;
import cn.hisdar.file.share.tool.command.GetFileCommand;
import cn.hisdar.file.share.tool.command.PutFileCommand;
import cn.hisdar.file.share.tool.command.RemoteFile;
import cn.hisdar.lib.log.HLog;

public class Device {

	private String ipAddress;
	private DataSocket dataSocket;
	private CommandSocket commandSocket;
	
	private DeviceInformation deviceInfo;

	public Device() {
		ipAddress = null;
		commandSocket = null;
		
		deviceInfo = null;
	}
	
	public boolean connect(String ip) {
		if (ip == null) {
			HLog.il("ipaddress is null");
			return false;
		}

		ipAddress = ip;

        try {
        	Socket data = new Socket();
        	Socket command = new Socket();

        	SocketAddress commandAddress = new InetSocketAddress(ipAddress, 5299);
        	SocketAddress dataAddress = new InetSocketAddress(ipAddress, 5300);
        	
        	data.connect(dataAddress, 500);
        	command.connect(commandAddress, 500);
        	
    		dataSocket = new DataSocket(data);
    		commandSocket = new CommandSocket(command);
        } catch (IOException e) {
        	// connect timeout means the host is not exist
			return false;
		}
                
        return true;
	}
	
	public String getIPAddress() {
		return ipAddress;
	}
	
	public ArrayList<RemoteFile> getChildFiles(String parentPath) {
		GetChildFilesCommand getChildFilesCommand = new GetChildFilesCommand(parentPath);
		Command result = writeAndWaitResponse(getChildFilesCommand);
		
		return getChildFilesCommand.parseChileFiles(result);
	}
	
	private void initDeviceInformation() {
		GetDeviceInfoCommand request = new GetDeviceInfoCommand();
		Command result = writeAndWaitResponse(request);

		deviceInfo = new DeviceInformation();
		deviceInfo.parse(result);
	}
	
	public DeviceInformation getDeviceInformation() {
		if (deviceInfo == null) {
			initDeviceInformation();
		}
		
		return deviceInfo;
	}
	
	public boolean getFile(String filePath, String savePath) {
		GetFileCommand request = new GetFileCommand(filePath);
		Command response = writeAndWaitResponse(request);
		String fileLengthString = response.getCommandItem("FileLength");
		int fileLength = Integer.parseInt(fileLengthString);
		
		HLog.il("file length:" + fileLength);
		boolean bRet = dataSocket.receive(fileLength, savePath);
		if (!bRet) {
			HLog.il("get file fail, path:" + filePath);
			return false;
		}
		
		HLog.il("get file <" + filePath + "> success, saved to <" + savePath + ">");
		
		return true;
	}

	public boolean putFile(String srcPath, String tagPath) {
		PutFileCommand request = new PutFileCommand(srcPath, tagPath);
		writeAndWaitResponse(request);
		HLog.il("start to send file");
		dataSocket.send(srcPath);
		HLog.il("save file success");
		
		return true;
	}
	
	public boolean connect() {
		DeviceSearcher.getInstance().notifyDeviceConnectState(this, true);
		return true;
	}
	
	public Command writeAndWaitResponse(Command cmd) {
		boolean bRet = commandSocket.writeCommand(cmd);
		if (!bRet) {
			HLog.il("write command fail");
			return null;
		}
		
		return commandSocket.receive();
	}	
}

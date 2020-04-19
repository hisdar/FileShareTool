package cn.hisdar.file.share.tool.server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Semaphore;

import cn.hisdar.file.share.tool.command.Command;
import cn.hisdar.file.share.tool.command.CommandOutputStream;
import cn.hisdar.file.share.tool.command.GetDeviceInfoCommand;
import cn.hisdar.lib.log.HLog;

public class Device {

	private String ipAddress;
	private Socket dataSocekt;
	private Socket commandSocket;
	private InputStream  inputStream;
	private OutputStream outputStream;
	
	private InputStream dataInputStream;

	private Semaphore writeLock;
	private Semaphore responseLock;
	private Command responseCommand;
	private CommandReadThread commandReadThread;

	public Device() {
		ipAddress = null;
		dataSocekt = null;
		commandSocket = null;


		inputStream = null;
		outputStream = null;
		
		dataInputStream = null;

		writeLock = null;
		responseLock = null;
		responseCommand = null;
		
		commandReadThread = null;
	}
	
	public boolean connect(String ip) {
		ipAddress = ip;
		commandSocket = new Socket();
		dataSocekt = new Socket();

        try {
        	SocketAddress commandAddress = new InetSocketAddress(ipAddress, 5299);
        	SocketAddress dataAddress = new InetSocketAddress(ipAddress, 5300);
        	commandSocket.connect(commandAddress, 500);
        	dataSocekt.connect(dataAddress, 500);
        	
    		writeLock = new Semaphore(1);
    		responseLock = new Semaphore(1);
    		try {
    			responseLock.acquire();
    		} catch (InterruptedException e) {}
    		
    		inputStream = commandSocket.getInputStream();
    		outputStream = commandSocket.getOutputStream();
    		
    		dataInputStream = dataSocekt.getInputStream();
    		
    		commandReadThread = new CommandReadThread();
    		commandReadThread.start();
        } catch (IOException e) {
			return false;
		}
        
        return true;
	}
	
	public String getIPAddress() {
		return ipAddress;
	}
	
	public String getName() {
		GetDeviceInfoCommand deviceInfo = new GetDeviceInfoCommand();
		deviceInfo.exec(this);
		
		return deviceInfo.getDeviceName();
	}

	public boolean connect() {
		
		DeviceSearcher.getInstance().notifyDeviceConnectState(this, true);
		return true;
	}
	
	public Command writeAndWaitResponse(byte[] data) {
		
		responseCommand = null;
		writeCommand(data);
		while (true) {
			try {
				responseLock.acquire();
				if (responseCommand != null) {
					break;
				}
				
				writeCommand(data);
			} catch (InterruptedException e) {
				continue;
			}
		}
		
		return responseCommand;
	}
	
	private int writeCommand(byte[] data) {
		while (true) {
			try {
				writeLock.acquire();
				break;
			} catch (InterruptedException e) {
				continue;
			}
		}
		
		if (outputStream != null) {
			CommandOutputStream commandOutputStream = new CommandOutputStream(outputStream);

			commandOutputStream.write(data);
			commandOutputStream.flush();
		}
		
		writeLock.release();
		
		return 0;
	}
		
	private void notifyDeviceOffline() {
		DeviceSearcher.getInstance().notifyDeviceConnectState(this, false);
		DeviceSearcher.getInstance().notifyDeviceStete(this, false);
	}
		
	private class CommandReadThread extends Thread {
		
		public CommandReadThread() {
			
		}
		
		public void run() {
			HLog.il("InStreamReaderThread started");
			
			String lineString = null;
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			
	        while (true) {
	            try {
	                lineString = reader.readLine();
	                if (lineString == null) {
	                	if (commandSocket.isClosed() || !commandSocket.isConnected()) {
	                		HLog.il("exit thread");
	                		break;
	                	}
	                    continue;
	                }

	                if (!lineString.trim().equals("<HisdarSocketCommand>")) {
	                    continue;
	                }

	                StringBuffer commandStringBuffer = new StringBuffer();
	                while (true) {
	                    lineString = reader.readLine();
	                    if (lineString.trim().equals("</HisdarSocketCommand>")) {
	                        break;
	                    }

	                    commandStringBuffer.append(lineString);
	                    commandStringBuffer.append("\n");
	                }

	                Command command = new Command();
	                command.parseCommand(commandStringBuffer);
	                if (command.isResponse() && command.getCommand().equals(Command.COMMAND_GET_FILE)) {
	                	String fileLengthString = command.getCommandItem("FileLength");
	                	long fileLength = Long.parseLong(fileLengthString);
	                	byte[] cbuf = new byte[1024];
	                	int readCount = 0;
	                	while (readCount < fileLength) {
	                		readCount += dataInputStream.read(cbuf);
	                		HLog.il("get file success, readCount:" + readCount + ", fileLength:" + fileLength);
	                	}
	                }
	                
	                responseCommand = command;
	                responseLock.release();
	            } catch (IOException e) {
	                e.printStackTrace();
	                break;
	            }
	        }

			notifyDeviceOffline();
			HLog.il("InStreamReaderThread end");
		}
	}
}

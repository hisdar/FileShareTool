package cn.hisdar.file.share.tool.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import cn.hisdar.file.share.tool.command.Command;
import cn.hisdar.lib.log.HLog;

public class CommandSocket {

	private Socket socket;
	private InputStream inputStream;
	private OutputStream outputStream;

	private BufferedReader bufferedReader;
	
	public CommandSocket(Socket commandSocket) {
		socket = commandSocket;
		if (socket == null) {
			inputStream = null;
			bufferedReader = null;
			outputStream = null;
		} else {
			try {
				inputStream = socket.getInputStream();
				outputStream = socket.getOutputStream();
				bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
			} catch (IOException e) {
				HLog.el(e);
			}
		}
	}
	
	public boolean writeCommand(Command cmd) {
		String commandString = cmd.getCommandString();
		if (commandString == null) {
			HLog.el("commandString is null");
			return false;
		}
		
		try {
			outputStream.write(commandString.getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			HLog.el(e);
			return false;
		}
		
		return true;
	}
	
	public Command receive() {
		if (bufferedReader == null) {
			return null;
		}
		
		try {
			String lineString = null;
			Command command = new Command();
			while (true) {
            
                lineString = bufferedReader.readLine();
                if (lineString == null) {
                    continue;
                }

                if (!lineString.trim().equals("<HisdarSocketCommand>")) {
                    continue;
                }

                StringBuffer commandStringBuffer = new StringBuffer();
                while (true) {
                    lineString = bufferedReader.readLine();
                    if (lineString.trim().equals("</HisdarSocketCommand>")) {
                        break;
                    }

                    commandStringBuffer.append(lineString);
                    commandStringBuffer.append("\n");
                }

                command.parseCommand(commandStringBuffer);
                return command;
	        }
        } catch (IOException e) {
            HLog.el(e);
            return null;
        }
	}
}

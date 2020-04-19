package cn.hisdar.file.share.tool.command;

import java.nio.charset.StandardCharsets;

import cn.hisdar.file.share.tool.server.Device;

public class GetFileCommand extends Command {

	private String srcPath;
	private String savePath;
	
	public GetFileCommand(String srcPath, String savePath) {
		this.srcPath = srcPath;
		this.savePath = savePath;
	}
	
	public int exec(Device dev) {
		String cmdString = getFormatedCommandType(COMMAND_TYPE_REQUEST);
		cmdString += getFormatedCommand(COMMAND_GET_FILE);
		cmdString += "<SrcPath>" + srcPath + "</SrcPath>\n";
		cmdString += "<SavePath>" + savePath + "</SavePath>\n";
		cmdString = addCommandHeadAndTail(cmdString);
		
		Command response = dev.writeAndWaitResponse(cmdString.getBytes(StandardCharsets.UTF_8));
		
		return 0;
	}
}

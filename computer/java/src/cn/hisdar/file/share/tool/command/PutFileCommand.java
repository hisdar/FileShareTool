package cn.hisdar.file.share.tool.command;

import java.io.File;

public class PutFileCommand extends Command {
	private String srcPath;
	private String tagPath;
	
	public PutFileCommand(String srcPath, String tagPath) {
		this.srcPath = srcPath;
		this.tagPath = tagPath;
	}
	
	public String getCommandString() {
		long fileLength = new File(srcPath).length();
		String cmdString = getFormatedCommandType(COMMAND_TYPE_REQUEST);
		cmdString += getFormatedCommand(COMMAND_PUT_FILE);
		cmdString += "<SrcPath>" + srcPath + "</SrcPath>\n";
		cmdString += "<TagPath>" + tagPath + "</TagPath>\n";
		cmdString += "<FileLength>" + fileLength + "</FileLength>\n";
		cmdString = addCommandHeadAndTail(cmdString);
		
		return cmdString;
	}
}

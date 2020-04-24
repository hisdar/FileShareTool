package cn.hisdar.file.share.tool.command;

public class GetFileCommand extends Command {

	private String srcPath;
	
	public GetFileCommand(String srcPath) {
		this.srcPath = srcPath;
	}
	
	public String getCommandString() {
		String cmdString = getFormatedCommandType(COMMAND_TYPE_REQUEST);
		cmdString += getFormatedCommand(COMMAND_GET_FILE);
		cmdString += "<SrcPath>" + srcPath + "</SrcPath>\n";
		cmdString = addCommandHeadAndTail(cmdString);
		
		return cmdString;
	}
}

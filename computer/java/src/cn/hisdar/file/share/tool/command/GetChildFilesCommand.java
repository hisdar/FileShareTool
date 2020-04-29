package cn.hisdar.file.share.tool.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import cn.hisdar.file.share.tool.server.Device;
import cn.hisdar.lib.log.HLog;

public class GetChildFilesCommand extends Command {
	
	private String directoryPath;
	
	public GetChildFilesCommand(String path) {
		directoryPath = path;
	}
	
	public String getCommandString() {
		String command = "";
		command += getFormatedCommandType(COMMAND_TYPE_REQUEST);
		command += getFormatedCommand(COMMAND_GET_CHILD_FILES);
		command += "<DirectoryPath>" + directoryPath + "</DirectoryPath>\n";
		command = addCommandHeadAndTail(command);
		return command;
	}
	
	public ArrayList<RemoteFile> parseChileFiles(Command result) {
		
		ArrayList<RemoteFile> childFileList = new ArrayList<>();
		String childFiles = result.getCommandItem("ChildFiles");
		
		Command childFilesPaser = new Command();
		childFilesPaser.parseCommand(new StringBuffer(childFiles));
		HashMap<String, String> childFilesMap = childFilesPaser.getCmdItems();
		Iterator<Entry<String, String>> iter = childFilesMap.entrySet().iterator();
		while (iter.hasNext()) {
			HashMap.Entry<String, String> entry = (HashMap.Entry<String, String>) iter.next();
			String key = entry.getKey();
			String val = entry.getValue();
			RemoteFile file = new RemoteFile(key, directoryPath);
			parseRemoteFileAttr(file, val);
			childFileList.add(file);
		}
		
		return childFileList;
	}
	
	private void parseRemoteFileAttr(RemoteFile file, String src) {
		Command attrParser = new Command();
		attrParser.parseCommand(new StringBuffer(src));
		file.setFileType(attrParser.getCommandItem("FileType"));
		file.setHidden(Boolean.parseBoolean(attrParser.getCommandItem("IsHidden")));
		file.setLastModified(Long.parseLong(attrParser.getCommandItem("LastModified")));
		file.setLength(Long.parseLong(attrParser.getCommandItem("Length")));
		file.setCanRead(Boolean.parseBoolean(attrParser.getCommandItem("CanRead")));
		file.setCanWrite(Boolean.parseBoolean(attrParser.getCommandItem("CanWrite")));
		file.setCanExecute(Boolean.parseBoolean(attrParser.getCommandItem("CanExecute")));
	}
}

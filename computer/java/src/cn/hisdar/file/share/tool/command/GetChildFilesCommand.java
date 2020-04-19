package cn.hisdar.file.share.tool.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import cn.hisdar.file.share.tool.server.Device;
import cn.hisdar.lib.log.HLog;

public class GetChildFilesCommand extends Command {
	
	private String directoryPath;
	private ArrayList<RemoteFile> childFileList;
	
	public GetChildFilesCommand(String path) {
		childFileList = new ArrayList<>();
		directoryPath = path;
	}
	
	/***
	 * Execute function exec first befor run getChildFileList
	 * @return
	 */
	public ArrayList<RemoteFile> getChildFileList() {
		return childFileList;
	}
	
	public int exec(Device dev) {
		String command = "";
		command += getFormatedCommandType(COMMAND_TYPE_REQUEST);
		command += getFormatedCommand(COMMAND_GET_CHILD_FILES);
		command += "<DirectoryPath>" + directoryPath + "</DirectoryPath>\n";
		command = addCommandHeadAndTail(command);
		
		Command response = dev.writeAndWaitResponse(command.getBytes());
		String childFiles = response.getCommandItem("ChildFiles");
		parseChileFiles(childFiles);
		
		return 0;
	}
	
	private void parseChileFiles(String src) {
		Command childFilesPaser = new Command();
		childFilesPaser.parseCommand(new StringBuffer(src));
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

package cn.hisdar.file.share.tool.view.explorer;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import cn.hisdar.file.share.tool.command.GetChildFilesCommand;
import cn.hisdar.file.share.tool.command.GetDeviceInfoCommand;
import cn.hisdar.file.share.tool.command.GetFileCommand;
import cn.hisdar.file.share.tool.command.RemoteFile;
import cn.hisdar.file.share.tool.server.Device;
import cn.hisdar.file.share.tool.server.DeviceStateListener;
import cn.hisdar.file.share.tool.server.DeviceSearcher;
import cn.hisdar.lib.log.HLog;
import cn.hisdar.lib.ui.HLinearPanel;

public class ExplorerView extends JPanel implements DeviceStateListener, RemoteFileEventListener {

	private static final long serialVersionUID = 1L;
	
	private Device device;
	private HLinearPanel fileInforPanel;
	private ExplorerTitlePanel explorerTitlePanel;
	private ArrayList<ExplorerItemPanel> explorerItemPanels;
	private String[] titles = {"名称", "修改日期", "类型", "大小"};

	private FileSystemService fileSystemService;
	
	public ExplorerView() {
		device = null;
		fileSystemService = new FileSystemService();
		fileSystemService.start();

		fileInforPanel = new HLinearPanel();
		setLayout(new BorderLayout());
		
		explorerTitlePanel = new ExplorerTitlePanel(titles);
		explorerItemPanels = new ArrayList<>();

		JScrollPane viewScrollPanel = new JScrollPane(fileInforPanel);
		JScrollBar verticalBar = viewScrollPanel.getVerticalScrollBar();
		verticalBar.setUnitIncrement(10);
		
		add(explorerTitlePanel, BorderLayout.NORTH);
		add(viewScrollPanel, BorderLayout.CENTER);
		DeviceSearcher.getInstance().addDeviceStateListener(this);
	}
	
	@Override
	public void deviceOnline(Device dev) {
		
	}

	@Override
	public void deviceOffline(Device dev) {
		
	}

	@Override
	public void deviceConnected(Device dev) {
		HLog.il("deviceConnected");
		if (device == null) {
			device = dev;
			
			InnerCommand command = new InnerCommand();
			command.command = InnerCommand.COMMAND_SHOW_SDCARDS;
			fileSystemService.addCommand(command);
			fileSystemService.interrupt();
		}
	}

	@Override
	public void deviceDisconnected(Device dev) {
		if (device == dev) {
			clearView();
			device = null;
		}
	}
	
	private class InnerCommand {
		
		public static final int COMMAND_SHOW_SDCARDS = 1;
		
		public int command;
		public Object object;
	}
	
	private class FileSystemService extends Thread {
		
		private boolean exit;
		private ArrayList<InnerCommand> commandList;

		public FileSystemService() {
			exit = false;
			commandList = new ArrayList<>();
		}
		
		public void addCommand(InnerCommand cmd) {
			commandList.add(cmd);
		}
		
		public void run() {
			while (!exit) {
				if (commandList.size() <= 0) {
					try {
						sleep(1000 * 60);
					} catch (InterruptedException e) {}
					continue;
				}
				
				InnerCommand cmd = commandList.get(0);
				commandList.remove(0);
				
				if (cmd.command == InnerCommand.COMMAND_SHOW_SDCARDS) {					
					showSDCardInfo();
				}
			}
		}
	}
	
	private void showSDCardInfo() {
		if (device == null) {
			return;
		}
		
		HLog.il("getSDCardInfo....");
		GetDeviceInfoCommand devInfor = new GetDeviceInfoCommand();
		int ret = devInfor.exec(device);
		if (ret != 0) {
			return;
		}
		
		String sdcardPath = devInfor.getInnerSdcardPath();
		if (sdcardPath != null) {
			showFilesInDirectory(sdcardPath);
		}
	}
	
	private ArrayList<RemoteFile> sortFilesByName(ArrayList<RemoteFile> fileList) {
		ArrayList<RemoteFile> newFileList = new ArrayList<>();
		
		while (fileList.size() > 0) {
			RemoteFile currentFile = fileList.get(fileList.size() - 1);
			fileList.remove(fileList.size() - 1);
			
			int insertIndex = 0;
			for (int i = 0; i < newFileList.size(); i++) {
				RemoteFile compareFile = newFileList.get(i);
				if (currentFile.getName().compareTo(compareFile.getName()) < 0) {
					insertIndex = i;
					break;
				}
			}
			newFileList.add(insertIndex, currentFile);
		}
		return newFileList;
	}
	
	private ArrayList<RemoteFile> sortFilesByType(ArrayList<RemoteFile> fileList) {
		ArrayList<RemoteFile> newFileList = new ArrayList<>();
		ArrayList<RemoteFile> directorys = new ArrayList<>();
		
		HashMap<String, ArrayList<RemoteFile>> fileTypeMap = new HashMap<>();
		while (fileList.size() > 0) {
			RemoteFile currentFile = fileList.get(fileList.size() - 1);
			fileList.remove(fileList.size() - 1);
			if (currentFile.isDirectory()) {
				directorys.add(currentFile);
			} else {
				String fileTypeString = currentFile.getFileTypeString();
				ArrayList<RemoteFile> currentArray = fileTypeMap.get(fileTypeString);
				if (currentArray == null) {
					currentArray = new ArrayList<>();
					fileTypeMap.put(fileTypeString, currentArray);
				}
				currentArray.add(currentFile);
			}
		}

		newFileList.addAll(sortFilesByName(directorys));
		Iterator<Entry<String, ArrayList<RemoteFile>>> itr = fileTypeMap.entrySet().iterator();
		while (itr.hasNext()) {
			newFileList.addAll(sortFilesByName(itr.next().getValue()));
		}
		return newFileList;
	}

	private void clearView() {
		fileInforPanel.removeAllChilds();
		for (int i = explorerItemPanels.size(); i > 0 ; i--) {
			explorerItemPanels.get(i - 1).removeRemoteFileEventListener(this);
			explorerItemPanels.remove(i - 1);
		}
	}
	
	private void showFilesInDirectory(String path) {
		
		GetChildFilesCommand getChildFilesCommand = new GetChildFilesCommand(path);
		int ret = getChildFilesCommand.exec(device);
		if (ret != 0) {
			return;
		}
		
		clearView();
		
		ArrayList<RemoteFile> childFiles = getChildFilesCommand.getChildFileList();
		if (childFiles.size() <= 0) {
			return;
		}
		
		childFiles = sortFilesByType(childFiles);
		for (int i = 0; i < childFiles.size(); i++) {
			RemoteFile currentFile = childFiles.get(i);
			String fileName = currentFile.getName();
			String filePath = path + "/" + fileName;
			if (fileName.startsWith(".")) {
				continue;
			}
			
			if (currentFile.isHidden()) {
				continue;
			}
			
			ExplorerItemPanel itemPanel = new ExplorerItemPanel(titles.length);
			itemPanel.setFilePath(filePath);
			itemPanel.setRemoteFile(currentFile);
			
			itemPanel.setItemText(fileName, 0);
			itemPanel.setIcon(childFiles.get(i).getIcon());
			itemPanel.setItemText(currentFile.getLastModifiedString(), 1);
			itemPanel.setItemText(getSize(currentFile.getLength()), 3);
			
			if (childFiles.get(i).isDirectory()) {
				itemPanel.setItemText("文件夹", 2);
			} else {
				itemPanel.setItemText(currentFile.getFileTypeString(), 2);
			}

			itemPanel.addRemoteFileEventListener(this);
			explorerItemPanels.add(itemPanel);
			fileInforPanel.add(itemPanel);
		}
	}
	
	private String getSize(long size) {
		String unitArray[] = {"B", "K", "M", "G", "T", "P"};

		int index = 0;
		double result = size;
		while (result > 1024) {
			index++;
			result = result / 1024;
		}
		
		result = ((int)(result * 10) / 10.0);
		String sizeString = (result + " " + unitArray[index]);
		return sizeString;
	}
	
	@Override
	public void remoteFileEvent(RemoteFile file, int event) {
		if (event == ExplorerItemPanel.REMOTE_FILE_EVENT_OPEN) {
			if (file.isDirectory()) {
				showFilesInDirectory(file.getAbsolutePath());
			} else {
				HLog.il("goint to read:" + file.getAbsolutePath());
				GetFileCommand getFileCmd = new GetFileCommand(file.getAbsolutePath(), file.getName());
				getFileCmd.exec(device);
			}
		}
	}
}

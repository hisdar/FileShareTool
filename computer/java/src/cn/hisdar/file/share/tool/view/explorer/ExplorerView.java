package cn.hisdar.file.share.tool.view.explorer;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import com.sun.xml.internal.ws.wsdl.writer.document.http.Address;

import cn.hisdar.file.share.tool.command.RemoteFile;
import cn.hisdar.file.share.tool.server.Device;
import cn.hisdar.file.share.tool.server.DeviceSearcher;
import cn.hisdar.file.share.tool.server.DeviceStateAdapter;
import cn.hisdar.file.share.tool.server.DeviceStateListener;
import cn.hisdar.lib.log.HLog;
import cn.hisdar.lib.ui.HLinearPanel;

public class ExplorerView extends JPanel implements RemoteFileEventListener {

	private static final long serialVersionUID = 1L;
	
	private Device device;
	private ExplorerItemView explorerItemView;
	private ExplorerTitlePanel explorerTitlePanel;
	private String[] titles = {"名称", "修改日期", "类型", "大小"};
	
	private AddressBarEventHandler addressBarEventHandler;
	private DeviceStateEventHandler deviceStateEventHandler;
	private ExplorerTitleEventHandler explorerTitleEventHandler;
	private ExplorerAddressListenerManager addressListenerManager;

	private FileSystemService fileSystemService;
	
	public ExplorerView() {
		device = null;
		addressBarEventHandler = new AddressBarEventHandler();
		deviceStateEventHandler = new DeviceStateEventHandler();
		explorerTitleEventHandler = new ExplorerTitleEventHandler();
		addressListenerManager = new ExplorerAddressListenerManager();
		fileSystemService = new FileSystemService();
		fileSystemService.start();

		setLayout(new BorderLayout());
		
		explorerTitlePanel = new ExplorerTitlePanel(titles);
		explorerTitlePanel.addExplorTitleListener(explorerTitleEventHandler);
		
		explorerItemView = new ExplorerItemView();
		
		add(explorerTitlePanel, BorderLayout.NORTH);
		add(explorerItemView, BorderLayout.CENTER);
		DeviceSearcher.getInstance().addDeviceStateListener(deviceStateEventHandler);
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
	
	private class AddressBarEventHandler implements AddressBarListener {
		@Override
		public void updateAddress(String path) {
			showFilesInDirectory(path);
		}
	}
	
	private class DeviceStateEventHandler extends DeviceStateAdapter {
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
				explorerItemView.removeAllExplorerItem();
				device = null;
			}
		}
	}
	
	private void showSDCardInfo() {
		if (device == null) {
			return;
		}
		
		String sdcardPath = device.getDeviceInformation().getInnerSDCardPath();
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

	private void showFilesInDirectory(String path) {

		if (device == null) {
			HLog.il("device is null");
			return;
		}

		explorerItemView.removeAllExplorerItem();
		addressListenerManager.notifyExplorerAddressEvent(path);
		
		ArrayList<RemoteFile> childFiles = device.getChildFiles(path);		
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
			itemPanel.setItemText(currentFile.getSizeString(), 3);
			
			if (childFiles.get(i).isDirectory()) {
				itemPanel.setItemText("文件夹", 2);
			} else {
				itemPanel.setItemText(currentFile.getFileTypeString(), 2);
			}

			itemPanel.addRemoteFileEventListener(this);
			explorerItemView.addExplorerItem(itemPanel);
		}
	}
	
	@Override
	public void remoteFileEvent(RemoteFile file, int event) {
		if (event == ExplorerItemPanel.REMOTE_FILE_EVENT_OPEN) {
			if (file.isDirectory()) {
				showFilesInDirectory(file.getAbsolutePath());
			} else {
				HLog.il("goint to read:" + file.getAbsolutePath());
				String savePath = "D:/temp/" + file.getName();
				boolean bRet = device.getFile(file.getAbsolutePath(), savePath);
				if (!bRet) {
					HLog.il("get file fail, file:" + file.getAbsolutePath());
					return;
				}
				
				try {
					String cmd = "cmd  /c  start \"\" \"" + savePath + "\"";
					HLog.il(cmd);
					Runtime.getRuntime().exec(cmd);
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
			}
		} else if (event == ExplorerItemPanel.REMOTE_FILE_EVENT_POST) {
			Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
	        Transferable clipTf = sysClip.getContents(null);
	        if (clipTf == null) {
	        	HLog.il("get contents fail");
	        	return;
	        }
	        
	        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable t = clipboard.getContents(null); // 获取粘贴板内数据传输对象
			DataFlavor dataFlavors = DataFlavor.javaFileListFlavor;// 数据对象类型
			if (!t.isDataFlavorSupported(dataFlavors)) {// 类型是否匹配为文件
				HLog.el("unsupported flavor:" + dataFlavors);
				return;
			}
		
			try {
				List<File> filelist = (List<File>)t.getTransferData(dataFlavors);// 拿出粘贴板内文件对象列表
				for (int i = 0; i < filelist.size(); i++) { // 遍历文件列表并复制
					HLog.il("file:" + filelist.get(i).getAbsolutePath());
					String srcPath = filelist.get(i).getAbsolutePath();
					String tagPath = file.getParentPath() + "/" + filelist.get(i).getName();
					
					HLog.il("srcPath:" + srcPath + ", tagPath:" + tagPath);
					device.putFile(srcPath, tagPath);
				}
			} catch (Exception e) {
				HLog.el(e);
			}
		}
	}

	
	public ExplorerAddressListenerManager getExplorerAddressListenerManager() {
		return addressListenerManager;
	}
	
	public AddressBarListener getAddressBarListener() {
		return addressBarEventHandler;
	}
	
	private class ExplorerTitleEventHandler implements ExplorerTitleEventListener {

		@Override
		public void setColumnSize(int[] columnWidthArray) {
			explorerItemView.setColumnWidth(columnWidthArray);
		}

		@Override
		public void autoSetColumnSize(int columnNumber) {
			HLog.il("auto resize column");
		}
	}

}

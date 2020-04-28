package cn.hisdar.file.share.tool.command;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.filechooser.FileSystemView;

import cn.hisdar.file.share.tool.Global;

public class RemoteFile {

    private String FILE_TYPE_FILE      = "File";
    private String FILE_TYPE_DIRECTORY = "Directory";
    private String FILE_TYPE_ERROR     = "Error";
	
	private String name;
	private String parent;
	private String fileType;
	private long   lastModified;
	private long   length;
	private boolean isHidden;
	private boolean canRead;
	private boolean canWrite;
	private boolean canExecute;
	
	public RemoteFile(String name, String parentPath) {
		this.name = name;
		this.parent = parentPath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getParentPath() {
		return parent;
	}

	public String getFileType() {
		return fileType;
	}
	
	public String getFileTypeString() {
		int index = name.lastIndexOf(".");
		if (index < 0 || name.length() == index + 1) {
			return "нд╪Ч";
		}
		
		return name.substring(index + 1);
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public long getLastModified() {
		return lastModified;
	}
	
	public String getLastModifiedString() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY/MM/DD hh:mm:ss");
		return dateFormat.format(new Date(lastModified));
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public boolean isHidden() {
		return isHidden;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public boolean isCanRead() {
		return canRead;
	}

	public void setCanRead(boolean canRead) {
		this.canRead = canRead;
	}

	public boolean isCanWrite() {
		return canWrite;
	}

	public void setCanWrite(boolean canWrite) {
		this.canWrite = canWrite;
	}

	public boolean isCanExecute() {
		return canExecute;
	}

	public void setCanExecute(boolean canExecute) {
		this.canExecute = canExecute;
	}
	
	public boolean isDirectory() {
		if (fileType.equals(FILE_TYPE_DIRECTORY)) {
			return true;
		}
		
		return false;
	}
	
	public String getAbsolutePath() {
		if (parent.endsWith("/")) {
			return parent + name;
		} else {
			return parent + "/" + name;
		}
	}
	
	public Icon getIcon() {
		if (isDirectory()) {
			return getDirectoryIcon();
		}
		
		return getFileIcon(name);
	}
	
	private Icon getDirectoryIcon() {
		File file = new File(".");
		FileSystemView view = FileSystemView.getFileSystemView();
		Icon smallIcon = view.getSystemIcon(file);
		return smallIcon;
	}
	
	private Icon getFileIcon(String fileName) {
		
		File file;
		try {
			file = File.createTempFile("icon", fileName);
			Icon smallIcon = Global.getFileSystemView().getSystemIcon(file);
			//ShellFolder shellFolder = ShellFolder.getShellFolder(file);
			//Icon bigIcon = new ImageIcon(shellFolder.getIcon(true));
			file.delete();
			return smallIcon;
		} catch (IOException e) {
			e.printStackTrace();
		}
	      
		return null;
	}
	
	public String getSizeString() {
		String unitArray[] = {"B", "K", "M", "G", "T", "P"};

		int index = 0;
		double result = length;
		while (result > 1024) {
			index++;
			result = result / 1024;
		}
		
		result = ((int)(result * 10) / 10.0);
		String sizeString = (result + " " + unitArray[index]);
		return sizeString;
	}
}

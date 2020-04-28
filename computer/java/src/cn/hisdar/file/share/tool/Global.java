package cn.hisdar.file.share.tool;

import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.filechooser.FileSystemView;

public class Global {

	private static FileSystemView fileSystemView = null;
	
	public static Font getDefaultFont() {
		Font sysFont = new JLabel().getFont();

		int defSize = (int)(sysFont.getSize() * 1);
		Font defFont = new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, defSize);
		return defFont;
	}
	
	public static FileSystemView getFileSystemView() {
		if (fileSystemView == null) {
			fileSystemView = FileSystemView.getFileSystemView();
		}
		
		return fileSystemView;
	}
}

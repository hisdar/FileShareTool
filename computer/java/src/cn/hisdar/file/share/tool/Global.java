package cn.hisdar.file.share.tool;

import java.awt.Font;

import javax.swing.JLabel;

public class Global {

	
	public static Font getDefaultFont() {
		Font sysFont = new JLabel().getFont();

		int defSize = (int)(sysFont.getSize() * 1);
		Font defFont = new Font("Î¢ÈíÑÅºÚ", Font.PLAIN, defSize);
		return defFont;
	}
}

package cn.hisdar.file.share.tool;

import javax.swing.UIManager;

import cn.hisdar.file.share.tool.view.MainFrame;
import cn.hisdar.lib.log.HLog;

public class main {

	public static void main(String[] args) {
		
		HLog.enableCmdLog();
		try {

			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");//Nimbus风格，jdk6 update10版本以后的才会出现
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());//当前系统风格
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");//Motif风格，是蓝黑
			//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());//跨平台的Java风格
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");//windows风格
			//UIManager.setLookAndFeel("javax.swing.plaf.windows.WindowsLookAndFeel");//windows风格
			//UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");//java风格
			//UIManager.setLookAndFeel("com.apple.mrj.swing.MacLookAndFeel");//待考察，

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Global.getFileSystemView();
		
		MainFrame mainFrame = new MainFrame();
		mainFrame.setVisible(true);
	}
}

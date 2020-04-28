package cn.hisdar.file.share.tool;

import javax.swing.UIManager;

import cn.hisdar.file.share.tool.view.MainFrame;
import cn.hisdar.lib.log.HLog;

public class main {

	public static void main(String[] args) {
		
		HLog.enableCmdLog();
		try {

			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");//Nimbus���jdk6 update10�汾�Ժ�ĲŻ����
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());//��ǰϵͳ���
			//UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");//Motif���������
			//UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());//��ƽ̨��Java���
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");//windows���
			//UIManager.setLookAndFeel("javax.swing.plaf.windows.WindowsLookAndFeel");//windows���
			//UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");//java���
			//UIManager.setLookAndFeel("com.apple.mrj.swing.MacLookAndFeel");//�����죬

		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Global.getFileSystemView();
		
		MainFrame mainFrame = new MainFrame();
		mainFrame.setVisible(true);
	}
}

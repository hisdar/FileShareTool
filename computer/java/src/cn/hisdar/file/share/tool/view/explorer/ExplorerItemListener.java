package cn.hisdar.file.share.tool.view.explorer;

import java.awt.event.MouseEvent;

public interface ExplorerItemListener {

	public void mouseClicked(ExplorerItemPanel explorerItem, MouseEvent e);
	public void mouseEntered(ExplorerItemPanel explorerItem, MouseEvent e);
	public void mouseExited(ExplorerItemPanel explorerItem, MouseEvent e);
}

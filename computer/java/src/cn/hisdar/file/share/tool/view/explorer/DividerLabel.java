package cn.hisdar.file.share.tool.view.explorer;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JLabel;

public class DividerLabel extends JLabel {

	private static final long serialVersionUID = 1L;
	
	private boolean enableMouseEvent;
	private Color dividerColor;
	private MouseEventHandler mouseEventHandler;
	private ArrayList<DividerLabelListener> dividerLabelListeners;
	
	public DividerLabel() {
		super();
		init(true);
	}
	
	public DividerLabel(boolean enableMouseEvent) {
		super();
		init(enableMouseEvent);
	}
	
	private void init(boolean enableMouseEvent) {
		this.enableMouseEvent = enableMouseEvent;

		dividerColor = new Color(0x888888);
		mouseEventHandler = new MouseEventHandler(this);
		if (enableMouseEvent) {
			addMouseListener(mouseEventHandler);
			addMouseMotionListener(mouseEventHandler);
		}
		dividerLabelListeners = new ArrayList<>();
	}
	
	public void setDividerColor(Color color) {
		dividerColor = color;
	}
	
	public void addDividerLabelListener(DividerLabelListener l) {
		for (int i = 0; i < dividerLabelListeners.size(); i++) {
			if (dividerLabelListeners.get(i) == l) {
				return;
			}
		}
		
		dividerLabelListeners.add(l);
	}
	
	public void deleteDividerLabelListener(DividerLabelListener l) {
		for (int i = 0; i < dividerLabelListeners.size(); i++) {
			if (dividerLabelListeners.get(i) == l) {
				dividerLabelListeners.remove(i);
				return;
			}
		}
	}
	
	private void notifyAutoResizeEvent() {
		for (int i = 0; i < dividerLabelListeners.size(); i++) {
			dividerLabelListeners.get(i).dividerLabelAutoResize(this);
		}
	}
	
	private void notifyResizeEvent(int x, int y) {
		for (int i = 0; i < dividerLabelListeners.size(); i++) {
			dividerLabelListeners.get(i).dividerLabelResize(this, x, y);
		}
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		g.setColor(dividerColor);
		int indexX = (getWidth() + 1) / 2;
		g.drawLine(indexX, 0, indexX, getHeight());
	}

	private class MouseEventHandler extends MouseAdapter {

		private Cursor  oldCursor = null;
		private boolean isDraging = false;
		private boolean isMouseIn = false;
		private long    lastTime  = 0;
		private Point   lastPointForResize = null;
		
		public MouseEventHandler(Component component) {
		}
		
		private void handleLeftButtonEvent(MouseEvent e) {
			if (lastTime == 0) {
				lastTime = System.currentTimeMillis();
				return;
			}
			
			long currentTime = System.currentTimeMillis();
			if (currentTime - lastTime < 500) {
				notifyAutoResizeEvent();
			}
			
			lastTime = currentTime;
			lastPointForResize = e.getLocationOnScreen();
		}
		

		
		@Override
		public void mousePressed(MouseEvent e) {
			
			if (e.getButton() == MouseEvent.BUTTON1) {
				handleLeftButtonEvent(e);
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			isDraging = false;
			if (!isMouseIn) {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			isDraging = true;
			Point currentPoint = e.getLocationOnScreen();
			if (lastPointForResize == null) {
				lastPointForResize = currentPoint;
				return;
			}
			
			double x = currentPoint.getX() - lastPointForResize.getX();
			notifyResizeEvent((int)(x), 0);
			lastPointForResize = currentPoint;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			isMouseIn = true;
			if (!isDraging) {
				oldCursor = getCursor();
				setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			isMouseIn = false;
			if (!isDraging) {
				if (oldCursor != null) {
					setCursor(oldCursor);
				}
			}
		}
	}
}

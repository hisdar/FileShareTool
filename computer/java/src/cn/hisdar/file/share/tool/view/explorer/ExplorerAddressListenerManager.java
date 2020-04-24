package cn.hisdar.file.share.tool.view.explorer;

import java.util.ArrayList;

public class ExplorerAddressListenerManager {
	
	private String address;
	private ArrayList<ExplorerAddressListener> listeners;

	public ExplorerAddressListenerManager() {
		address = null;
		listeners = new ArrayList<>();
	}
	
	public void addListener(ExplorerAddressListener listener) {
		for (int i = 0; i < listeners.size(); i++) {
			if (listeners.get(i) == listener) {
				return ;
			}
		}
		
		listeners.add(listener);
		if (address != null) {
			listener.updateExplorerAddress(address);
		}
	}
	
	public void removeListener(ExplorerAddressListener listener) {
		for (int i = 0; i < listeners.size(); i++) {
			if (listeners.get(i) == listener) {
				listeners.remove(i);
				return ;
			}
		}
	}
	
	public void notifyExplorerAddressEvent(String address) {
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).updateExplorerAddress(address);
		}
	}
}

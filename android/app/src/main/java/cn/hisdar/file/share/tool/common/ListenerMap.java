package cn.hisdar.file.share.tool.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ListenerMap <T> {

    private HashMap<T, T> listenersMap;

    public ListenerMap () {
        listenersMap = new HashMap<>();
    }

    public void addListener(T t) {
        T listener = listenersMap.get(t);
        if (listener == null) {
            listenersMap.put(t, t);
        }
    }

    public void removeListener(T t) {
        T listener = listenersMap.get(t);
        if (listener != null) {
            listenersMap.remove(listener);
        }
    }

    public ArrayList<T> getAllListeners() {
        ArrayList<T> allListeners = new ArrayList<>();
        Iterator<Map.Entry<T,T>> iter = listenersMap.entrySet().iterator();
        while (iter.hasNext()) {
            allListeners.add(iter.next().getValue());
        }
        return allListeners;
    }
}

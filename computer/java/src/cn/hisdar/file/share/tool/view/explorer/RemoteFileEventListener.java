package cn.hisdar.file.share.tool.view.explorer;

import cn.hisdar.file.share.tool.command.RemoteFile;

public interface RemoteFileEventListener {

	public void remoteFileEvent(RemoteFile file, int event);
}

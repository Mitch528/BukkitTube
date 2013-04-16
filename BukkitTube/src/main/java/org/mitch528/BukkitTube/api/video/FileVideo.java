package org.mitch528.BukkitTube.api.video;

import java.io.File;

public class FileVideo extends Video
{
	
	public FileVideo(File file)
	{
		this.filename = file.getAbsolutePath();
	}
	
}

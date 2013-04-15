package org.mitch528.BukkitTube.api.video;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class FileVideo extends Video
{
	
	public FileVideo(File file)
	{
		this.filename = file.getAbsolutePath();
	}
	
	@Override
	public void start()
	{
		
		try
		{
			
			File tmp = File.createTempFile(filename, ".tmp");
			tmp.deleteOnExit();
			
			FileUtils.copyFile(new File(filename), tmp);
			
			filename = tmp.getAbsolutePath();
			
		}
		catch (Exception e)
		{
			
		}
		
		loop();
		
	}
}

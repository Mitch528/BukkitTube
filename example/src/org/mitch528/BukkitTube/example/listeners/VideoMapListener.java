package org.mitch528.BukkitTube.example.listeners;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import org.bukkit.event.Listener;
import org.bukkit.event.server.MapInitializeEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapRenderer;
import org.mitch528.BukkitTube.api.VideoSender;
import org.mitch528.BukkitTube.api.video.FileVideo;
import org.mitch528.BukkitTube.api.video.Video;
import org.mitch528.BukkitTube.example.Example;

public class VideoMapListener implements Listener
{
	
	private Example example;
	
	public VideoMapListener(Example ex)
	{
		example = ex;
	}
	
	@EventHandler
	public void onMapInitialize(final MapInitializeEvent event)
	{
		
		for (final Player player : Bukkit.getOnlinePlayers())
		{
			
			Inventory inv = player.getInventory();
			
			for (ItemStack is : inv.getContents())
			{
				
				final String vidFile = example.getVideosToPlay().get(player.getName());
				
				if (is != null && is.getType() == Material.MAP && is.getDurability() == event.getMap().getId() && vidFile != null && !vidFile.equals(""))
				{
					
					for (MapRenderer r : event.getMap().getRenderers())
					{
						event.getMap().removeRenderer(r);
					}
					
					example.getVideosToPlay().remove(player.getName());
					example.getPreviousVideos().put(event.getMap().getId(), vidFile);
					
					if (vidFile == null || vidFile.equals(""))
					{
						return;
					}
					
					Video vid = new FileVideo(new File(vidFile));
					vid.start();
					
					VideoSender.startSending(vid, event.getMap(), player);
					
					break;
					
				}
			}
			
		}
		
		//		if (vidFile == null || vidFile.equals(""))
		//		{
		//			return;
		//		}
		
		//		for (MapRenderer r : event.getMap().getRenderers())
		//		{
		//			event.getMap().removeRenderer(r);
		//		}
		//		
		//		event.getMap().addRenderer(new VideoMapRenderer(vidFile));
		
	}
}

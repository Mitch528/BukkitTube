package org.mitch528.listeners;

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
import org.mitch528.BukkitTube.BukkitTube;
import org.mitch528.api.VideoSender;
import org.mitch528.api.video.FileVideo;
import org.mitch528.api.video.Video;

public class VideoMapListener implements Listener
{
	
	private BukkitTube bukkitTube;
	
	public VideoMapListener(BukkitTube bt)
	{
		bukkitTube = bt;
	}
	
	@EventHandler
	public void onMapInitialize(final MapInitializeEvent event)
	{
		
		for (final Player player : Bukkit.getOnlinePlayers())
		{
			
			Inventory inv = player.getInventory();
			
			for (ItemStack is : inv.getContents())
			{
				
				final String vidFile = bukkitTube.getVideosToPlay().get(player.getName());
				
				if (is != null && is.getType() == Material.MAP && is.getDurability() == event.getMap().getId() && vidFile != null && !vidFile.equals(""))
				{
					
					for (MapRenderer r : event.getMap().getRenderers())
					{
						event.getMap().removeRenderer(r);
					}
					
					bukkitTube.getVideosToPlay().remove(player.getName());
					bukkitTube.getPreviousVideos().put(event.getMap().getId(), vidFile);
					
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

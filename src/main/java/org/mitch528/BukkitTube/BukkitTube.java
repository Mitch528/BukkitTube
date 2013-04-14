package org.mitch528.BukkitTube;

import java.io.File;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

import org.mitch528.listeners.VideoMapListener;
import org.mitch528.video.FileVideo;
import org.mitch528.video.Video;

public class BukkitTube extends JavaPlugin
{
	
	private HashMap<String, String> videosToPlay;
	private HashMap<String, String> previousVideoPlayed;
	
	@Override
	public void onEnable()
	{
		
		videosToPlay = new HashMap<String, String>();
		previousVideoPlayed = new HashMap<String, String>();
		
		getServer().getPluginManager().registerEvents(new VideoMapListener(this), this);
		
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
	{
		
		if (!(sender instanceof Player))
			return false;
		
		Player player = (Player) sender;
		
		if (cmd.getName().equalsIgnoreCase("loadvideo"))
		{
			
			if (args.length != 1)
				return false;
			
			String fileName = args[0];
			
			if (!new File(fileName).exists())
			{
				
				player.sendMessage("The specified file does not exist!");
				
				return true;
				
			}
			
			videosToPlay.put(player.getName(), fileName);
			
			Inventory inv = player.getInventory();
			
			int slot = -1;
			
			for (int i = 0; i < inv.getSize(); i++)
			{
				if (inv.getItem(i) == null)
				{
					
					inv.setItem(i, new ItemStack(Material.MAP));
					slot = i;
					
					break;
					
				}
			}
			
			if (slot == -1)
			{
				player.sendMessage("You do not have enough inventory space!");
			}
			
			return true;
			
		}
		else if (cmd.getName().equalsIgnoreCase("replay"))
		{
			
			String file = previousVideoPlayed.get(player.getName());
			
			if (file == null || file.equals(""))
			{
				
				player.sendMessage("Error! No video to replay.");
				
				return true;
				
			}
			
			ItemStack is = player.getInventory().getItemInHand();
			
			if (is != null && is.getType() == Material.MAP && is.getDurability() == is.getDurability())
			{
				
				MapView map = Bukkit.getMap(is.getDurability());
				
				if (map != null)
				{
					
					player.sendMessage("Replaying...");
					
					Video vid = new FileVideo(new File(file));
					vid.start();
					
					VideoSender.startSending(vid, map, player);
					
				}
				
			}
			
			return true;
			
		}
		
		return false;
		
	}
	
	public HashMap<String, String> getVideosToPlay()
	{
		return videosToPlay;
	}
	
	public HashMap<String, String> getPreviousVideos()
	{
		return previousVideoPlayed;
	}
	
}

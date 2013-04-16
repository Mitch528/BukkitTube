package org.mitch528.BukkitTube.api;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;
import org.mitch528.BukkitTube.api.video.Video;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

public class VideoSender
{
	
	private static ProtocolManager manager = ProtocolLibrary.getProtocolManager();
	
	private static List<Short> mapsCurrentlyPlaying;
	
	static
	{
		mapsCurrentlyPlaying = Collections.synchronizedList(new ArrayList<Short>());
	}
	
	public static boolean isMapPlaying(short id)
	{
		return mapsCurrentlyPlaying.contains(id);
	}
	
	public static void startSending(final Video vid, final MapView map, final Player player)
	{
		
		mapsCurrentlyPlaying.add(map.getId());
		
		new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				
				BufferedImage img = null;
				
				while (true)
				{
					
					if (!player.isOnline())
					{
						
						vid.stop();
						
						return;
						
					}
					
					if (vid.isDone())
					{
						
						vid.stop();
						
						if (mapsCurrentlyPlaying.contains(map.getId()))
							mapsCurrentlyPlaying.remove(mapsCurrentlyPlaying.indexOf(map.getId()));
						
						return;
						
					}
					
					img = vid.getImage();
					
					if (img == null)
						continue;
					
					//					BufferedImage newImg = MapPalette.resizeImage(img);
					
					/**
					 * https://github.com/Bukkit/Bukkit/blob/master/src/main/java/org/bukkit/map/MapPalette.java
					 */
					
					int[] pixels = new int[img.getWidth() * img.getHeight()];
					img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());
					
					byte[] data = new byte[img.getWidth() * img.getHeight()];
					
					for (int i = 0; i < pixels.length; i++)
					{
						data[i] = MapPalette.matchColor(new Color(pixels[i], true));
					}
					
					img.flush();
					
					img = null;
					
					for (int col = 0; col < 128; ++col)
					{
						
						PacketContainer container = manager.createPacket(131);
						
						container.getShorts().write(0, (short) Material.MAP.getId());
						container.getShorts().write(1, map.getId());
						
						byte[] raw = new byte[131];
						raw[0] = 0;
						raw[1] = (byte) col;
						raw[2] = 0;
						
						for (int row = 0; row < 128; ++row)
						{
							raw[3 + row] = data[row * 128 + col];
						}
						
						container.getByteArrays().write(0, raw);
						
						try
						{
							manager.sendServerPacket(player, container);
						}
						catch (Exception e)
						{
							
							vid.stop();
							
							raw = null;
							data = null;
							
							return;
							
						}
						
						raw = null;
						
					}
					
					data = null;
					
				}
				
			}
			
		}).start();
		
	}
	
}

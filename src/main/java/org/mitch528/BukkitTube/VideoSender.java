package org.mitch528.BukkitTube;

import java.awt.image.BufferedImage;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.map.MapPalette;
import org.bukkit.map.MapView;
import org.mitch528.video.Video;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

public class VideoSender
{
	
	private static ProtocolManager manager = ProtocolLibrary.getProtocolManager();
	
	public static void startSending(final Video vid, final MapView map, final Player player)
	{
		
		new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				
				BufferedImage img = null;
				
				while (true)
				{
					
					if (vid.isDone())
					{
						return;
					}
					
					if (!Bukkit.getPlayer(player.getName()).isOnline())
					{
						return;
					}
					
					img = vid.getImage();
					
					if (img == null)
						continue;
					
					BufferedImage newImg = MapPalette.resizeImage(img);
					
					byte[] data = MapPalette.imageToBytes(newImg);
					
					img.flush();
					newImg.flush();
					
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
							e.printStackTrace();
						}
						
						raw = null;
						
					}
					
					data = null;
					
				}
				
			}
			
		}).start();
		
	}
	
}

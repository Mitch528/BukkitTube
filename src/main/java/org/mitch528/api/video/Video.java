package org.mitch528.api.video;

import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.xuggle.xuggler.Global;
import com.xuggle.xuggler.ICodec.Type;
import com.xuggle.xuggler.IContainer;
import com.xuggle.xuggler.IPacket;
import com.xuggle.xuggler.IPixelFormat;
import com.xuggle.xuggler.IStream;
import com.xuggle.xuggler.IStreamCoder;
import com.xuggle.xuggler.IVideoPicture;
import com.xuggle.xuggler.IVideoResampler;
import com.xuggle.xuggler.Utils;

public abstract class Video
{
	
	private ConcurrentLinkedQueue<BufferedImage> images;
	
	private AtomicBoolean isdone;
	private AtomicBoolean shouldstop;
	
	protected String filename;
	
	public Video()
	{
		images = new ConcurrentLinkedQueue<BufferedImage>();
		isdone = new AtomicBoolean();
		shouldstop = new AtomicBoolean();
		isdone.set(false);
		shouldstop.set(false);
	}
	
	public void start()
	{
		shouldstop.set(false);
		loop();
	}
	
	public void stop()
	{
		
		shouldstop.set(true);
		
		for (BufferedImage img : images)
		{
			img.flush();
			img = null;
		}
		
		images.clear();
		
	}
	
	public BufferedImage getImage()
	{
		return images.poll();
	}
	
	public boolean isDone()
	{
		return isdone.get() || shouldstop.get();
	}
	
	public void loop()
	{
		
		new Thread(new Runnable()
		{
			
			@Override
			public void run()
			{
				
				try
				{
					
					/**
					 * https://github.com/xuggle/xuggle-xuggler/blob/master/src/com/xuggle/xuggler/demos/DecodeAndPlayVideo.java
					 */
					
					IContainer container = IContainer.make();
					
					if (container.open(filename, IContainer.Type.READ, null) < 0)
						throw new IllegalArgumentException("could not open file: " + filename);
					
					int numStreams = container.getNumStreams();
					
					int videoStreamId = -1;
					IStreamCoder videoCoder = null;
					for (int i = 0; i < numStreams; i++)
					{
						IStream stream = container.getStream(i);
						IStreamCoder coder = stream.getStreamCoder();
						
						if (coder.getCodecType() == Type.CODEC_TYPE_VIDEO)
						{
							videoStreamId = i;
							videoCoder = coder;
							break;
						}
					}
					if (videoStreamId == -1)
						throw new RuntimeException("could not find video stream in container: " + filename);
					
					videoCoder.setBitRate(16);
					videoCoder.setBitRateTolerance(2);
					videoCoder.setFlag(IStreamCoder.Flags.FLAG_QSCALE, false);
					
					if (videoCoder.open() < 0)
						throw new RuntimeException("could not open video decoder for container: " + filename);
					
					IVideoResampler resampler = null;
					if (videoCoder.getPixelType() != IPixelFormat.Type.BGR24)
					{
						
						resampler = IVideoResampler.make(videoCoder.getWidth(), videoCoder.getHeight(), IPixelFormat.Type.BGR24, videoCoder.getWidth(), videoCoder.getHeight(),
								videoCoder.getPixelType());
						if (resampler == null)
							throw new RuntimeException("could not create color space " + "resampler for: " + filename);
					}
					
					IPacket packet = IPacket.make();
					long firstTimestampInStream = Global.NO_PTS;
					long systemClockStartTime = 0;
					
					while (container.readNextPacket(packet) >= 0)
					{
						
						if (shouldstop.get())
						{
							break;
						}
						
						if (packet.getStreamIndex() == videoStreamId)
						{
							
							IVideoPicture picture = IVideoPicture.make(videoCoder.getPixelType(), videoCoder.getWidth(), videoCoder.getHeight());
							
							int offset = 0;
							while (offset < packet.getSize())
							{
								
								if (shouldstop.get())
								{
									break;
								}
								
								int bytesDecoded = videoCoder.decodeVideo(picture, packet, offset);
								if (bytesDecoded < 0)
									throw new RuntimeException("got error decoding video in: " + filename);
								offset += bytesDecoded;
								
								if (picture.isComplete())
								{
									IVideoPicture newPic = picture;
									if (resampler != null)
									{
										newPic = IVideoPicture.make(resampler.getOutputPixelFormat(), picture.getWidth(), picture.getHeight());
										if (resampler.resample(newPic, picture) < 0)
											throw new RuntimeException("could not resample video from: " + filename);
									}
									if (newPic.getPixelType() != IPixelFormat.Type.BGR24)
										throw new RuntimeException("could not decode video" + " as BGR 24 bit data in: " + filename);
									
									if (firstTimestampInStream == Global.NO_PTS)
									{
										firstTimestampInStream = picture.getTimeStamp();
										systemClockStartTime = System.currentTimeMillis();
									}
									else
									{
										long systemClockCurrentTime = System.currentTimeMillis();
										long millisecondsClockTimeSinceStartofVideo = systemClockCurrentTime - systemClockStartTime;
										long millisecondsStreamTimeSinceStartOfVideo = (picture.getTimeStamp() - firstTimestampInStream) / 1000;
										final long millisecondsTolerance = 50;
										final long millisecondsToSleep = (millisecondsStreamTimeSinceStartOfVideo - (millisecondsClockTimeSinceStartofVideo + millisecondsTolerance));
										if (millisecondsToSleep > 0)
										{
											try
											{
												Thread.sleep(millisecondsToSleep);
											}
											catch (InterruptedException e)
											{
												return;
											}
										}
									}
									
									BufferedImage javaImage = Utils.videoPictureToImage(newPic);
									
									newPic.getData().delete();
									newPic.delete();
									
									images.add(javaImage);
									
								}
								
								picture.getData().delete();
								picture.delete();
								
							}
						}
						else
						{
							do
							{
							}
							while (false);
						}
						
					}
					
					packet.delete();
					
					if (videoCoder != null)
					{
						videoCoder.close();
						videoCoder = null;
					}
					if (container != null)
					{
						container.close();
						container = null;
					}
					
					isdone.set(true);
					
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				
			}
			
		}).start();
		
	}
}

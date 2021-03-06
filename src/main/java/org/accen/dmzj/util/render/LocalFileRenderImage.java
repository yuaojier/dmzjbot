package org.accen.dmzj.util.render;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class LocalFileRenderImage extends RenderImage{
	private File localFile ;
	private BufferedImage buff;
	public LocalFileRenderImage(File file) throws IOException {
		super();
		this.localFile = file;
		this.buff = ImageIO.read(localFile);
	}
	@Override
	public BufferedImage getBuffer() {
		return buff;
	}

	@Override
	public int getWidth() {
		return buff.getWidth();
	}

	@Override
	public int getHeight() {
		return buff.getHeight();
	}
	
}

package b100.natrium;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.client.render.tessellator.TessellatorBase;
import net.minecraft.core.util.helper.MathHelper;

public class TessellatorNatrium extends TessellatorBase {
	private static final Logger LOGGER = LogUtils.getLogger();
	public VertexData data;
	public boolean drawing;

	private int color;
	
	private int lightmapCoord;
	
	private double textureU;
	private double textureV;
	
	private byte normalX;
	private byte normalY;
	private byte normalZ;
	
	private double offsetX;
	private double offsetY;
	private double offsetZ;
	
	private boolean lockedColor = false;
	
	public TessellatorNatrium(int bufferSize) {
		data = new VertexData(bufferSize);
	}
	
	@Override
	public void startDrawing(int drawMode) {
		if(drawing) {
			throw new IllegalStateException("Already drawing!");
		}
		
		VertexConfig config = data.config;
		
		config.drawMode = drawMode;
		this.drawing = true;
		data.vertexCount = 0;
		
		config.enableColor = false;
		config.enableTexture = false;
		config.enableLightmap = false;
		config.enableNormal = false;

		this.lockedColor = false;
		
		data.buffer.clear();
	}

	@Override
	public void draw() {
		checkIsDrawing();
		this.drawing = false;
		
		if(data.vertexCount == 0) {
			return;
		}
		
		data.buffer.flip();
		data.drawAll();
	}
	
	public int getVertexSize() {
		return data.config.getVertexSize();
	}

	@Override
	public void addVertex(double x, double y, double z) {
		checkIsDrawing();

		if(data.buffer.capacity() < data.buffer.position() + 64) {
			int newSize = data.buffer.capacity() * 2;
			
			LOGGER.info("Expanding Tessellator Buffer (" + data.buffer.capacity() + " -> " + newSize + ")");
			
			ByteBuffer newBuffer = ByteBuffer.allocateDirect(newSize).order(ByteOrder.nativeOrder());
			data.buffer.flip();
			newBuffer.put(data.buffer);
			data.buffer = newBuffer;
		}
		
		ByteBuffer buffer = data.buffer;
		
		buffer.putFloat((float) (offsetX + x));
		buffer.putFloat((float) (offsetY + y));
		buffer.putFloat((float) (offsetZ + z));

		VertexConfig config = data.config;
		
		if(config.enableColor) {
			buffer.putInt(color);
		}
		if(config.enableTexture) {
			buffer.putFloat((float) textureU);
			buffer.putFloat((float) textureV);
		}
		if(config.enableLightmap) {
			buffer.putInt(lightmapCoord);
		}
		if(config.enableNormal) {
			buffer.put(normalX);
			buffer.put(normalY);
			buffer.put(normalZ);
		}
		
		data.vertexCount++;
	}

	@Override
	public void setTextureUV(double u, double v) {
		checkIsDrawing();
		if(!data.config.enableTexture) {
			if(data.vertexCount > 0) {
				throw new IllegalStateException("Texture is disabled!");
			}
			data.config.enableTexture = true;
		}
		
		this.textureU = u;
		this.textureV = v;
	}

	@Override
	public void setLightmapCoord(int lightmapCoord) {
		checkIsDrawing();
		if(!data.config.enableLightmap) {
			if(data.vertexCount > 0) {
				throw new IllegalStateException("Lightmap is disabled!");
			}
			data.config.enableLightmap = true;
		}
		
		this.lightmapCoord = lightmapCoord;
	}

	@Override
	public void setColorRGBA(int r, int g, int b, int a) {
		checkIsDrawing();
		if(lockedColor) {
			return;
		}
		if(!data.config.enableColor) {
			if(data.vertexCount > 0) {
				throw new IllegalStateException("Color is disabled!");
			}
			data.config.enableColor = true;
		}
		
		r = MathHelper.clamp(r, 0, 255);
		g = MathHelper.clamp(g, 0, 255);
		b = MathHelper.clamp(b, 0, 255);
		a = MathHelper.clamp(a, 0, 255);
		
		this.color = a << 24 | b << 16 | g << 8 | r;
	}

	@Override
	public void setNormal(float x, float y, float z) {
		checkIsDrawing();
		if(!data.config.enableNormal) {
			if(data.vertexCount > 0) {
				throw new IllegalStateException("Texture is disabled!");
			}
			data.config.enableNormal = true;
		}
		
		this.normalX = (byte) (x * 127.0f);
		this.normalY = (byte) (y * 127.0f);
		this.normalZ = (byte) (z * 127.0f);
	}

	@Override
	public void lockColor() {
		checkIsDrawing();
		
		this.lockedColor = true;
	}

	@Override
	public void setTranslation(double x, double y, double z) {
		this.offsetX = x;
		this.offsetY = y;
		this.offsetZ = z;
	}

	@Override
	public void offsetTranslation(float x, float y, float z) {
		this.offsetX += x;
		this.offsetY += y;
		this.offsetZ += z;
	}
	
	@Override
	public void checkIsDrawing() {
		if(!drawing) {
			throw new IllegalStateException("Not drawing!");
		}
	}
	
}

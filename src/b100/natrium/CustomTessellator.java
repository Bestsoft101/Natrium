package b100.natrium;

import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;

import net.minecraft.client.render.Tessellator;
import net.minecraft.core.util.helper.MathHelper;

public class CustomTessellator extends Tessellator {
	
	public CustomTessellator() {
		super(0);
		
		this.byteBuffer = BufferHelper.createByteBuffer(262144);
	}
	
	@Override
	public void startDrawing(int drawMode) {
		if(isDrawing) {
			throw new RuntimeException("Already drawing!");
		}
		
		this.hasColor = false;
		this.hasTexture = false;
		this.hasNormals = false;
		
		this.drawMode = drawMode;
		addedVertices = 0;
		
		byteBuffer.clear();
		
		this.isColorDisabled = false;
		this.isDrawing = true;
	}
	
	@Override
	public void draw() {
		checkIsDrawing();
		isDrawing = false;
		
		if(addedVertices == 0) {
			return;
		}
		
		byteBuffer.flip();
		
		int vertexSize = getVertexSize();
		int offset = 0;
		
		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(3, GL_FLOAT, vertexSize, byteBuffer);
		offset += 12;
		
		if(hasColor) {
			byteBuffer.position(offset);
			glEnableClientState(GL_COLOR_ARRAY);
			glColorPointer(4, GL_UNSIGNED_BYTE, vertexSize, byteBuffer);
			offset += 4;
		}else {
			glDisableClientState(GL_COLOR_ARRAY);
		}
		if(hasTexture) {
			byteBuffer.position(offset);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			glTexCoordPointer(2, GL_FLOAT, vertexSize, byteBuffer);
			offset += 8;
		}else {
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		}
		if(hasNormals) {
			byteBuffer.position(offset);
			glEnableClientState(GL_NORMAL_ARRAY);
			glNormalPointer(GL_BYTE, vertexSize, byteBuffer);
			offset += 3;
		}else {
			glDisableClientState(GL_NORMAL_ARRAY);
		}
		
		glDrawArrays(drawMode, 0, addedVertices);
	}
	
	@Override
	public void addVertex(double x, double y, double z) {
		checkIsDrawing();
		
		if(byteBuffer.capacity() < byteBuffer.position() + 64) {
			expandBuffer();
		}
		
		byteBuffer.putFloat((float) (xOffset + x));
		byteBuffer.putFloat((float) (yOffset + y));
		byteBuffer.putFloat((float) (zOffset + z));

		if(hasColor) {
			byteBuffer.putInt(color);
		}
		
		if(hasTexture) {
			byteBuffer.putFloat((float) textureU);
			byteBuffer.putFloat((float) textureV);
		}
		
		if(hasNormals) {
			byteBuffer.put((byte) ((this.normal >> 16) & 0xFF));
			byteBuffer.put((byte) ((this.normal >>  8) & 0xFF));
			byteBuffer.put((byte) (this.normal & 0xFF));
		}
		
		addedVertices++;
	}
	
	@Override
	public void setNormal(float x, float y, float z) {
		if(addedVertices > 0 && !hasNormals) {
			throw new RuntimeException("Normals are disabled!");
		}
		this.hasNormals = true;
		byte bx = (byte) ((int) (x * 127.0F));
		byte by = (byte) ((int) (y * 127.0F));
		byte bz = (byte) ((int) (z * 127.0F));
		this.normal = bx << 16 | by << 8 | bz;
	}
	
	@Override
	public void setTextureUV(double u, double v) {
		if(addedVertices > 0 && !hasTexture) {
			throw new RuntimeException("Texcoord is disabled!");
		}
		this.hasTexture = true;
		this.textureU = u;
		this.textureV = v;
	}
	
	@Override
	public void setColorRGBA(int r, int g, int b, int a) {
		if(isColorDisabled) {
			return;
		}
		if(addedVertices > 0 && !hasColor) {
//			throw new RuntimeException("Color is disabled!");
			return;
		}
		hasColor = true;
		r = MathHelper.clamp(r, 0, 255);
		g = MathHelper.clamp(g, 0, 255);
		b = MathHelper.clamp(b, 0, 255);
		a = MathHelper.clamp(a, 0, 255);
		this.color = a << 24 | b << 16 | g << 8 | r;
	}
	
	public int getVertexSize() {
		int size = 12;
		
		if(hasColor) size += 4;
		if(hasTexture) size += 8;
		if(hasNormals) size += 3;
		
		return size;
	}
	
	public void expandBuffer() {
		int newSize = byteBuffer.capacity() * 2;
		System.out.println("Expanding tessellator buffer to " + newSize);
		
		ByteBuffer newBuffer = BufferHelper.createByteBuffer(newSize);
		newBuffer.clear();
		byteBuffer.flip();
		newBuffer.put(byteBuffer);
		this.byteBuffer = newBuffer;
	}
}

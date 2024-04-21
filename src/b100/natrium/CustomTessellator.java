package b100.natrium;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.render.LightmapHelper;
import net.minecraft.client.render.tessellator.TessellatorBase;
import net.minecraft.core.util.helper.MathHelper;

public class CustomTessellator extends TessellatorBase {
	
	public ByteBuffer buffer;

	public List<VertexAttribute> vertexAttribs = new ArrayList<>();
	
	public int drawMode;
	
	public boolean hasColor = false;
	public boolean hasTexture = false;
	public boolean hasNormals = false;
	public boolean hasLightmap = false;

	private int normal;
	private int color;
	private int lightmapCoord;
	private double textureU;
	private double textureV;
	
	public boolean isDrawing = false;
	public int addedVertices = 0;
	
	private boolean isColorDisabled = false;
	
	private double xOffset;
	private double yOffset;
	private double zOffset;
	
	public CustomTessellator() {
		this.buffer = BufferHelper.createByteBuffer(131072);
	}
	
	@Override
	public void startDrawing(int drawMode) {
		if(isDrawing) {
			throw new RuntimeException("Already drawing!");
		}
		
		this.hasColor = false;
		this.hasTexture = false;
		this.hasNormals = false;
		this.hasLightmap = false;
		vertexAttribs.clear();
		
		this.drawMode = drawMode;
		addedVertices = 0;
		
		buffer.clear();
		
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
		
		buffer.flip();
		
		int vertexSize = getVertexSize();
		int offset = 0;
		
		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(3, GL_FLOAT, vertexSize, buffer);
		offset += 12;
		
		if(hasColor) {
			buffer.position(offset);
			glEnableClientState(GL_COLOR_ARRAY);
			glColorPointer(4, GL_UNSIGNED_BYTE, vertexSize, buffer);
			offset += 4;
		}else {
			glDisableClientState(GL_COLOR_ARRAY);
		}
		if(hasTexture) {
			buffer.position(offset);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			glTexCoordPointer(2, GL_FLOAT, vertexSize, buffer);
			offset += 8;
		}else {
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		}
		if(hasNormals) {
			buffer.position(offset);
			glEnableClientState(GL_NORMAL_ARRAY);
			glNormalPointer(GL_BYTE, vertexSize, buffer);
			offset += 3;
		}else {
			glDisableClientState(GL_NORMAL_ARRAY);
		}

		glClientActiveTexture(GL_TEXTURE1);
		if(hasLightmap) {
			buffer.position(offset);
			glTexCoordPointer(2, GL_SHORT, vertexSize, buffer);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			offset += 4;
		}else {
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		}
		glClientActiveTexture(GL_TEXTURE0);
		
		for(int i=0; i < vertexAttribs.size(); i++) {
			VertexAttribute attrib = vertexAttribs.get(i);
			
			buffer.position(offset);
			glEnableVertexAttribArray(attrib.id);
			glVertexAttribPointer(attrib.id, attrib.getSize(), attrib.type, false, vertexSize, buffer);
			offset += attrib.getTypeSize();
		}
		
		glDrawArrays(drawMode, 0, addedVertices);
		
		for(int i=0; i < vertexAttribs.size(); i++) {
			VertexAttribute attrib = vertexAttribs.get(i);
			glDisableVertexAttribArray(attrib.id);
		}
	}
	
	@Override
	public void addVertex(double x, double y, double z) {
		checkIsDrawing();
		
		if(buffer.capacity() < buffer.position() + 64) {
			expandBuffer();
		}
		
		buffer.putFloat((float) (xOffset + x));
		buffer.putFloat((float) (yOffset + y));
		buffer.putFloat((float) (zOffset + z));

		if(hasColor) {
			buffer.putInt(color);
		}
		
		if(hasTexture) {
			buffer.putFloat((float) textureU);
			buffer.putFloat((float) textureV);
		}
		
		if(hasNormals) {
			buffer.put((byte) ((this.normal >> 16) & 0xFF));
			buffer.put((byte) ((this.normal >>  8) & 0xFF));
			buffer.put((byte) (this.normal & 0xFF));
		}
		
		if(hasLightmap) {
			buffer.putInt(lightmapCoord);
		}
		
		for(int i=0; i < vertexAttribs.size(); i++) {
			vertexAttribs.get(i).addVertex(buffer);
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
	public void setLightmapCoord(int lmc) {
		if(addedVertices > 0 && !hasLightmap || !LightmapHelper.isLightmapEnabled()) {
			throw new RuntimeException("Lightmap is disabled!");
		}
		this.hasLightmap = true;
		this.lightmapCoord = lmc;
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
	
	public void addVertexAttrib(VertexAttribute vertexAttribute) {
		checkIsDrawing();
		if(addedVertices > 0) {
			throw new RuntimeException("Already started drawing!");
		}
		for(int i=0; i < this.vertexAttribs.size(); i++) {
			VertexAttribute attrib = this.vertexAttribs.get(i);
			
			if(attrib.id == vertexAttribute.id) {
				throw new RuntimeException("Vertex Attribute with ID " + attrib.id + " is already added!");
			}
			if(attrib.name.equals(vertexAttribute.name)) {
				throw new RuntimeException("Vertex Attribute with name '" + attrib.name + "' is already added!");
			}
		}
		this.vertexAttribs.add(vertexAttribute);
	}
	
	public int getVertexSize() {
		int size = 12;
		
		if(hasColor) size += 4;
		if(hasTexture) size += 8;
		if(hasNormals) size += 3;
		if(hasLightmap) size += 4;
		
		for(int i=0; i < vertexAttribs.size(); i++) {
			size += vertexAttribs.get(i).getTypeSize();
		}
		
		return size;
	}
	
	public void expandBuffer() {
		int newSize = buffer.capacity() * 2;
		NatriumMod.log("Expanding tessellator buffer to " + newSize);
		
		ByteBuffer newBuffer = BufferHelper.createByteBuffer(newSize);
		newBuffer.clear();
		buffer.flip();
		newBuffer.put(buffer);
		this.buffer = newBuffer;
	}
	
	@Override
	public void disableColor() {
		this.isColorDisabled = true;
	}

	@Override
	public void offsetTranslation(float x, float y, float z) {
		this.xOffset += (double) x;
		this.yOffset += (double) y;
		this.zOffset += (double) z;
	}

	@Override
	public void setTranslation(double x, double y, double z) {
		this.xOffset = x;
		this.yOffset = y;
		this.zOffset = z;
	}

	@Override
	public void checkIsDrawing() {
		if (!this.isDrawing) {
			throw new IllegalStateException("Not tesselating!");
		}
	}

	@Override
	public void setUseVBO(boolean b) {
		// yes
	}
}

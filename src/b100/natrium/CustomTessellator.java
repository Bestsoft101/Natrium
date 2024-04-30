package b100.natrium;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import b100.natrium.vertex.VertexAttribute;
import b100.natrium.vertex.VertexComponent;
import b100.natrium.vertex.VertexComponentColor;
import b100.natrium.vertex.VertexComponentMultiTexCoordShort;
import b100.natrium.vertex.VertexComponentNormalByte;
import b100.natrium.vertex.VertexComponentPositionFloat;
import b100.natrium.vertex.VertexComponentTexCoordFloat;
import net.minecraft.client.render.LightmapHelper;
import net.minecraft.client.render.tessellator.TessellatorBase;
import net.minecraft.core.util.helper.MathHelper;

public class CustomTessellator extends TessellatorBase {
	
	private static boolean checkDuplicateComponents = false;
	
	public ByteBuffer buffer;

	public VertexComponentPositionFloat positionComponent = new VertexComponentPositionFloat();
	public VertexComponentTexCoordFloat texcoordComponent = new VertexComponentTexCoordFloat();
	public VertexComponentNormalByte normalComponent = new VertexComponentNormalByte();
	public VertexComponentColor colorComponent = new VertexComponentColor();
	public VertexComponentMultiTexCoordShort lightmapComponent = new VertexComponentMultiTexCoordShort(GL_TEXTURE1);
	
	public List<VertexComponent> enabledVertexComponents = new ArrayList<>();
	
	public boolean hasColor = false;
	public boolean hasTexture = false;
	public boolean hasNormals = false;
	public boolean hasLightmap = false;
	
	public List<VertexAttribute> vertexAttribs = new ArrayList<>();
	
	public int drawMode;
	
	public boolean isDrawing = false;
	public int addedVertices = 0;
	
	private boolean isColorLocked = false;
	
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
		
		enabledVertexComponents.clear();
		enabledVertexComponents.add(positionComponent);
		
		vertexAttribs.clear();
		
		this.drawMode = drawMode;
		this.addedVertices = 0;
		
		buffer.clear();
		
		this.isColorLocked = false;
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
		
		for(int i=0; i < enabledVertexComponents.size(); i++) {
			VertexComponent vertexComponent = enabledVertexComponents.get(i);
			
			buffer.position(offset);
			vertexComponent.enable(vertexSize, buffer);
			offset += vertexComponent.getSize();
		}
		
		for(int i=0; i < vertexAttribs.size(); i++) {
			VertexAttribute attrib = vertexAttribs.get(i);
			
			buffer.position(offset);
			glEnableVertexAttribArray(attrib.id);
			glVertexAttribPointer(attrib.id, attrib.getSize(), attrib.type, false, vertexSize, buffer);
			offset += attrib.getTypeSize();
		}
		
		glDrawArrays(drawMode, 0, addedVertices);
		
		for(int i=0; i < enabledVertexComponents.size(); i++) {
			enabledVertexComponents.get(i).disable();
		}
		
		for(int i=0; i < vertexAttribs.size(); i++) {
			VertexAttribute attrib = vertexAttribs.get(i);
			glDisableVertexAttribArray(attrib.id);
		}
	}
	
	@Override
	public void addVertex(double x, double y, double z) {
		checkIsDrawing();
		
		if(buffer.capacity() < buffer.position() + 256) {
			expandBuffer();
		}
		
		positionComponent.x = (float) (this.xOffset + x);
		positionComponent.y = (float) (this.yOffset + y);
		positionComponent.z = (float) (this.zOffset + z);
		
		for(int i=0; i < enabledVertexComponents.size(); i++) {
			enabledVertexComponents.get(i).addVertex(buffer);
		}
		
		for(int i=0; i < vertexAttribs.size(); i++) {
			vertexAttribs.get(i).addVertex(buffer);
		}
		
		addedVertices++;
	}
	
	@Override
	public void setNormal(float x, float y, float z) {
		if(!this.hasNormals) {
			if(addedVertices > 0) {
				throw new RuntimeException("Normals are disabled!");
			}
			this.hasNormals = true;
			this.addVertexComponent(normalComponent);
		}
		
		this.normalComponent.x = (byte) ((int) (x * 127.0F));
		this.normalComponent.y = (byte) ((int) (y * 127.0F));
		this.normalComponent.z = (byte) ((int) (z * 127.0F));
	}
	
	@Override
	public void setTextureUV(double u, double v) {
		if(!hasTexture) {
			if(addedVertices > 0) {
				throw new RuntimeException("Texcoord is disabled!");
			}
			this.hasTexture = true;
			this.addVertexComponent(texcoordComponent);
		}
		
		this.texcoordComponent.u = (float) u;
		this.texcoordComponent.v = (float) v;
	}
	
	@Override
	public void setLightmapCoord(int lmc) {
		if(!hasLightmap) {
			if(addedVertices > 0 || !LightmapHelper.isLightmapEnabled()) {
				throw new RuntimeException("Lightmap is disabled!");	
			}
			this.hasLightmap = true;
			this.addVertexComponent(lightmapComponent);
		}
		
		this.lightmapComponent.texCoord = lmc;
	}
	
	@Override
	public void setColorRGBA(int r, int g, int b, int a) {
		if(isColorLocked) {
			return;
		}
		if(!hasColor) {
			if(addedVertices > 0) {
//				throw new RuntimeException("Color is disabled!");
				return;
			}
			this.hasColor = true;
			this.addVertexComponent(colorComponent);
		}
		r = MathHelper.clamp(r, 0, 255);
		g = MathHelper.clamp(g, 0, 255);
		b = MathHelper.clamp(b, 0, 255);
		a = MathHelper.clamp(a, 0, 255);
		this.colorComponent.color = a << 24 | b << 16 | g << 8 | r;
	}
	
	public void addVertexComponent(VertexComponent vertexComponent) {
		checkIsDrawing();
		if(checkDuplicateComponents) {
			for(int i=0; i < this.enabledVertexComponents.size(); i++) {
				if(enabledVertexComponents.get(i) == vertexComponent) {
					throw new RuntimeException("VertexComponent is already added: " + vertexComponent);
				}
			}	
		}
		this.enabledVertexComponents.add(vertexComponent);
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
		int size = 0;
		
		for(int i=0; i < enabledVertexComponents.size(); i++) {
			size += enabledVertexComponents.get(i).getSize();
		}
		
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
		this.isColorLocked = true;
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

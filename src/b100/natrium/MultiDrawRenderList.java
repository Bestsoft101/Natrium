package b100.natrium;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiDrawRenderList {
	
	public final VBOPool vboPool;
	
	List<VBOPool.Entry> entries = new ArrayList<>();
	Set<VBOPool.Entry> visibleEntries = new HashSet<>();
	
	public VertexConfig config;

	private IntBuffer posBuffer;
	private IntBuffer sizeBuffer;
	
	private boolean visibleEntriesSetUpdated = true;
	
	public MultiDrawRenderList(VBOPool vboPool) {
		this.vboPool = vboPool;
		
		posBuffer = BufferHelper.createIntBuffer(100000);
		sizeBuffer = BufferHelper.createIntBuffer(100000);
	}
	
	public VBOPool.Entry add(CustomTessellator tessellator, boolean visible) {
		if(tessellator.addedVertices == 0) {
			return null;
		}
		
		if(entries.size() == 0) {
			this.config = VertexConfig.fromTessellator(tessellator);
		}else {
			if(!this.config.equals(VertexConfig.fromTessellator(tessellator))) {
				throw new RuntimeException("VertexConfig Mismatch!");
			}
		}
		
		VBOPool.Entry entry = vboPool.add(tessellator);
		this.entries.add(entry);
		
		if(visible) {
			this.visibleEntries.add(entry);
			this.visibleEntriesSetUpdated = true;
		}
		
		return entry;
	}
	
	public boolean remove(VBOPool.Entry entry) {
		setVisible(entry, false);
		if(entries.remove(entry)) {
			return vboPool.remove(entry);
		}
		return false;
	}
	
	public void setVisible(VBOPool.Entry entry, boolean visible) {
		if(entry == null) {
			throw new NullPointerException();
		}
		if(visible) {
			if(!this.visibleEntries.contains(entry)) {
				this.visibleEntries.add(entry);
				this.visibleEntriesSetUpdated = true;
			}
		}else {
			if(this.visibleEntries.contains(entry)) {
				this.visibleEntries.remove(entry);
				this.visibleEntriesSetUpdated = true;
			}
		}
	}
	
	public void draw() {
		if(entries.size() == 0) {
			return;
		}
		
		if(visibleEntriesSetUpdated) {
			int vertexSize = config.getVertexSize();
			
			this.posBuffer.clear();
			this.sizeBuffer.clear();
			
			for(VBOPool.Entry entry : visibleEntries) {
				posBuffer.put(entry.getPos() / vertexSize);
				sizeBuffer.put(entry.getSize() / vertexSize);
			}
			
			this.posBuffer.flip();
			this.sizeBuffer.flip();
		}
		
		glBindBuffer(GL_ARRAY_BUFFER, vboPool.getVBO());
		
		int vertexSize = config.getVertexSize();
		int offset = 0;
		
		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(3, GL_FLOAT, vertexSize, offset);
		offset += 12;
		
		if(config.enableColor) {
			glEnableClientState(GL_COLOR_ARRAY);
			glColorPointer(4, GL_UNSIGNED_BYTE, vertexSize, offset);
			offset += 4;
		}else {
			glDisableClientState(GL_COLOR_ARRAY);
		}
		if(config.enableTexcoord) {
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			glTexCoordPointer(2, GL_FLOAT, vertexSize, offset);
			offset += 8;
		}else {
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		}
		if(config.enableNormal) {
			glEnableClientState(GL_NORMAL_ARRAY);
			glNormalPointer(GL_BYTE, vertexSize, offset);
			offset += 3;
		}else {
			glDisableClientState(GL_NORMAL_ARRAY);
		}
		
		glMultiDrawArrays(config.drawMode, posBuffer, sizeBuffer);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

}

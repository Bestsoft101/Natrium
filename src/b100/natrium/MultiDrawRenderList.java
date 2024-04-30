package b100.natrium;

import static org.lwjgl.opengl.GL14.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import b100.natrium.vertex.VertexAttribute;
import b100.natrium.vertex.VertexComponent;

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
			VertexConfig tessellatorConfig = VertexConfig.fromTessellator(tessellator);
			int compareStatus = VertexConfig.compare(config, tessellatorConfig);
			if(compareStatus != 0) {
				throw new RuntimeException("VertexConfig Mismatch!\n" + this.config + "\n" + tessellatorConfig + "\nError: " + compareStatus);
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
		
		for(int i=0; i < config.vertexComponents.size(); i++) {
			VertexComponent vertexComponent = config.vertexComponents.get(i);
			vertexComponent.enable(vertexSize, offset);
			offset += vertexComponent.getSize();
		}
		
		for(int i=0; i < config.vertexAttribs.size(); i++) {
			VertexAttribute attrib = config.vertexAttribs.get(i);
			
			glEnableVertexAttribArray(attrib.id);
			glVertexAttribPointer(attrib.id, attrib.getSize(), attrib.type, false, vertexSize, offset);
			offset += attrib.getTypeSize();
		}
		
		glMultiDrawArrays(config.drawMode, posBuffer, sizeBuffer);
		
		for(int i=0; i < config.vertexComponents.size(); i++) {
			config.vertexComponents.get(i).disable();
		}
		
		for(int i=0; i < config.vertexAttribs.size(); i++) {
			VertexAttribute attrib = config.vertexAttribs.get(i);
			
			glDisableVertexAttribArray(attrib.id);
		}
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

}

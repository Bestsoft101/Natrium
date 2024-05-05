package b100.natrium;

import static org.lwjgl.opengl.GL15.*;

import java.util.ArrayList;
import java.util.List;

public class VBOPool {
	
	private List<Entry> entries = new ArrayList<>();
	
	private VertexConfig config;

	private int vbo;
	private long capacity;
	
	private int entryHashCounter;
	
	public VBOPool(long initialSize) {
		this.capacity = initialSize;
		this.vbo = glGenBuffers();
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, capacity, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public Entry add(CustomTessellator tessellator) {
		if(entries.size() == 0) {
			this.config = VertexConfig.fromTessellator(tessellator);
		}else {
			int compareStatus = VertexConfig.compare(config, tessellator);
			if(compareStatus != 0) {
				throw new RuntimeException("VertexConfig Mismatch!\n" + this.config + "\n" + VertexConfig.fromTessellator(tessellator) + "\nError: " + compareStatus);
			}
		}
		
		if(tessellator.addedVertices == 0) {
			return null;
		}
		
		if(entries.size() == 0) {
			return insertAt(tessellator, 0);
		}
		
		int bytes = tessellator.buffer.position();
		
		if(entries.size() == 1) {
			Entry entry = entries.get(0);
			if(entry.pos > bytes) {
				return insertAt(tessellator, 0);
			}else {
				return insertAt(tessellator, 1);
			}
		}
		
		for(int i = 0; i < entries.size() - 1; i++) {
			Entry entry = entries.get(i);
			Entry nextEntry = entries.get(i + 1);
			
			int availableSpace = nextEntry.pos - (entry.pos + entry.size);
			if(availableSpace > bytes) {
				return insertAt(tessellator, i + 1);
			}
		}
		
		return insertAt(tessellator, entries.size());
	}
	
	private Entry insertAt(CustomTessellator tessellator, int listIndex) {
		int pos = 0;
		int bytes = tessellator.buffer.position();
		
		if(listIndex > 0) {
			Entry prev = entries.get(listIndex - 1);
			pos = prev.pos + prev.size;
		}
		
		if(pos + bytes >= capacity) {
			throw new RuntimeException("Buffer is full!");
		}
		
		Entry entry = new Entry(pos, bytes);
		
		if(listIndex > 0) {
			Entry previousEntry = entries.get(listIndex - 1);
			if(previousEntry.pos + previousEntry.size > pos) {
				throw new RuntimeException("Entry collision: " + entry + " and " + previousEntry);
			}
		}
		if(listIndex < entries.size() - 1) {
			Entry nextEntry = entries.get(listIndex + 1);
			if(nextEntry.pos <= pos + bytes) {
				throw new RuntimeException("Entry collision: " + entry + " and " + nextEntry);
			}
		}
		
		tessellator.buffer.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferSubData(GL_ARRAY_BUFFER, pos, tessellator.buffer);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		entries.add(listIndex, entry);
		
		return entry;
	}
	
	public boolean remove(Entry entry) {
		return this.entries.remove(entry);
	}
	
	public int getVBO() {
		return vbo;
	}
	
	public VertexConfig getVertexConfig() {
		return config;
	}
	
	public class Entry {
		
		private int pos;
		private int size;
		
		private final int hashCode = entryHashCounter++;
		
		public Entry(int pos, int size) {
			this.pos = pos;
			this.size = size;
		}
		
		public int getPos() {
			return pos;
		}
		
		public int getSize() {
			return size;
		}
		
		@Override
		public String toString() {
			return "[pos: " + pos + ", size: " + size + "]";
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
		
	}

}

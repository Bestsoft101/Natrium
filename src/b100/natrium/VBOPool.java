package b100.natrium;

import static org.lwjgl.opengl.GL15.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class VBOPool {
	
	public List<Entry> entries = new ArrayList<>();

	private int vbo;
	private int capacity;
	
	private int entryHashCounter;
	
	public VBOPool(int initialSize) {
		this.capacity = initialSize;
		this.vbo = glGenBuffers();
		
		ByteBuffer buffer = BufferHelper.createByteBuffer(initialSize);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public Entry add(CustomTessellator vertexData) {
		if(vertexData.addedVertices == 0) {
			return null;
		}
		
		if(entries.size() == 0) {
			return insertAt(vertexData, 0);
		}
		
		int bytes = vertexData.byteBuffer.position();
		
		if(entries.size() == 1) {
			Entry entry = entries.get(0);
			if(entry.pos > bytes) {
				return insertAt(vertexData, 0);
			}else {
				return insertAt(vertexData, 1);
			}
		}
		
		for(int i = 0; i < entries.size() - 1; i++) {
			Entry entry = entries.get(i);
			Entry nextEntry = entries.get(i + 1);
			
			int availableSpace = nextEntry.pos - (entry.pos + entry.size);
			if(availableSpace > bytes) {
				return insertAt(vertexData, i + 1);
			}
		}
		
		return insertAt(vertexData, entries.size());
	}
	
	private Entry insertAt(CustomTessellator tessellator, int listIndex) {
		int pos = 0;
		int bytes = tessellator.byteBuffer.position();
		
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
		
		tessellator.byteBuffer.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferSubData(GL_ARRAY_BUFFER, pos, tessellator.byteBuffer);
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

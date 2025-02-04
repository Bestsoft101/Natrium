package b100.natrium;

import static org.lwjgl.opengl.GL15.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL31;

import net.minecraft.client.render.OpenGLHelper;

/**
 * 		A large buffer that contains vertex data
 * <br> Elements can have different vertex configurations
 * <br> When the buffer is full, it will automatically expand
 */
public class VertexBuffer {
	private int vbo;
	private long capacity;
	
	public List<Entry> entries = new ArrayList<>();
	
	private int entryHashCounter;
	
	public VertexBuffer(long initialCapacity) {
		this.capacity = initialCapacity;
		
		vbo = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, capacity, GL_STATIC_DRAW);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	public Entry addVertexData(VertexData vertexData) {
		if(vertexData.vertexCount <= 0) {
			return null;
		}
		
		// If the buffer is empty, just put it at the start
		if(entries.size() == 0) {
			return insertAtIndex(vertexData, 0);
		}
		
		int bytes = vertexData.buffer.position();
		
		// If there is a single element in the buffer, check if there is enough space in front of it
		// If not put it behind the other element
		if(entries.size() == 1) {
			Entry entry = entries.get(0);
			if(entry.position > bytes) {
				return insertAtIndex(vertexData, 0);
			}else {
				return insertAtIndex(vertexData, 1);
			}
		}
		
		// Check between every single element for a large enough gap
		for(int i = 0; i < entries.size() - 1; i++) {
			Entry entry = entries.get(i);
			Entry nextEntry = entries.get(i + 1);
			
			int availableSpace = nextEntry.position - (entry.position + entry.size);
			if(availableSpace > bytes) {
				return insertAtIndex(vertexData, i + 1);
			}
		}
		
		// Put it all the way at the end
		return insertAtIndex(vertexData, entries.size());
	}
	
	private Entry insertAtIndex(VertexData vertexData, int listIndex) {
		int pos = 0;
		int bytes = vertexData.buffer.position();
		
		if(listIndex > 0) {
			Entry prev = entries.get(listIndex - 1);
			pos = prev.position + prev.size;
		}
		
		if(pos + bytes >= capacity) {
			expandVBO();
		}
		
		Entry entry = new Entry(pos, bytes);
		
		if(listIndex > 0) {
			Entry previousEntry = entries.get(listIndex - 1);
			if(previousEntry.position + previousEntry.size > pos) {
				throw new RuntimeException("Entry collision: " + entry + " and " + previousEntry);
			}
		}
		if(listIndex < entries.size() - 1) {
			Entry nextEntry = entries.get(listIndex + 1);
			if(nextEntry.position <= pos + bytes) {
				throw new RuntimeException("Entry collision: " + entry + " and " + nextEntry);
			}
		}
		
		vertexData.buffer.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferSubData(GL_ARRAY_BUFFER, pos, vertexData.buffer);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		entries.add(listIndex, entry);
		
		return entry;
	}
	
	private void expandVBO() {
		final long prevCapacity = this.capacity;
		long newCapacity = this.capacity * 2L;
		if(newCapacity < capacity) {
			// future proof
			throw new RuntimeException();
		}
		
		NatriumMod.print("Expand VBO Buffer: " + newCapacity);
		
		OpenGLHelper.checkError("pre expand buffer");
		
		int newBuffer = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, newBuffer);
		glBufferData(GL_ARRAY_BUFFER, newCapacity, GL_STATIC_DRAW);
		
		OpenGLHelper.checkError("create expand buffer");
		
		if(OpenGLHelper.gl31) {
			glBindBuffer(GL31.GL_COPY_READ_BUFFER, this.vbo);
			glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, newBuffer);
			
			GL31.glCopyBufferSubData(GL31.GL_COPY_READ_BUFFER, GL31.GL_COPY_WRITE_BUFFER, 0, 0, this.capacity);
			
			glBindBuffer(GL31.GL_COPY_READ_BUFFER, 0);
			glBindBuffer(GL31.GL_COPY_WRITE_BUFFER, 0);	
		}else {
			// confirmed to work on trash hardware
			
			int iPrevCapacity = (int) prevCapacity;
			int iNewCapacity = (int) newCapacity;

			if(iPrevCapacity < 0) throw new RuntimeException("Previous capacity < 0");
			if(iNewCapacity < 0) throw new RuntimeException("New capacity < 0");
			if(iNewCapacity < iPrevCapacity) throw new RuntimeException("New capacity < previous capacity: " + iNewCapacity + " < " + iPrevCapacity);
			
			ByteBuffer oldData = ByteBuffer.allocateDirect(iPrevCapacity).order(ByteOrder.nativeOrder());
			
			glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
			glGetBufferSubData(GL_ARRAY_BUFFER, 0L, oldData);
			
			oldData.position(0);
			
			glBindBuffer(GL_ARRAY_BUFFER, newBuffer);
			glBufferSubData(GL_ARRAY_BUFFER, 0L, oldData);
		}
		
		OpenGLHelper.checkError("copy buffer");
		
		this.capacity = newCapacity;
		this.vbo = newBuffer;
	}
	
	public boolean removeEntry(Entry entry) {
		return entries.remove(entry);
	}
	
	public int getVbo() {
		return vbo;
	}
	
	public int entryCount() {
		return entries.size();
	}
	
	/**
	 * A reference to some vertex data that has been stored in this buffer
	 */
	public class Entry {
		
		public final int position;
		public final int size;
		
		private final int hashCode = entryHashCounter++;
		
		public Entry(int position, int size) {
			this.position = position;
			this.size = size;
		}
		
		@Override
		public String toString() {
			return "VertexBufferEntry[position: " + position + ", size: " + size + "]";
		}
		
		@Override
		public int hashCode() {
			return hashCode;
		}
		
	}

}

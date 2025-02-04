package b100.natrium;

import static org.lwjgl.opengl.GL14.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import b100.natrium.VertexBuffer.Entry;
import net.minecraft.core.util.helper.Buffer;

/**
 * 		Can render a lot of objects with a single call
 * <br> All objects need to have the same {@link VertexConfig}
 * <br> Entries can be toggled visible or invisible quickly
 */
public class RenderList {
	
	public final VertexBuffer vertexBuffer;
	
	private VertexConfig vertexConfig;
	private boolean vertexConfigLocked = false;
	
	private IntBuffer posBuffer;
	private IntBuffer sizeBuffer;
	
	private List<Entry> entryList = new ArrayList<>();
	private Set<Entry> visibleEntries = new HashSet<>();
	
	private boolean visibleEntriesUpdated = true;
	
	public RenderList(VertexBuffer vertexBuffer, int capacity) {
		this.vertexBuffer = vertexBuffer;
		this.posBuffer = Buffer.createBuffer(capacity * 4).asIntBuffer();
		this.sizeBuffer = Buffer.createBuffer(capacity * 4).asIntBuffer();
	}
	
	public void draw() {
		if(entryList.size() == 0) {
//			System.out.println("No Entries!");
			return;
		}
		
		if(visibleEntriesUpdated) {
			int vertexSize = vertexConfig.getVertexSize();
			
			posBuffer = expandIfNecessary(posBuffer, visibleEntries.size());
			sizeBuffer = expandIfNecessary(sizeBuffer, visibleEntries.size());
			
			posBuffer.clear();
			sizeBuffer.clear();
			
			for(Entry entry : visibleEntries) {
				posBuffer.put(entry.position / vertexSize);
				sizeBuffer.put(entry.size / vertexSize);
			}
			
			posBuffer.flip();
			sizeBuffer.flip();
			
			visibleEntriesUpdated = false;
		}
		
		if(visibleEntries.size() == 0) {
//			System.out.println("No Visible Entries!");
			return;
		}
		
//		System.out.println("Rendering " + visibleEntries.size() + " entries");
		glMultiDrawArrays(vertexConfig.drawMode, posBuffer, sizeBuffer);
	}
	
	public Entry add(VertexData vertexData, boolean visible) {
		if(vertexData.vertexCount <= 0) {
			return null;
		}
		
		if(entryList.size() == 0 && !vertexConfigLocked) {
			this.vertexConfig = vertexData.config.copy();
		}else {
			VertexConfig.compareThrow(vertexConfig, vertexData.config);
		}
		
		Entry entry = vertexBuffer.addVertexData(vertexData);
		entryList.add(entry);
		
		if(visible) {
			visibleEntries.add(entry);
			visibleEntriesUpdated = true;
		}
		
		return entry;
	}
	
	public void setVisible(Entry entry, boolean visible) {
		if(entry == null) {
			throw new NullPointerException();
		}
		if(visible) {
			if(visibleEntries.add(entry)) {
				visibleEntriesUpdated = true;
			}
		}else {
			if(visibleEntries.remove(entry)) {
				visibleEntriesUpdated = true;
			}
		}
	}
	
	public void lockVertexConfig(VertexConfig vertexConfig) {
		if(vertexConfig == null) {
			throw new NullPointerException("VertexConfig cannot be null!");
		}
		if(entryList.size() > 0 && !this.vertexConfig.equals(vertexConfig)) {
			VertexConfig.compareThrow(this.vertexConfig, vertexConfig);
		}
		this.vertexConfig = vertexConfig.copy();
		this.vertexConfigLocked = true;
	}
	
	public boolean remove(Entry entry) {
		if(entryList.remove(entry)) {
			vertexBuffer.removeEntry(entry);
			visibleEntries.remove(entry);
			visibleEntriesUpdated = true;
			return true;
		}
		return false;
	}
	
	public int size() {
		return entryList.size();
	}
	
	public int visibleEntryCount() {
		return visibleEntries.size();
	}
	
	public VertexConfig getVertexConfig() {
		return vertexConfig;
	}
	
	public boolean isVertexConfigLocked() {
		return vertexConfigLocked;
	}
	
	public static IntBuffer expandIfNecessary(IntBuffer buffer, int requiredCapacity) {
		if(buffer.capacity() < requiredCapacity) {
			int newCapacity = Math.max(buffer.capacity() * 2, requiredCapacity);
			
			NatriumMod.print("Expanding RenderList IntBuffer from " + buffer.capacity() + " to " + newCapacity + " ints");
			
			return Buffer.createBuffer(newCapacity << 2).asIntBuffer();
		}
		return buffer;
	}

}

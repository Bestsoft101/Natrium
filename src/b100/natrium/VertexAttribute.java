package b100.natrium;

import java.nio.ByteBuffer;

/**
 * Can be used by mods
 */
public abstract class VertexAttribute {
	
	public final String name;
	public final int id;
	public final int type;
	
	public VertexAttribute(String name, int id, int type) {
		this.name = name;
		this.id = id;
		this.type = type;
	}
	
	public abstract void addVertex(ByteBuffer buffer);
	
	public abstract int getSize();
	
	public abstract int getTypeSize();
	
	public static int compare(VertexAttribute a1, VertexAttribute a2) {
		if(a1 == a2) {
			return 0;
		}
		
		if(!a1.name.equals(a2.name)) return 1;
		if(a1.id != a2.id) return 2;
		if(a1.type != a2.type) return 3;
		if(a1.getSize() != a2.getSize()) return 4;
		
		return 0;
	}
	

}

package b100.natrium.vertex;

import java.nio.ByteBuffer;

public abstract class VertexComponent {
	
	public abstract void addVertex(ByteBuffer buffer);
	
	public abstract void enable(int vertexSize, ByteBuffer buffer);
	
	public abstract void enable(int vertexSize, long offset);
	
	public abstract void disable();
	
	public abstract int getSize();

}

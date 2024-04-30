package b100.natrium.vertex;

import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;

public class VertexComponentPositionFloat extends VertexComponent {

	public float x;
	public float y;
	public float z;

	@Override
	public void addVertex(ByteBuffer buffer) {
		buffer.putFloat(x);
		buffer.putFloat(y);
		buffer.putFloat(z);
	}
	
	@Override
	public void enable(int vertexSize, ByteBuffer buffer) {
		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(3, GL_FLOAT, vertexSize, buffer);
	}
	
	@Override
	public void enable(int vertexSize, long offset) {
		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(3, GL_FLOAT, vertexSize, offset);
	}

	@Override
	public void disable() {
		glDisableClientState(GL_VERTEX_ARRAY);
	}

	@Override
	public int getSize() {
		return 12;
	}

}

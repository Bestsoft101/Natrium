package b100.natrium.vertex;

import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;

public class VertexComponentColor extends VertexComponent {

	public int color;
	
	@Override
	public void addVertex(ByteBuffer buffer) {
		buffer.putInt(color);
	}
	
	@Override
	public void enable(int vertexSize, ByteBuffer buffer) {
		glEnableClientState(GL_COLOR_ARRAY);
		glColorPointer(4, GL_UNSIGNED_BYTE, vertexSize, buffer);
	}

	@Override
	public void enable(int vertexSize, long offset) {
		glEnableClientState(GL_COLOR_ARRAY);
		glColorPointer(4, GL_UNSIGNED_BYTE, vertexSize, offset);
	}

	@Override
	public void disable() {
		glDisableClientState(GL_COLOR_ARRAY);
	}

	@Override
	public int getSize() {
		return 4;
	}

}

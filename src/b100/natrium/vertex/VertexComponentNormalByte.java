package b100.natrium.vertex;

import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;

public class VertexComponentNormalByte extends VertexComponent {

	public byte x;
	public byte y;
	public byte z;

	@Override
	public void addVertex(ByteBuffer buffer) {
		buffer.put(x);
		buffer.put(y);
		buffer.put(z);
	}
	
	@Override
	public void enable(int vertexSize, ByteBuffer buffer) {
		glEnableClientState(GL_NORMAL_ARRAY);
		glNormalPointer(GL_BYTE, vertexSize, buffer);
	}
	
	@Override
	public void enable(int vertexSize, long offset) {
		glEnableClientState(GL_NORMAL_ARRAY);
		glNormalPointer(GL_BYTE, vertexSize, offset);
	}

	@Override
	public void disable() {
		glDisableClientState(GL_NORMAL_ARRAY);
	}

	@Override
	public int getSize() {
		return 3;
	}

}

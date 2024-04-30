package b100.natrium.vertex;

import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;

public class VertexComponentTexCoordFloat extends VertexComponent {

	public float u;
	public float v;
	
	@Override
	public void addVertex(ByteBuffer buffer) {
		buffer.putFloat(u);
		buffer.putFloat(v);
	}

	@Override
	public void enable(int vertexSize, ByteBuffer buffer) {
		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		glTexCoordPointer(2, GL_FLOAT, vertexSize, buffer);
	}

	@Override
	public void enable(int vertexSize, long offset) {
		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		glTexCoordPointer(2, GL_FLOAT, vertexSize, offset);
	}

	@Override
	public void disable() {
		glDisableClientState(GL_TEXTURE_COORD_ARRAY);
	}

	@Override
	public int getSize() {
		return 8;
	}

}

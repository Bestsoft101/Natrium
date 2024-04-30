package b100.natrium.vertex;

import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;

public class VertexAttributeFloat extends VertexAttribute {

	public float value;
	
	public VertexAttributeFloat(String name, int id) {
		super(name, id, GL_FLOAT);
	}

	@Override
	public void addVertex(ByteBuffer buffer) {
		buffer.putFloat(value);
	}

	@Override
	public int getSize() {
		return 1;
	}

	@Override
	public int getTypeSize() {
		return 4;
	}

}

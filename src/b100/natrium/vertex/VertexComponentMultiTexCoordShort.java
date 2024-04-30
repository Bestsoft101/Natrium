package b100.natrium.vertex;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

import java.nio.ByteBuffer;

public class VertexComponentMultiTexCoordShort extends VertexComponent {
	
	public int textureIndex;
	
	public int texCoord;
	
	public VertexComponentMultiTexCoordShort(int texture) {
		this.textureIndex = texture;
	}
	
	@Override
	public void addVertex(ByteBuffer buffer) {
		buffer.putInt(texCoord);
	}

	@Override
	public void enable(int vertexSize, ByteBuffer buffer) {
		glClientActiveTexture(textureIndex);
		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		glTexCoordPointer(2, GL_SHORT, vertexSize, buffer);
		glClientActiveTexture(GL_TEXTURE0);
	}

	@Override
	public void enable(int vertexSize, long offset) {
		glClientActiveTexture(textureIndex);
		glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		glTexCoordPointer(2, GL_SHORT, vertexSize, offset);
		glClientActiveTexture(GL_TEXTURE0);
	}

	@Override
	public void disable() {
		glClientActiveTexture(textureIndex);
		glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		glClientActiveTexture(GL_TEXTURE0);
	}

	@Override
	public int getSize() {
		return 4;
	}

}

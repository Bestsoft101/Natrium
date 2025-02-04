package b100.natrium;

import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;

import net.minecraft.core.util.helper.Buffer;

public class VertexData {
	
	public ByteBuffer buffer;
	
	public VertexConfig config = new VertexConfig();
	
	public int vertexCount;
	
	public VertexData(int size) {
		buffer = Buffer.createBuffer(size);
	}
	
	public void drawAll() {
		draw(0, vertexCount);
	}
	
	public void draw(int first, int count) {
		if(count == 0) {
			return;
		}
		
		buffer.limit(config.getVertexSize() * vertexCount);
		config.enableDirect(buffer);
		glDrawArrays(config.drawMode, first, count);
		config.disable();
	}

}

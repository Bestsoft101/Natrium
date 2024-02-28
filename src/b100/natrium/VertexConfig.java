package b100.natrium;

import net.minecraft.client.render.Tessellator;

public class VertexConfig {

	public int drawMode;
	public boolean enableColor = false;
	public boolean enableTexcoord = false;
	public boolean enableNormal = false;
	
	public int getVertexSize() {
		int size = 12;
		
		if(enableColor) size += 4;
		if(enableTexcoord) size += 8;
		if(enableNormal) size += 3;
		
		return size;
	}
	
	public VertexConfig copy() {
		VertexConfig copy = new VertexConfig();
		copy.drawMode = drawMode;
		copy.enableColor = enableColor;
		copy.enableTexcoord = enableTexcoord;
		copy.enableNormal = enableNormal;
		return copy;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof VertexConfig) {
			return compare(this, (VertexConfig) obj);
		}
		return false;
	}
	
	public static boolean compare(VertexConfig c1, VertexConfig c2) {
		if(c1.drawMode != c2.drawMode) return false;
		if(c1.enableColor != c2.enableColor) return false;
		if(c1.enableTexcoord != c2.enableTexcoord) return false;
		if(c1.enableNormal != c2.enableNormal) return false;
		return true;
	}
	
	public static VertexConfig fromTessellator(Tessellator tessellator) {
		VertexConfig vertexConfig = new VertexConfig();
		vertexConfig.enableColor = tessellator.hasColor;
		vertexConfig.enableTexcoord = tessellator.hasTexture;
		vertexConfig.enableNormal = tessellator.hasNormals;
		vertexConfig.drawMode = tessellator.drawMode;
		return vertexConfig;
	}

}

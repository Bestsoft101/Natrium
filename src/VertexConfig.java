public class VertexConfig {

	public int drawMode;
	public boolean enableColor = false;
	public boolean enableTexcoord = false;
	public boolean enableNormal = false;
	public boolean enableBrightness = false;
	
	public int getVertexSize() {
		int size = 12;
		
		if(enableColor) size += 4;
		if(enableTexcoord) size += 8;
		if(enableNormal) size += 3;
		if(enableBrightness) size += 4;
		
		return size;
	}
	
	public VertexConfig copy() {
		VertexConfig copy = new VertexConfig();
		copy.drawMode = drawMode;
		copy.enableColor = enableColor;
		copy.enableTexcoord = enableTexcoord;
		copy.enableNormal = enableNormal;
		copy.enableBrightness = enableBrightness;
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
		if(c1.enableBrightness != c2.enableBrightness) return false;
		return true;
	}
	
	public static VertexConfig fromTessellator(CustomTessellator tessellator) {
		VertexConfig vertexConfig = new VertexConfig();
		vertexConfig.drawMode = tessellator.u;
		vertexConfig.enableBrightness = tessellator.lightmapEnabled();
		vertexConfig.enableColor = tessellator.colorEnabled();
		vertexConfig.enableTexcoord = tessellator.texcoordEnabled();
		vertexConfig.enableNormal = tessellator.normalEnabled();
		return vertexConfig;
	}
	
	@Override
	public String toString() {
		return "VertexConfig[mode="+drawMode+",lightmap="+enableBrightness+",color="+enableColor+",texcoord="+enableTexcoord+",normal="+enableNormal+"]";
	}

}

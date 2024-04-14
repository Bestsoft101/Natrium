package b100.natrium;

import java.util.ArrayList;
import java.util.List;

import b100.json.element.JsonObject;

public class VertexConfig {

	public int drawMode;
	public boolean enableColor = false;
	public boolean enableTexcoord = false;
	public boolean enableNormal = false;
	public final List<VertexAttribute> vertexAttribs = new ArrayList<>();
	
	public int getVertexSize() {
		int size = 12;
		
		if(enableColor) size += 4;
		if(enableTexcoord) size += 8;
		if(enableNormal) size += 3;
		for(int i=0; i < vertexAttribs.size(); i++) {
			size += vertexAttribs.get(i).getTypeSize();
		}
		
		return size;
	}
	
	public VertexConfig copy() {
		VertexConfig copy = new VertexConfig();
		copy.drawMode = drawMode;
		copy.enableColor = enableColor;
		copy.enableTexcoord = enableTexcoord;
		copy.enableNormal = enableNormal;
		copy.vertexAttribs.addAll(vertexAttribs);
		return copy;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof VertexConfig) {
			return compare(this, (VertexConfig) obj) == 0;
		}
		return false;
	}
	
	public static int compare(VertexConfig c1, VertexConfig c2) {
		if(c1.drawMode != c2.drawMode) return 1;
		if(c1.enableColor != c2.enableColor) return 2;
		if(c1.enableTexcoord != c2.enableTexcoord) return 3;
		if(c1.enableNormal != c2.enableNormal) return 4;
		
		if(c1.vertexAttribs.size() != c2.vertexAttribs.size()) {
			return 5;
		}
		
		for(int i=0; i < c1.vertexAttribs.size(); i++) {
			VertexAttribute attrib1 = c1.vertexAttribs.get(i);
			VertexAttribute attrib2 = c2.vertexAttribs.get(i);
			int compare = VertexAttribute.compare(attrib1, attrib2); 
			if(compare != 0) {
				return (6 + i) * 1000 + compare;
			}
		}
		
		return 0;
	}
	
	public static VertexConfig fromTessellator(CustomTessellator tessellator) {
		VertexConfig vertexConfig = new VertexConfig();
		vertexConfig.enableColor = tessellator.hasColor;
		vertexConfig.enableTexcoord = tessellator.hasTexture;
		vertexConfig.enableNormal = tessellator.hasNormals;
		vertexConfig.drawMode = tessellator.drawMode;
		vertexConfig.vertexAttribs.addAll(tessellator.vertexAttribs);
		return vertexConfig;
	}
	
	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.set("drawMode", drawMode);
		object.set("enableColor", enableColor);
		object.set("enableTexcoord", enableTexcoord);
		object.set("enableNormal", enableNormal);
		if(vertexAttribs.size() > 0) {
			JsonObject attribsJson = new JsonObject();
			
			attribsJson.set("size", vertexAttribs.size());
			
			for(int i=0; i < vertexAttribs.size(); i++) {
				VertexAttribute attribute = vertexAttribs.get(i);
				JsonObject attribObject = new JsonObject();
				
				attribObject.set("id", attribute.id);
				attribObject.set("type", attribute.type);
				
				attribsJson.set(attribute.name, attribObject);
			}
			
			object.set("attribs", attribsJson);
		}
		return object;
	}
	
	@Override
	public String toString() {
		return toJson().toString();
	}

}

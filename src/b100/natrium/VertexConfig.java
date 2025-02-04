package b100.natrium;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.b100.json.element.JsonObject;

public class VertexConfig {
	
	public int drawMode;
	public boolean enableColor = false;
	public boolean enableTexture = false;
	public boolean enableNormal = false;
	public boolean enableLightmap = false;
	public final List<VertexAttribute> attributes = new ArrayList<>();
	
	public void enableDirect(ByteBuffer buffer) {
		int vertexSize = getVertexSize();
		int offset = 0;
		
		buffer.position(offset);
		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(3, GL_FLOAT, vertexSize, buffer);
		offset += 12;
		
		if(enableColor) {
			buffer.position(offset);
			glColorPointer(4, GL_UNSIGNED_BYTE, vertexSize, buffer);
			glEnableClientState(GL_COLOR_ARRAY);
			offset += 4;
		}else {
			glDisableClientState(GL_COLOR_ARRAY);
		}
		
		if(enableTexture) {
			buffer.position(offset);
			glTexCoordPointer(2, GL_FLOAT, vertexSize, buffer);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			offset += 8;
		}else {
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		}
		
		if(enableNormal) {
			buffer.position(offset);
			glNormalPointer(GL_BYTE, vertexSize, buffer);
			glEnableClientState(GL_NORMAL_ARRAY);
			offset += 3;
		}else {
			glDisableClientState(GL_NORMAL_ARRAY);
		}
		
		glClientActiveTexture(GL_TEXTURE1);
		if(enableLightmap) {
			buffer.position(offset);
			glTexCoordPointer(2, GL_SHORT, vertexSize, buffer);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			offset += 4;
		}else {
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		}
		glClientActiveTexture(GL_TEXTURE0);
		
		for(int i=0; i < attributes.size(); i++) {
			VertexAttribute attrib = attributes.get(i);
			
			buffer.position(offset);
			glEnableVertexAttribArray(attrib.id);
			glVertexAttribPointer(attrib.id, attrib.getSize(), attrib.type, false, vertexSize, buffer);
			offset += attrib.getTypeSize();
		}
	}
	
	public void enableVBO(int vbo) {
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		
		int vertexSize = getVertexSize();
		int offset = 0;
		
		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(3, GL_FLOAT, vertexSize, offset);
		offset += 12;
		
		if(enableColor) {
			glColorPointer(4, GL_UNSIGNED_BYTE, vertexSize, offset);
			glEnableClientState(GL_COLOR_ARRAY);
			offset += 4;
		}else {
			glDisableClientState(GL_COLOR_ARRAY);
		}
		
		if(enableTexture) {
			glTexCoordPointer(2, GL_FLOAT, vertexSize, offset);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			offset += 8;
		}else {
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		}
		
		if(enableNormal) {
			glNormalPointer(GL_BYTE, vertexSize, offset);
			glEnableClientState(GL_NORMAL_ARRAY);
			offset += 3;
		}else {
			glDisableClientState(GL_NORMAL_ARRAY);
		}
		
		if(enableLightmap) {
			glClientActiveTexture(GL_TEXTURE1);
			glTexCoordPointer(2, GL_SHORT, vertexSize, offset);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			offset += 4;
			glClientActiveTexture(GL_TEXTURE0);
		}
		
		for(int i=0; i < attributes.size(); i++) {
			VertexAttribute attrib = attributes.get(i);
			
			glEnableVertexAttribArray(attrib.id);
			glVertexAttribPointer(attrib.id, attrib.getSize(), attrib.type, false, vertexSize, offset);
			offset += attrib.getTypeSize();
		}
	}
	
	public void disable() {
		if(enableLightmap) {
			glClientActiveTexture(GL_TEXTURE1);
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
			glClientActiveTexture(GL_TEXTURE0);
		}
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		for(int i=0; i < attributes.size(); i++) {
			VertexAttribute attrib = attributes.get(i);
			glDisableVertexAttribArray(attrib.id);
		}
	}
	
	public int getVertexSize() {
		int size = 12;
		
		if(enableColor) size += 4;
		if(enableTexture) size += 8;
		if(enableNormal) size += 3;
		if(enableLightmap) size += 4;
		
		for(int i=0; i < attributes.size(); i++) {
			size += attributes.get(i).getTypeSize();
		}
		
		return size;
	}
	
	public VertexConfig copy() {
		VertexConfig copy = new VertexConfig();
		copy.drawMode = drawMode;
		copy.enableColor = enableColor;
		copy.enableTexture = enableTexture;
		copy.enableNormal = enableNormal;
		copy.enableLightmap = enableLightmap;
		copy.attributes.addAll(attributes);
		return copy;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof VertexConfig) {
			return compare(this, (VertexConfig) obj) == 0;
		}
		return false;
	}
	
	/**
	 * Only used for debugging
	 */
	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		object.set("drawMode", drawMode);
		object.set("enableColor", enableColor);
		object.set("enableTexcoord", enableTexture);
		object.set("enableNormal", enableNormal);
		object.set("enableLightmap", enableLightmap);
		if(attributes.size() > 0) {
			JsonObject attribsJson = new JsonObject();
			
			attribsJson.set("size", attributes.size());
			
			for(int i=0; i < attributes.size(); i++) {
				VertexAttribute attribute = attributes.get(i);
				JsonObject attribObject = new JsonObject();
				
				attribObject.set("id", attribute.id);
				attribObject.set("type", attribute.type);
				
				attribsJson.set(attribute.name, attribObject);
			}
			
			object.set("attribs", attribsJson);
		}
		return object;
	}
	
	public static int compare(VertexConfig c1, VertexConfig c2) {
		if(c1.drawMode != c2.drawMode) return 1;
		if(c1.enableColor != c2.enableColor) return 2;
		if(c1.enableTexture != c2.enableTexture) return 3;
		if(c1.enableNormal != c2.enableNormal) return 4;
		if(c1.enableLightmap != c2.enableLightmap) return 4;
		
		if(c1.attributes.size() != c2.attributes.size()) {
			return 5;
		}
		
		for(int i=0; i < c1.attributes.size(); i++) {
			VertexAttribute attrib1 = c1.attributes.get(i);
			VertexAttribute attrib2 = c2.attributes.get(i);
			int compare = VertexAttribute.compare(attrib1, attrib2); 
			if(compare != 0) {
				return (6 + i) * 1000 + compare;
			}
		}
		
		return 0;
	}
	
	public static void compareThrow(VertexConfig config1, VertexConfig config2) {
		int compare = compare(config1, config2);
		if(compare != 0) {
			System.err.println("VertexConfig Mismatch:\n" + config1 + "\n!=\n" + config2);
			throw new RuntimeException("VertexConfig mismatch " + compare + "!");
		}	
	}
	
	@Override
	public String toString() {
		return toJson().toString();
	}

}

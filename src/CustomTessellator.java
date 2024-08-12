import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CustomTessellator extends TessellatorBase {
	
	public static boolean CRASH_WHEN_DISABLED = false;
	public static boolean ENABLE_ALL = false;
	public static boolean DONT_DRAW = false;
	
	public static final int COLOR_TEXTURE = GL_TEXTURE0;
	public static final int LIGHTMAP_TEXTURE = GL_TEXTURE1;
	
	public CustomTessellator(int size) {
		super(0);
		
		// byteBuffer
		this.d = NatriumUtils.createByteBuffer(size);
	}
	
	@Override
	public void startDrawing(int mode) {
		if(z) {
			if(DONT_DRAW) {
				return;
			}
			throw new RuntimeException("Already drawing!");
		}
		
		if(ENABLE_ALL) {
			setColorEnabled(true);
			setTexcoordEnabled(true);
			setLightmapEnabled(true);
			setNormalEnabled(true);
		}else {
			setColorEnabled(false);
			setTexcoordEnabled(false);
			setLightmapEnabled(false);
			setNormalEnabled(false);
		}
		
		// drawMode
		this.u = mode;
		
		// addedVertices
		this.s = 0;
		
		// byteBuffer
		this.d.clear();
		
		// drawing
		this.z = true;
		
		// lockColor
		this.t = false;
	}
	
	@Override
	public int draw() {
		if(DONT_DRAW) {
			return 0;
		}
		checkIsDrawing();
		
		// drawing
		this.z = false;
		
		if(this.s == 0) {
			return 0;
		}
		
		ByteBuffer byteBuffer = this.d;
		byteBuffer.flip();
		
		int vertexSize = getVertexSize();
		int offset = 0;
		
		glEnableClientState(GL_VERTEX_ARRAY);
		glVertexPointer(3, GL_FLOAT, vertexSize, byteBuffer);
		offset += 12;
		
		if(colorEnabled()) {
			byteBuffer.position(offset);
			glEnableClientState(GL_COLOR_ARRAY);
			glColorPointer(4, GL_UNSIGNED_BYTE, vertexSize, byteBuffer);
			offset += 4;
		}else {
			glDisableClientState(GL_COLOR_ARRAY);
		}
		
		if(texcoordEnabled()) {
			byteBuffer.position(offset);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			glTexCoordPointer(2, GL_FLOAT, vertexSize, byteBuffer);
			offset += 8;
		}else {
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		}
		
		if(normalEnabled()) {
			byteBuffer.position(offset);
			glEnableClientState(GL_NORMAL_ARRAY);
			glNormalPointer(GL_BYTE, vertexSize, byteBuffer);
			offset += 3;
		}else {
			glDisableClientState(GL_NORMAL_ARRAY);
		}
		
		glClientActiveTexture(LIGHTMAP_TEXTURE);
		if(lightmapEnabled()) {
			byteBuffer.position(offset);
			glTexCoordPointer(2, GL_SHORT, vertexSize, byteBuffer);
			glEnableClientState(GL_TEXTURE_COORD_ARRAY);
			offset += 4;
		}else {
			glDisableClientState(GL_TEXTURE_COORD_ARRAY);
		}
		glClientActiveTexture(COLOR_TEXTURE);
		
		glDrawArrays(u, 0, s);
		return 0;
	}
	
	@Override
	public void addVertex(double x, double y, double z) {
		if(parent != null) {
			addVertexFromSource(parent, x, y, z);
		}else {
			addVertexFromSource(this, x, y, z);
		}
	}
	
	private void addVertexFromSource(TessellatorBase source, double x, double y, double z) {
		checkIsDrawing();
		
		if(d.capacity() < d.position() + 64) {
			expandBuffer();
		}
		
		d.putFloat((float) (this.v + x));
		d.putFloat((float) (this.w + y));
		d.putFloat((float) (this.x + z));
		
		if(this.colorEnabled()) {
			d.putInt(source.m);
		}
		
		if(this.texcoordEnabled()) {
			d.putFloat((float) this.j);
			d.putFloat((float) this.k);
		}
		
		if(this.normalEnabled()) {
			d.put((byte) ((this.y >> 16) & 0xFF));
			d.put((byte) ((this.y >>  8) & 0xFF));
			d.put((byte) (this.y & 0xFF));
		}
		
		if(this.lightmapEnabled()) {
			d.putInt(source.l);
		}
		
		// addedVertices
		this.s++;
	}
	
	/**
	 * setTexcoord
	 */
	@Override
	public void a(double u, double v) {
		if(getVertexCount() > 0 && !texcoordEnabled()) {
			if(CRASH_WHEN_DISABLED) {
				throw new RuntimeException("Texcoord is disabled!");	
			}
			return;
		}
		// hasTexture
		setTexcoordEnabled(true);
		this.j = u;
		this.k = v;
	}
	
	/**
	 * setColorRGBA
	 */
	@Override
	public void setColor(int r, int g, int b, int a) {
		if(parent != null) {
			parent.setColor(r, g, b, a);
			return;
		}
		
		if(/*isColorLocked*/ this.t) {
			return;
		}
		if(getVertexCount() > 0 && !colorEnabled()) {
			if(CRASH_WHEN_DISABLED) {
				throw new RuntimeException("Color is disabled!");	
			}
			return;
		}
		
		setColorEnabled(true);
		
		r = NatriumUtils.clamp(r, 0, 255);
		g = NatriumUtils.clamp(g, 0, 255);
		b = NatriumUtils.clamp(b, 0, 255);
		a = NatriumUtils.clamp(a, 0, 255);

		// color
		this.m = a << 24 | b << 16 | g << 8 | r;
	}
	
	/**
	 * setNormal
	 */
	@Override
	public void b(float x, float y, float z) {
		if(getVertexCount() > 0 && !normalEnabled()) {
			if(CRASH_WHEN_DISABLED) {
				throw new RuntimeException("Normals are disabled!");	
			}
			return;
		}
		setNormalEnabled(true);
		
		byte bx = (byte) ((int) (x * 127.0F));
		byte by = (byte) ((int) (y * 127.0F));
		byte bz = (byte) ((int) (z * 127.0F));
		
		// normal
		this.y = bx << 16 | by << 8 | bz;
	}
	
	@Override
	public void setLightmapCoord(int lmc) {
		if(parent != null) {
			parent.setLightmapCoord(lmc);
			return;
		}
		
		if(getVertexCount() > 0 && !lightmapEnabled()) {
			if(CRASH_WHEN_DISABLED) {
				throw new RuntimeException("Brightness is disabled!");	
			}
			return;
		}
		
		setLightmapEnabled(true);
		this.l = lmc;
	}
	
	public int getVertexSize() {
		int size = 12;
		
		if(colorEnabled()) size += 4;
		if(texcoordEnabled()) size += 8;
		if(normalEnabled()) size += 3;
		if(lightmapEnabled()) size += 4;
		
		return size;
	}

	public void checkIsDrawing() {
		if (!this.z) {
			throw new IllegalStateException("Not tesselating!");
		}
	}
	
	public void expandBuffer() {
		int newSize = this.d.capacity() * 2;
		System.out.println("Expanding tessellator buffer to " + newSize);
		
		ByteBuffer newBuffer = NatriumUtils.createByteBuffer(newSize);
		newBuffer.clear();
		this.d.flip();
		newBuffer.put(this.d);
		this.d = newBuffer;
	}
	
	/**
	 * Used for OptiFine's connected textures
	 */
	@Override
	public baz getSubTessellator(int ctmTexture) {
		return ChunkRendererMultiDraw.currentRenderer.getSubTessellator(this, ctmTexture);
	}
	
	@Override
	public baz getSubTessellatorImpl(int var1) {
		throw new RuntimeException();
	}
	
	private CustomTessellator parent;
	private final List<CustomTessellator> subTessellators = new ArrayList<>();
	
	public void setParent(CustomTessellator parent) {
		if(parent == this) {
			throw new RuntimeException("Cannot set parent to itself");
		}
		if(this.parent != null) {
			this.parent.subTessellators.remove(this);
		}
		this.parent = parent;
		this.parent.subTessellators.add(this);
	}
	
	public void clearSubTessellators() {
		for(int i=0; i < subTessellators.size(); i++) {
			subTessellators.get(i).parent = null;
		}
		subTessellators.clear();
	}
}

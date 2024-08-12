import java.nio.ByteBuffer;

public abstract class TessellatorBase extends baz {
	
	public TessellatorBase(int size) {
		super(size);
	}
	
	public abstract void startDrawing(int mode);
	
	public abstract int draw();
	
	public abstract void addVertex(double x, double y, double z);
	
	public abstract void setLightmapCoord(int lmc);
	
	public abstract void setColor(int r, int g, int b, int a);
	
	@Override
	public final void b(int mode) {
		startDrawing(mode);
	}
	
	@Override
	public final int a() {
		return draw();
	}
	
	@Override
	public final void a(double x, double y, double z) {
		addVertex(x, y, z);
	}
	
	@Override
	public final void c(int lmc) {
		setLightmapCoord(lmc);
	}
	
	@Override
	public final void a(int r, int g, int b, int a) {
		setColor(r, g, b, a);
	}
	
	// Getters & Setters
	
	public int getVertexCount() {
		return this.s;
	}
	
	public void setVertexCount(int val) {
		this.s = val;
	}
	
	public ByteBuffer getByteBuffer() {
		return this.d;
	}
	
	public boolean isDrawing() {
		return this.z;
	}
	
	public void setDrawing(boolean val) {
		this.z = val;
	}
	
	public boolean colorEnabled() {
		return n;
	}
	
	public boolean texcoordEnabled() {
		return o;
	}
	
	public boolean lightmapEnabled() {
		return p;
	}
	
	public boolean normalEnabled() {
		return q;
	}
	
	public void setColorEnabled(boolean val) {
		this.n = val;
	}
	
	public void setTexcoordEnabled(boolean val) {
		this.o = val;
	}
	
	public void setLightmapEnabled(boolean val) {
		this.p = val;
	}
	
	public void setNormalEnabled(boolean val) {
		this.q = val;
	}

}

import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.opengl.Display;

public class NatriumProfiler {

	private static long[] frametimes = new long[512];
	private static int pos = 0;
	private static int scale = 200000;
	
	private static long lastUpdate = 0;
	
	public static void beforeUpdate() {
		long now = System.nanoTime();
		if(lastUpdate == 0) {
			lastUpdate = now;
			return;
		}
		
		long delta = now - lastUpdate;
		lastUpdate = now;
		frametimes[pos++] = delta;
		pos %= frametimes.length;
	}
	
	public static void afterUpdate() {
		if(NatriumMod.isDebugScreenVisible()) {
			render();
		}
	}
	
	public static void beforeTick() {
		
	}
	
	public static void afterTick() {
		
	}
	
	public static void render() {
		int w = Display.getWidth();
		int h = Display.getHeight();
		
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		glOrtho(0, w, h, 0, -1, 1);
		
		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		
		glDisable(GL_DEPTH_TEST);
		glDisable(GL_ALPHA_TEST);
		glDisable(GL_TEXTURE_2D);
		glDisable(GL_CULL_FACE);
		glDisable(GL_LIGHTING);
		glEnable(GL_BLEND);
		glBlendFunc(770, 771);
		
		TessellatorBase tessellator = (TessellatorBase) baz.a;
		drawFrameTimes(tessellator, 0, h, frametimes);
	}
	
	private static void drawFrameTimes(TessellatorBase tessellator, int x, int y, long[] frametimes) {
		int h1 = 16666666 / scale;
		int h2 = 33333333 / scale;
		
		tessellator.startDrawing(GL_QUADS);
		tessellator.setColor(0, 0, 0, 127);
		drawRectangle(tessellator, x, y - h2, frametimes.length, h2);
		drawRectangle(tessellator, x, y - h1, frametimes.length, h1);
		tessellator.setColor(255, 255, 255, 255);
		
		for(int i=0; i < frametimes.length; i++) {
			float brightness = ((i - pos) & (frametimes.length - 1)) / (float) frametimes.length;
			brightness = brightness * brightness;
			int b = NatriumUtils.clamp((int) (brightness * 255.0f), 0, 255);
			tessellator.setColor(b, b, b, 255);
			
			int time = (int) (frametimes[i] / /*scale*/ 200000);
			drawRectangle(tessellator, x + i, y - time, 1, time);
		}
		
		tessellator.draw();
	}
	
	private static void drawRectangle(TessellatorBase tessellator, int x, int y, int w, int h) {
		tessellator.addVertex(x, y, 0);
		tessellator.addVertex(x+w, y, 0);
		tessellator.addVertex(x+w, y+h, 0);
		tessellator.addVertex(x, y+h, 0);
	}

}

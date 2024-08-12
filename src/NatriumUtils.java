import static org.lwjgl.opengl.GL11.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Set;

public class NatriumUtils {
	
	private static final Set<String> errorStates = new HashSet<String>();
	
	public static ByteBuffer createByteBuffer(int size) {
		return ByteBuffer.allocateDirect(size).order(ByteOrder.nativeOrder());
	}
	
	public static IntBuffer createIntBuffer(int size) {
		return createByteBuffer(size << 2).asIntBuffer();
	}
	
	public static void checkError(String state) {
		int i = glGetError();
		if(i != GL_NO_ERROR) {
			if(!errorStates.contains(state)) {
				errorStates.add(state);
				System.out.println("###############################");
				System.out.println("OpenGL Error " + i + ": " + state);
				System.out.println("###############################");
			}
		}
	}
	
	public static int clamp(int val, int min, int max) {
		if(val < min) return min;
		if(val > max) return max;
		return val;
	}
	
	public static int floor_double(double d) {
		if(d < 0.0) {
			return ((int) d) + 1;
		}
		return (int) d;
	}

}

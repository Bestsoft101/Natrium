import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

public class NatriumDebugRender {
	
	private static int baseOffset = 0;
	private static int line = 0;
	
	private static boolean up = false;
	private static boolean down = false;
	private static boolean left = false;
	private static boolean right = false;
	
	public static int textureOverrideIndex = -1;
	public static int textureOverrideTexture;
	
	public static void render() {
		boolean holdCtrl = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL) || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
		
		if(holdCtrl) {
			boolean holdUp = holdCtrl && Keyboard.isKeyDown(Keyboard.KEY_UP);
			boolean holdDown = holdCtrl && Keyboard.isKeyDown(Keyboard.KEY_DOWN);
			boolean holdLeft = holdCtrl && Keyboard.isKeyDown(Keyboard.KEY_LEFT);
			boolean holdRight = holdCtrl && Keyboard.isKeyDown(Keyboard.KEY_RIGHT);
			
			if(!up && holdUp) {
				up = true;
				onDebugInput(Keyboard.KEY_UP);
			}
			if(!down && holdDown) {
				down = true;
				onDebugInput(Keyboard.KEY_DOWN);
			}
			if(!left && holdLeft) {
				left = true;
				onDebugInput(Keyboard.KEY_LEFT);
			}
			if(!right && holdRight) {
				right = true;
				onDebugInput(Keyboard.KEY_RIGHT);
			}

			up = holdUp;
			down = holdDown;
			left = holdLeft;
			right = holdRight;
		}else {
			up = false;
			down = false;
			left = false;
			right = false;
		}

		baseOffset = 124;
		line = 0;
		
		TerrainRenderer terrainRenderer = NatriumMod.terrainRenderer;
		
		drawDebugLine("Textures: " + terrainRenderer.textureCount() + " Draw Calls: " + terrainRenderer.drawCalls);
		if(textureOverrideIndex != -1) {
			drawDebugLine("Texture Override: [" + textureOverrideIndex + "] " + getTextureName(textureOverrideTexture));
		}
	}
	
	private static void onDebugInput(int key) {
		if(key == Keyboard.KEY_UP) {
			toggleTextureOverride(1);
		}
		if(key == Keyboard.KEY_DOWN) {
			toggleTextureOverride(-1);
		}
	}
	
	private static void toggleTextureOverride(int dir) {
		List<Integer> allTextures = NatriumMod.terrainRenderer.getAllTextures();
		
		if(textureOverrideIndex == -1) {
			textureOverrideIndex = dir < 0 ? allTextures.size() - 1 : 0;
			textureOverrideTexture = allTextures.get(textureOverrideIndex);
			return;
		}
		
		textureOverrideIndex += dir;
		
		if(textureOverrideIndex < 0 || textureOverrideIndex >= allTextures.size()) {
			textureOverrideIndex = -1;
			textureOverrideTexture = 0;
		}else {
			textureOverrideTexture = allTextures.get(textureOverrideIndex);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static String getTextureName(int texture) {
		bba renderEngine = NatriumMod.getMinecraft().o;
		
		Map<String, Integer> textureMap = renderEngine.c;
		for(String key : textureMap.keySet()) {
			int id = textureMap.get(key);
			if(id == texture) {
				return key;
			}
		}
		
		return "???";
	}
	
	private static void drawDebugLine(String string) {
		drawStringWithShadow("[Natrium] " + string, 2, baseOffset + line++ * 10, 0xFFFFFF);
	}
	
	public static void drawStringWithShadow(String string, int x, int y, int color) {
		atq fontRenderer = NatriumMod.getMinecraft().p;
		
		fontRenderer.a(string, x, y, color);
	}
	
	public static void drawString(String string, int x, int y, int color) {
		atq fontRenderer = NatriumMod.getMinecraft().p;
		
		fontRenderer.b(string, x, y, color);
	}

}

import java.util.ArrayList;
import java.util.List;

import b100.natrium.utils.CallbackInfo;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.LoaderState;
import net.minecraft.client.Minecraft;

public class NatriumMod {
	
	private static Minecraft mc;
	public static TerrainRenderer terrainRenderer = new TerrainRenderer();
	
	private static md lastViewEntity;
	private static float lastPartialTicks;
	
	private static final List<bdg> allAnimations = new ArrayList<>();
	public static List<bdg> moddedAnimations = new ArrayList<>();
	
	public static Minecraft getMinecraft() {
		if(mc == null) {
			mc = Minecraft.x();
		}
		return mc;
	}
	
	public static void crash() {
		throw new RuntimeException("funy crash");
	}
	
	public static void checkChunkRenderer(baj chunkRenderer) {
		if(chunkRenderer instanceof ChunkRendererMultiDraw) {
			return;
		}
		throw new RuntimeException("Created wrong chunk renderer: " + chunkRenderer.getClass());
	}
	
	public static int onRenderTerrain(bav renderGlobal, md viewEntity, int renderPass, double d) {
		float partialTicks = (float) d;
		terrainRenderer.renderTerrain(viewEntity, partialTicks);
		lastViewEntity = viewEntity;
		lastPartialTicks = partialTicks;
		return 0;
	}
	
	public static void onRenderWater() {
		terrainRenderer.renderTranslucentTerrain(lastViewEntity, lastPartialTicks);
	}
	
	public static int nop(bav bav, int i, double d) {
		return 0;
	}
	
	public static void onForgeBindTexture(String texture, int subID, CallbackInfo ci) {
		if(ChunkRendererMultiDraw.currentRenderer != null) {
			ChunkRendererMultiDraw.currentRenderer.bindTexture(texture);
			ci.setCancelled(true);
		}
	}
	
	public static void onRenderGui(atr guiIngame, float f, boolean b, int i, int j) {
		if(isDebugScreenVisible()) {
			NatriumDebugRender.render();
		}
	}
	
	public static boolean isDebugScreenVisible() {
		return getMinecraft().y.X;
	}
	
	@SuppressWarnings("unchecked")
	public static void beforeStateMessage(LoadController loadController, LoaderState loaderState, Object...eventData) {
		if(loaderState == LoaderState.INITIALIZATION) {
			bba renderEngine = getMinecraft().o;
			
			// Get a list of all registered animations
			allAnimations.clear();
			allAnimations.addAll(renderEngine.h);
		}
	}

	@SuppressWarnings("unchecked")
	public static void afterStateMessage(LoadController loadController, LoaderState loaderState, Object...eventData) {
		if(loaderState == LoaderState.INITIALIZATION) {
			bba renderEngine = getMinecraft().o;
			List<bdg> animations = renderEngine.h;
			
			// After this event all mods should have registered their custom animations
			// Now we can compare this list with the previous one to find out which animations got added
			for(int i=0; i < animations.size(); i++) {
				bdg anim = animations.get(i);
				if(!allAnimations.contains(anim)) {
					moddedAnimations.add(anim);
				}
			}
			allAnimations.clear();
			animations.removeAll(moddedAnimations);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void onRegisterCustomAnimations(bba renderEngine) {
		if(Config.isAnimatedTerrain()) {
			System.out.println("Add modded animations");
			
			// If animations are turned on, just add all modded animations
			for(int i=0; i < moddedAnimations.size(); i++) {
				bdg anim = moddedAnimations.get(i);
				if(!renderEngine.h.contains(anim)) {
					renderEngine.h.add(anim);
				}
			}
		}else {
			System.out.println("Remove modded animations");
			
			// If animations are turned off, add all modded animations,
			// update them a few times, and then remove them
			
			// TODO This currently only works when switching animations
			// on or off in OptiFine's settings, but it needs to work when the game is started
			
			// Add modded animations
			for(int i=0; i < moddedAnimations.size(); i++) {
				bdg anim = moddedAnimations.get(i);
				if(!renderEngine.h.contains(anim)) {
					renderEngine.h.add(anim);
				}
			}
			
			// Update animations
			renderEngine.a();
			
			// Remove modded animations
			for(int i=0; i < moddedAnimations.size(); i++) {
				bdg anim = moddedAnimations.get(i);
				renderEngine.h.remove(anim);
				anim.a();
			}
		}
	}
	
}

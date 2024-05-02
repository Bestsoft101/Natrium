package b100.natrium.asm;

import b100.natrium.CustomTessellator;
import b100.natrium.MultiDrawRenderList;
import b100.natrium.NatriumMod;
import b100.natrium.VBOPool;
import b100.natrium.asm.utils.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.ChunkRenderer;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.client.render.tessellator.Tessellator;

public class Listeners {
	
	private static Minecraft mc;
	
	private static final boolean shadersInstalled;
	
	static {
		boolean flag = true;
		try {
			b100.shaders.asm.Listeners.class.getName();
		}catch (Throwable e) {
			flag = false;
		}
		shadersInstalled = flag;
		NatriumMod.log("Shaders Installed: " + shadersInstalled);
	}
	
	public static void onStartGame() {
		mc = Minecraft.getMinecraft(Minecraft.class);
		
		Tessellator.instance = NatriumMod.customTessellator;
		
		NatriumMod.terrainRenderer.init(mc);
	}
	
	public static void onWorldChange() {
		NatriumMod.terrainRenderer.updateRenderOffsetNext = true;
	}
	
	public static int onSortAndRender(RenderGlobal renderGlobal, ICamera activeCamera, int renderPass, double partialTicks) {
		if(renderPass == 0) {
			NatriumMod.terrainRenderer.renderTerrain(activeCamera, (float) partialTicks);
		}
		if(renderPass == 1) {
			if(mc.gameSettings.fancyGraphics.value == 0) {
				// When graphics is set to fast, onCallAllDisplayLists is never called
				NatriumMod.terrainRenderer.renderTranslucentTerrain();
				return 0;
			}else {
				// When graphics is set to fancy, at this point the color mask is disabled, so don't render it here
				return 1;	
			}
		}
		return 0;
	}
	
	public static void onCallAllDisplayLists(RenderGlobal renderGlobal, int renderPass, double partialTicks) {
		if(renderPass == 1) {
			// Only called when graphics is set to fancy
			NatriumMod.terrainRenderer.renderTranslucentTerrain();	
		}
	}
	
	public static void onTessellatorConstructed(Tessellator tessellator, int size, CallbackInfo ci) {
		if(size <= 0) {
			ci.setCancelled(true);
		}
	}
	
	public static void onReloadChunks() {
		NatriumMod.terrainRenderer.onReloadChunks();
	}
	
	public static void resetChunkRenderer(ChunkRenderer chunkRenderer) {
		if(chunkRenderer.renderListEntries == null) {
			chunkRenderer.renderListEntries = new VBOPool.Entry[2];
		}
		for(int renderPass=0; renderPass < chunkRenderer.renderListEntries.length; renderPass++) {
			VBOPool.Entry entry = chunkRenderer.renderListEntries[renderPass];
			if(entry != null) {
				boolean removed = NatriumMod.terrainRenderer.renderLists[renderPass].remove(entry);
				if(!removed) {
					throw new RuntimeException("Not removed!");
				}
				chunkRenderer.renderListEntries[renderPass] = null;
			}
		}
	}
	
	public static void beforeRenderChunk(ChunkRenderer chunkRenderer) {
		resetChunkRenderer(chunkRenderer);
	}
	
	public static void startRenderingChunk(ChunkRenderer chunkRenderer, int renderPass) {
		CustomTessellator tessellator = NatriumMod.customTessellator;
		
		tessellator.setTranslation(-NatriumMod.terrainRenderer.renderOffsetX, 0, -NatriumMod.terrainRenderer.renderOffsetZ);
		tessellator.setColorRGBA(255, 255, 255, 255);
		if(shadersInstalled) {
			b100.shaders.asm.Listeners.onChunkRenderStart(tessellator);
		}
	}
	
	public static void stopRenderingChunk(ChunkRenderer chunkRenderer, int renderPass) {
		CustomTessellator tessellator = NatriumMod.customTessellator;
		tessellator.isDrawing = false;
		
		if(tessellator.autoNormal) {
			tessellator.calculateNormals();
		}
		
		MultiDrawRenderList renderList = NatriumMod.terrainRenderer.renderLists[renderPass];
		if(renderList == null) {
			throw new NullPointerException("RenderList for RenderPass " + renderPass + " is null!");
		}
		
		VBOPool.Entry entry = renderList.add(NatriumMod.customTessellator, chunkRenderer.isInFrustum);
		if(entry != null) {
			chunkRenderer.renderListEntries[renderPass] = entry;	
		}
	}
	
	public static void onUpdateInFrustum(ChunkRenderer chunkRenderer) {
		if(chunkRenderer.renderListEntries != null) {
			for(int i=0; i < chunkRenderer.renderListEntries.length; i++) {
				VBOPool.Entry entry = chunkRenderer.renderListEntries[i];
				if(entry != null) {
					NatriumMod.terrainRenderer.renderLists[i].setVisible(entry, chunkRenderer.isInFrustum);
				}
			}	
		}
	}

}

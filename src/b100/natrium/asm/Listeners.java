package b100.natrium.asm;

import b100.natrium.NatriumMod;
import b100.natrium.asm.utils.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.camera.ICamera;

public class Listeners {
	
	private static Minecraft mc;
	
	public static void onStartGame() {
		mc = Minecraft.getMinecraft(Minecraft.class);
		
		Tessellator.instance = NatriumMod.customTessellator;
		
		NatriumMod.terrainRenderer.init(mc);
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

}

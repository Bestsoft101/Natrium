package b100.natrium.asm;

import b100.natrium.NatriumMod;
import b100.natrium.asm.utils.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.RenderGlobal;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.camera.ICamera;

public class Listeners {
	
	public static void onStartGame() {
		Tessellator.instance = NatriumMod.customTessellator;
		
		NatriumMod.terrainRenderer.init(Minecraft.getMinecraft(Minecraft.class));
	}
	
	public static int onSortAndRender(RenderGlobal renderGlobal, ICamera activeCamera, int renderPass, double partialTicks) {
		if(renderPass == 1) {
			return 1;
		}
		NatriumMod.terrainRenderer.renderTerrain(activeCamera, (float) partialTicks);
		return 0;
	}
	
	public static void onCallAllDisplayLists(RenderGlobal renderGlobal, int renderPass, double partialTicks) {
		NatriumMod.terrainRenderer.renderTranslucentTerrain();
	}
	
	public static void onTessellatorConstructed(Tessellator tessellator, int size, CallbackInfo ci) {
		if(size <= 0) {
			ci.setCancelled(true);
		}
	}

}

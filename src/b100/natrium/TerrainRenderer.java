package b100.natrium;

import static org.lwjgl.opengl.GL11.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.core.util.helper.MathHelper;

public class TerrainRenderer {
	
	public Minecraft mc;
	
	public VBOPool vboPool;
	
	public MultiDrawRenderList[] renderLists = new MultiDrawRenderList[2]; // 2 Render Passes
	
	public double renderPosX;
	public double renderPosY;
	public double renderPosZ;
	
	public double prevSortX;
	public double prevSortY;
	public double prevSortZ;
	
	public void init(Minecraft minecraft) {
		this.mc = minecraft;
		
		vboPool = new VBOPool(1073741824);
		renderLists[0] = new MultiDrawRenderList(vboPool);
		renderLists[1] = new MultiDrawRenderList(vboPool);
	}
	
	public void renderTerrain(ICamera camera, float partialTicks) {
		if(mc.gameSettings.renderDistance.value != mc.renderGlobal.renderDistance) {
			// Reload chunks if render distance has changed
			mc.renderGlobal.loadRenderers();
		}
		
		renderPosX = camera.getX(partialTicks);
		renderPosY = camera.getY(partialTicks);
		renderPosZ = camera.getZ(partialTicks);
		
		double dX = renderPosX - prevSortX;
		double dY = renderPosY - prevSortY;
		double dZ = renderPosZ - prevSortZ;
		
		if(dX * dX + dY * dY + dZ * dZ > 16.0) {
			prevSortX = renderPosX;
			prevSortY = renderPosY;
			prevSortZ = renderPosZ;
			
			int blockX = MathHelper.floor_double(renderPosX);
			int blockY = MathHelper.floor_double(renderPosY);
			int blockZ = MathHelper.floor_double(renderPosZ);
			
			mc.renderGlobal.markRenderersForNewPosition(blockX, blockY, blockZ);
		}
		
		glPushMatrix();
		glTranslated(-renderPosX, -renderPosY, -renderPosZ);
		
		renderLists[0].draw();
		
		glPopMatrix();
	}
	
	public void renderTranslucentTerrain() {
		if(renderLists[1].visibleEntries.size() == 0) {
			return;
		}

		glPushMatrix();
		glTranslated(-renderPosX, -renderPosY, -renderPosZ);
		
		boolean fancyGraphics = mc.gameSettings.fancyGraphics.value != 0;
		if(fancyGraphics) {
			glColorMask(false, false, false, false);
			renderLists[1].draw();
			glColorMask(true, true, true, true);
			renderLists[1].draw();
		}else {
			renderLists[1].draw();
		}
		
		
		glPopMatrix();
	}
}

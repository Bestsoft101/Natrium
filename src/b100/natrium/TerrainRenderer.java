package b100.natrium;

import static org.lwjgl.opengl.GL11.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.core.util.helper.MathHelper;

public class TerrainRenderer {
	
	public Minecraft mc;
	
	public VBOPool vboPool;
	
	public MultiDrawRenderList[] renderLists = new MultiDrawRenderList[NatriumMod.renderListCount];
	
	public double renderPosX;
	public double renderPosY;
	public double renderPosZ;
	
	public double prevSortX;
	public double prevSortY;
	public double prevSortZ;
	
	public long renderOffsetX;
	public long renderOffsetZ;
	
	private boolean dontSetOffset = false;
	
	public boolean updateRenderOffsetNext = false;
	
	public void init(Minecraft minecraft) {
		this.mc = minecraft;
		
		vboPool = new VBOPool(1073741824);
		for(int i=0; i < renderLists.length; i++) {
			renderLists[i] = new MultiDrawRenderList(vboPool);	
		}
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
		
		double dX1 = Math.abs(renderPosX - renderOffsetX);
		double dZ1 = Math.abs(renderPosZ - renderOffsetZ);
		
		if(Math.max(dX1, dZ1) > 4096 || updateRenderOffsetNext) {
			updateRenderOffsetNext = false;
			
			setRenderOffset(renderPosX, renderPosZ);

			double dX2 = Math.abs(renderPosX - renderOffsetX);
			double dZ2 = Math.abs(renderPosZ - renderOffsetZ);
			
			if(Math.max(dX2, dZ2) > 4096) {
				throw new RuntimeException("Render offset still too big after setting!");
			}
			
			try {
				dontSetOffset = true;
				mc.renderGlobal.loadRenderers();
			}finally {
				dontSetOffset = false;
			}
		}
		
		glPushMatrix();
		glTranslated(-renderPosX, -renderPosY, -renderPosZ);
		glTranslated(renderOffsetX, 0.0, renderOffsetZ);
		
		renderLists[NatriumMod.renderListRenderOffset].draw();
		
		glPopMatrix();
	}
	
	public void onReloadChunks() {
		if(dontSetOffset) {
			return;
		}
		ICamera camera = mc.activeCamera;
		if(camera != null) {
			setRenderOffset(camera.getX(), camera.getZ());	
		}
	}
	
	public void setRenderOffset(double x, double z) {
		long newX = (((long) x) >> 8) << 8;
		long newZ = (((long) z) >> 8) << 8;
		if(newX != renderOffsetX || newZ != renderOffsetZ) {
			renderOffsetX = newX;
			renderOffsetZ = newZ;
			NatriumMod.log("Set render offset: " + renderOffsetX + ", " + renderOffsetZ);
		}
	}
	
	public void renderTranslucentTerrain() {
		if(renderLists[1].visibleEntries.size() == 0) {
			return;
		}

		glPushMatrix();
		glTranslated(-renderPosX, -renderPosY, -renderPosZ);
		glTranslated(renderOffsetX, 0.0, renderOffsetZ);
		
		mc.worldRenderer.lightmapHelper.enableLightmapRendering();
		
		boolean fancyGraphics = mc.gameSettings.fancyGraphics.value != 0;
		if(fancyGraphics) {
			glColorMask(false, false, false, false);
			renderLists[NatriumMod.renderListRenderOffset + 1].draw();
			glColorMask(true, true, true, true);
			renderLists[NatriumMod.renderListRenderOffset + 1].draw();
		}else {
			renderLists[NatriumMod.renderListRenderOffset + 1].draw();
		}
		
		mc.worldRenderer.lightmapHelper.disableLightmapRendering();
		
		glPopMatrix();
	}
}

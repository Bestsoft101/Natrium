package b100.natrium;

import static org.lwjgl.opengl.GL11.*;

import java.util.List;

import org.lwjgl.opengl.GL11;

import b100.natrium.VertexBuffer.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.render.camera.ICamera;
import net.minecraft.client.render.terrain.ChunkRenderer;
import net.minecraft.client.render.terrain.TerrainRenderer;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.util.helper.MathHelper;
import net.minecraft.core.world.World;

public class TerrainRendererMultiDrawNatrium extends TerrainRenderer {

	public final int VERTEX_BUFFER_INITIAL_CAPACITY = 67108864;
	
	public final int RENDER_LIST_INITIAL_CAPACITY = 2048;
	
	/**
	 * Exponential Region Size
	 * Lower Region Size: More Draw Calls, reduced FPS
	 * Higher Region Size: More Visual Artifacts
	 */
	public final int REGION_SIZE = 9;
	
	public final int MAX_RENDER_PASSES = 2;
	
	/**
	 * For debugging purposes
	 */
	public final boolean DISABLE_REGION_OFFSET = false;
	
	public VertexBuffer vertexBuffer;
	public RegionBuffer regionBuffer;
	
	private VertexConfig terrainVertexConfig;
	
	int[] drawCalls = new int[MAX_RENDER_PASSES];
	
	private double renderPosX;
	private double renderPosY;
	private double renderPosZ;

	private double prevSortX;
	private double prevSortY;
	private double prevSortZ;
	
	public TerrainRendererMultiDrawNatrium(Minecraft minecraft) {
		super(minecraft);
		
		vertexBuffer = new VertexBuffer(VERTEX_BUFFER_INITIAL_CAPACITY);
		regionBuffer = new RegionBuffer(REGION_SIZE);
	}
	
	@Override
	public void renderSolidTerrain(float partialTicks) {
		for(int i=0; i < drawCalls.length; i++) {
			drawCalls[i] = 0;
		}

		ICamera camera = mc.activeCamera;
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
			
			int x = MathHelper.floor(renderPosX);
			int y = MathHelper.floor(renderPosY);
			int z = MathHelper.floor(renderPosZ);
			
			mc.renderGlobal.markRenderersForNewPosition(x, y, z);
		}
		
		if(terrainVertexConfig == null) {
			return;
		}
		
		glPushMatrix();
		glTranslated(-renderPosX, -renderPosY, -renderPosZ);
		
		terrainVertexConfig.enableVBO(vertexBuffer.getVbo());
		
		List<RenderRegion> regions = regionBuffer.getAllRegions();
		for(int i=0; i < regions.size(); i++) {
			RenderRegion region = regions.get(i);
			// TODO Clear empty regions, but not like this
//			if(!region.hasRenderData()) {
//				regionBuffer.remove(region);
//				i--;
//				continue;
//			}

			RenderList renderList = region.renderLists[0];
			if(renderList != null && renderList.size() > 0) {
				if(!DISABLE_REGION_OFFSET) {
					glPushMatrix();
					glTranslated((region.posX << REGION_SIZE), 0.0, (region.posZ << REGION_SIZE));
				}
				
				renderList.draw();
				drawCalls[0]++;
				
				if(!DISABLE_REGION_OFFSET) {
					glPopMatrix();
				}
			}
		}
		
		terrainVertexConfig.disable();
		
		glPopMatrix();
		
	}
	
	public void renderAllLists(int renderPass) {
		List<RenderRegion> regions = regionBuffer.getAllRegions();
		for(int i=0; i < regions.size(); i++) {
			RenderRegion region = regions.get(i);
			RenderList renderList = region.renderLists[renderPass];
			if(renderList != null && renderList.size() > 0) {
				if(!DISABLE_REGION_OFFSET) {
					glPushMatrix();
					glTranslated((region.posX << REGION_SIZE), 0.0, (region.posZ << REGION_SIZE));
				}
				
				renderList.draw();
				drawCalls[renderPass]++;
				
				if(!DISABLE_REGION_OFFSET) {
					glPopMatrix();
				}
			}
		}
	}

	@Override
	public void renderTranslucentTerrain(float partialTicks) {
		if(terrainVertexConfig == null) {
			return;
		}
		
		glPushMatrix();
		glTranslated(-renderPosX, -renderPosY, -renderPosZ);
		
		terrainVertexConfig.enableVBO(vertexBuffer.getVbo());
		
		boolean fancy = mc.gameSettings.fancyGraphics.value != 0;
		
		if(fancy) {
			GL11.glColorMask(false, false, false, false);
		}
		
		renderAllLists(1);
		
		if(fancy) {
			GL11.glColorMask(true, true, true, true);
			if(drawCalls[1] > 0) {
				renderAllLists(1);
			}
		}
		
		terrainVertexConfig.disable();
		
		glPopMatrix();
	}
	
	public void chunkRendered(ChunkRendererMultiDraw chunkRenderer, TessellatorNatrium tessellator, final int renderPass) {
		tessellator.drawing = false;
		VertexConfig config = tessellator.data.config;
		
		if(terrainVertexConfig == null) {
			this.terrainVertexConfig = config.copy();
		}else {
			if(!this.terrainVertexConfig.equals(tessellator.data.config)) {
				List<RenderRegion> allRegions = regionBuffer.getAllRegions();
				for(int i=0; i < allRegions.size(); i++) {
					RenderRegion region = allRegions.get(i);
					if(region.hasRenderData()) {
						VertexConfig.compareThrow(tessellator.data.config, terrainVertexConfig);
					}
					regionBuffer.remove(region);
					i--;
				}
				
				this.terrainVertexConfig = config.copy();
			}
		}
		
		RenderList renderList = getOrCreateRenderList(chunkRenderer, renderPass);
		Entry entry = renderList.add(tessellator.data, chunkRenderer.visible);
		chunkRenderer.renderListEntries[renderPass] = entry;
	}
	
	public void chunkVisibilityChanged(ChunkRendererMultiDraw chunkRenderer) {
		RenderRegion region = getRenderRegionAtBlockPos(chunkRenderer.posX, chunkRenderer.posZ);
		
		for(int renderPass=0; renderPass < MAX_RENDER_PASSES; renderPass++) {
			Entry entry = chunkRenderer.renderListEntries[renderPass];
			if(entry == null) {
				continue;
			}
			RenderList renderList = region.renderLists[renderPass];
			if(renderList == null) {
				throw new NullPointerException("Region " + region + " does not have a render list for render pass " + renderPass + "!");
			}
			renderList.setVisible(entry, chunkRenderer.visible);
		}
	}
	
	public void chunkDeleted(ChunkRendererMultiDraw chunkRenderer) {
		RenderRegion region = getRenderRegionAtBlockPos(chunkRenderer.posX, chunkRenderer.posZ);
		for(int renderPass=0; renderPass < MAX_RENDER_PASSES; renderPass++) {
			Entry entry = chunkRenderer.renderListEntries[renderPass];
			if(entry == null) {
				continue;
			}
			RenderList renderList = region.renderLists[renderPass];
			if(renderList == null) {
				throw new NullPointerException("Region " + region + " does not have a render list for render pass " + renderPass + "!");
			}
			renderList.remove(entry);
			chunkRenderer.renderListEntries[renderPass] = null;
		}
	}
	
	public RenderList getOrCreateRenderList(ChunkRendererMultiDraw chunkRenderer, final int renderPass) {
		return getOrCreateRenderList(chunkRenderer.posX, chunkRenderer.posZ, renderPass);
	}
	
	public RenderList getOrCreateRenderList(int posX, int posZ, final int renderPass) {
		RenderRegion region = regionBuffer.getRegionAtBlockPos(posX, posZ);
		RenderList renderList = region.renderLists[renderPass];
		if(renderList == null) {
			renderList = new RenderList(vertexBuffer, RENDER_LIST_INITIAL_CAPACITY);
			region.renderLists[renderPass] = renderList;
		}
		return renderList;
	}

	@Override
	public ChunkRenderer createChunkRenderer(World world, List<TileEntity> globalRenderableTileEntities, int x, int y, int z, int size, int lists) {
		return new ChunkRendererMultiDraw(this, world, globalRenderableTileEntities, x, y, z, size, lists);
	}
	
	public RenderRegion getRenderRegionAtBlockPos(int blockX, int blockZ) {
		return regionBuffer.getRegionAtBlockPos(blockX, blockZ);
	}
	
	public int getTotalDrawCalls() {
		int totalDrawCalls = 0;
		for(int i=0; i < drawCalls.length; i++) {
			totalDrawCalls += drawCalls[i];
		}
		return totalDrawCalls;
	}
	
}

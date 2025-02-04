package b100.natrium;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import b100.natrium.VertexBuffer.Entry;
import net.minecraft.client.render.LightmapHelper;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.TileEntityRenderDispatcher;
import net.minecraft.client.render.block.model.BlockModel;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.culling.CameraFrustum;
import net.minecraft.client.render.terrain.ChunkRenderer;
import net.minecraft.client.render.tessellator.Tessellator;
import net.minecraft.core.block.Blocks;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.chunk.ChunkCache;

public class ChunkRendererMultiDraw extends ChunkRenderer {

	public final TerrainRendererMultiDrawNatrium terrainRenderer;
	public final Entry[] renderListEntries;
	
	public ChunkRendererMultiDraw(TerrainRendererMultiDrawNatrium terrainRenderer, World world, List<TileEntity> globalRenderableTileEntities, int x, int y, int z, int size, int lists) {
		super(world, globalRenderableTileEntities, x, y, z, size, lists);
		
		if(terrainRenderer == null) {
			throw new NullPointerException("TerrainRenderer is null!");
		}
		
		this.terrainRenderer = terrainRenderer;
		this.renderListEntries = new Entry[terrainRenderer.MAX_RENDER_PASSES];
	}
	
	@Override
	public void rebuild() {
		if(!dirty) {
			return;
		}
		
		updates++;
		
		final int minX = posX;
		final int minY = posY;
		final int minZ = posZ;
		
		final int maxX = posX + sizeX;
		final int maxY = posY + sizeY;
		final int maxZ = posZ + sizeZ;
		
		final int renderPassCount = terrainRenderer.MAX_RENDER_PASSES;
		
		terrainRenderer.chunkDeleted(this);
		
		Chunk.isLit = false;
		final Set<TileEntity> lastSpecialTileEntities = new HashSet<>(renderableBlockEntities);
		renderableBlockEntities.clear();
		
		final int cacheRadius = 1;
		final ChunkCache chunkCache = new ChunkCache(world, minX - cacheRadius, minY - cacheRadius, minZ - cacheRadius, maxX + cacheRadius, maxY + cacheRadius, maxZ + cacheRadius);
		final RenderBlocks renderBlocks = new RenderBlocks(chunkCache);
		BlockModel.setRenderBlocks(renderBlocks);
		
		TessellatorNatrium tessellator = (TessellatorNatrium) Tessellator.instance;
		for(int renderPass = 0; renderPass < renderPassCount; renderPass++) {
			boolean needsMoreRenderPasses = false;
			boolean hasRenderedBlock = false;
			boolean hasStartedDrawing = false;

			for(int y = minY; y < maxY; y++) {
				for(int z = minZ; z < maxZ; z++) {
					for(int x = minX; x < maxX; x++) {
						final int blockId = chunkCache.getBlockId(x, y, z);
						if(blockId <= 0) {
							continue;
						}
						if(!hasStartedDrawing) {
							hasStartedDrawing = true;
							tessellator.startDrawingQuads();
//							tessellator.setTranslation(-this.posX, -this.posY, -this.posZ);
							
							RenderRegion region = terrainRenderer.getRenderRegionAtBlockPos(posX, posZ);
							tessellator.setTranslation(-(region.posX << terrainRenderer.REGION_SIZE), 0.0, -(region.posZ << terrainRenderer.REGION_SIZE));
							
							if(LightmapHelper.isLightmapEnabled()) {
								// Make sure it's active
								tessellator.setLightmapCoord(LightmapHelper.getLightmapCoord(15, 15));
							}
							tessellator.setColorRGBA(255, 255, 255, 255);
							tessellator.setTextureUV(0.0, 0.0);
						}
						if(renderPass == 0 && Blocks.isEntityTile[blockId]) {
							final TileEntity tileentity = chunkCache.getTileEntity(x, y, z);
							if(TileEntityRenderDispatcher.instance.hasRenderer(tileentity)) {
								renderableBlockEntities.add(tileentity);
							}
						}
						final BlockModel<?> model = BlockModelDispatcher.getInstance().getDispatch(Blocks.blocksList[blockId]);
						final int blockRenderPass = model.renderLayer();
						
						if(blockRenderPass != renderPass) {
							needsMoreRenderPasses = true;
							continue;
						}
						
						hasRenderedBlock |= renderBlock(tessellator, renderBlocks, model, x, y, z);
					}
				}
			}
			if(hasStartedDrawing) {
				terrainRenderer.chunkRendered(this, tessellator, renderPass);
				tessellator.setTranslation(0.0, 0.0, 0.0);
			}else {
				hasRenderedBlock = false;
			}

			if(hasRenderedBlock) {
				empty[renderPass] = false;
			}
			if(!needsMoreRenderPasses) {
				break;
			}
		}
		
		HashSet<TileEntity> newSpecialTileEntities = new HashSet<>(renderableBlockEntities);
		newSpecialTileEntities.removeAll(lastSpecialTileEntities);
		globalRenderableBlockEntities.addAll(newSpecialTileEntities);
		renderableBlockEntities.forEach(lastSpecialTileEntities::remove);
		globalRenderableBlockEntities.removeAll(lastSpecialTileEntities);
		skyLit = Chunk.isLit;
		compiled = true;
	}
	
	@Override
	public void cull(CameraFrustum frustum, float partialTick) {
		boolean wasVisible = this.visible;
		super.cull(frustum, partialTick);
		if(wasVisible != visible) {
			terrainRenderer.chunkVisibilityChanged(this);
		}
	}
	
	@Override
	public void reset() {
		// TerrainRenderer is null during initialization
		if(terrainRenderer != null) {
			terrainRenderer.chunkDeleted(this);	
		}
		super.reset();
	}
	
	@Override
	public String toString() {
		return "[" + posX + "," + posY + "," + posZ + "]";
	}
}

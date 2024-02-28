package b100.natrium;

import java.util.HashSet;
import java.util.List;

import net.minecraft.client.render.ChunkRenderer;
import net.minecraft.client.render.RenderBlocks;
import net.minecraft.client.render.RenderEngine;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.TileEntityRenderDispatcher;
import net.minecraft.client.render.block.model.BlockModel;
import net.minecraft.client.render.block.model.BlockModelDispatcher;
import net.minecraft.client.render.block.model.BlockModelRenderBlocks;
import net.minecraft.client.render.culling.CameraFrustum;
import net.minecraft.core.block.Block;
import net.minecraft.core.block.entity.TileEntity;
import net.minecraft.core.world.World;
import net.minecraft.core.world.chunk.Chunk;
import net.minecraft.core.world.chunk.ChunkCache;

public class ChunkRendererMultiDraw extends ChunkRenderer {
	
	public VBOPool.Entry[] renderListEntries = new VBOPool.Entry[MAX_RENDER_PASSES];

	public ChunkRendererMultiDraw(RenderEngine renderEngine, World world, List<TileEntity> list, int posX, int posY, int posZ, int size, int renderList) {
		super(renderEngine, world, list, posX, posY, posZ, size, renderList);
	}

	public void updateRenderer() {
		if(!needsUpdate) {
			return;
		}
		
//		Tessellator originalTessellator = Tessellator.instance;
//		Tessellator.instance = tessellator = NatriumMod.customTessellator;
		
		chunksUpdated++;
		int minX = posX;
		int minY = posY;
		int minZ = posZ;
		int maxX = posX + sizeWidth;
		int maxY = posY + sizeHeight;
		int maxZ = posZ + sizeDepth;
		
		for(int renderPass=0; renderPass < renderListEntries.length; renderPass++) {
			VBOPool.Entry entry = renderListEntries[renderPass];
			if(entry != null) {
				boolean removed = NatriumMod.terrainRenderer.renderLists[renderPass].remove(entry);
				if(!removed) {
					throw new RuntimeException("Not removed!");
				}
				renderListEntries[renderPass] = null;
			}
		}
		
		Chunk.isLit = false;
		HashSet<TileEntity> lastSpecialTileEntities = new HashSet<>(specialTileEntities);
		specialTileEntities.clear();
		int cacheRadius = 1;
		ChunkCache chunkcache = new ChunkCache(worldObj, minX - cacheRadius, minY - cacheRadius, minZ - cacheRadius, maxX + cacheRadius, maxY + cacheRadius, maxZ + cacheRadius);
		RenderBlocks renderblocks = new RenderBlocks(worldObj, chunkcache);
		BlockModelRenderBlocks.setRenderBlocks(renderblocks);
		int renderPass = 0;
		while(true) {
			if(renderPass >= MAX_RENDER_PASSES) {
				break;
			}
			boolean needsMoreRenderPasses = false;
			boolean hasRenderedBlock = false;
			boolean hasStartedDrawing = false;
			for(int y = minY; y < maxY; y++) {
				for(int z = minZ; z < maxZ; z++) {
					for(int x = minX; x < maxX; x++) {
						int blockId = chunkcache.getBlockId(x, y, z);
						if(blockId <= 0) {
							continue;
						}
						if(!hasStartedDrawing) {
							hasStartedDrawing = true;
//							GL11.glNewList(glRenderList + renderPass, GL11.GL_COMPILE);
//							GL11.glPushMatrix();
//							setupGLTranslation();
//							float scale = 1.000001F;
//							GL11.glTranslatef((float) (-sizeDepth) / 2.0F, (float) (-sizeHeight) / 2.0F, (float) (-sizeDepth) / 2.0F);
//							GL11.glScalef(scale, scale, scale);
//							GL11.glTranslatef((float) sizeDepth / 2.0F, (float) sizeHeight / 2.0F, (float) sizeDepth / 2.0F);
//							tessellator.startDrawingQuads();
//							tessellator.setTranslation(-posX, -posY, -posZ);
							tessellator.startDrawingQuads();
							tessellator.setColorRGBA(255, 255, 255, 255);
						}
						if(renderPass == 0 && Block.isEntityTile[blockId]) {
							TileEntity tileentity = chunkcache.getBlockTileEntity(x, y, z);
							if(TileEntityRenderDispatcher.instance.hasRenderer(tileentity)) {
								specialTileEntities.add(tileentity);
							}
						}
						Block block = Block.blocksList[blockId];
						int blockRenderPass = block.getRenderBlockPass();
						if(blockRenderPass != renderPass) {
							needsMoreRenderPasses = true;
						}else {
							BlockModel model = BlockModelDispatcher.getInstance().getDispatch(block);
							hasRenderedBlock |= model.render(block, x, y, z);
							if(block.hasOverbright) {
								renderblocks.overbright = true;
								hasRenderedBlock |= model.render(block, x, y, z);
								renderblocks.overbright = false;
							}
						}
					}

				}

			}
			if(hasStartedDrawing) {
				tessellator.isDrawing = false;
				
				MultiDrawRenderList renderList = NatriumMod.terrainRenderer.renderLists[renderPass];
				
				if(renderList == null) {
					throw new NullPointerException("RenderList for RenderPass " + renderPass + " is null!");
				}
				
				VBOPool.Entry entry = renderList.add(NatriumMod.customTessellator, this.isInFrustum);
				
				if(entry != null) {
					this.renderListEntries[renderPass] = entry;	
				}
				
//				tessellator.draw();
//				GL11.glPopMatrix();
//				GL11.glEndList();
				tessellator.setTranslation(0.0D, 0.0D, 0.0D);
			}else {
				hasRenderedBlock = false;
			}

			if(hasRenderedBlock) {
				skipRenderPass[renderPass] = false;
			}
			if(!needsMoreRenderPasses) {
				break;
			}

			renderPass++;
		}
		HashSet<TileEntity> newSpecialTileEntities = new HashSet<>(specialTileEntities);
		newSpecialTileEntities.removeAll(lastSpecialTileEntities);
		tileEntities.addAll(newSpecialTileEntities);
		specialTileEntities.forEach(lastSpecialTileEntities::remove);
		tileEntities.removeAll(lastSpecialTileEntities);
		isChunkLit = Chunk.isLit;
		isInitialized = true;
		
//		Tessellator.instance = tessellator = originalTessellator;
	}
	
	@Override
	public void reset() {
		super.reset();

		for(int renderPass=0; renderPass < renderListEntries.length; renderPass++) {
			VBOPool.Entry entry = renderListEntries[renderPass];
			if(entry != null) {
				boolean removed = NatriumMod.terrainRenderer.renderLists[renderPass].remove(entry);
				if(!removed) {
					throw new RuntimeException("Not removed!");
				}
				renderListEntries[renderPass] = null;
			}
		}
	}
	
	@Override
	public void updateInFrustum(CameraFrustum frustum, float partialTick) {
		boolean wasInFrustum = this.isInFrustum;
		super.updateInFrustum(frustum, partialTick);
		if(isInFrustum != wasInFrustum) {
			for(int i=0; i < renderListEntries.length; i++) {
				VBOPool.Entry entry = renderListEntries[i];
				if(entry != null) {
					NatriumMod.terrainRenderer.renderLists[i].setVisible(entry, isInFrustum);
				}
			}
		}
	}

}

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ForgeHooksClient;

public class ChunkRendererMultiDraw extends baj {
	
	public static ChunkRendererMultiDraw currentRenderer = null;
	
	public TerrainRenderer terrainRenderer = NatriumMod.terrainRenderer;
	
	// Chunks can have meshes for multiple textures
	// Every texture can have 2 render passes
	public Map<Integer, VBOPool.Entry[]> allMeshes = new HashMap<>(); // VBOPool.Entry[2];
	
	private static Map<Integer, CustomTessellator> textureIdToTessellator = new HashMap<>();
	private static List<Integer> texturesBeingUsed = new ArrayList<>();
	
	private CustomTessellator currentTessellator = null;
	private int lastBlockTexture = -1;
	
	private boolean renderedSomething = false;
	private boolean renderOtherPass = false;
	
	public ChunkRendererMultiDraw(yc world, List list, int par3, int par4, int par5, int par6) {
		super(world, list, par3, par4, par5, par6);
	}
	
	@Override
	public void a() {
		try {
			CustomTessellator.CRASH_WHEN_DISABLED = true;
			CustomTessellator.ENABLE_ALL = true;
			CustomTessellator.DONT_DRAW = true;
			
			currentRenderer = this;
			
			if(baz.a == null) {
				throw new NullPointerException("Tessellator is null!");
			}
			
			updateChunkMultiDraw();
			
			if(baz.a == null) {
				throw new NullPointerException("Tessellator is null after rendering chunk!");
			}
		}catch (Exception e) {
			int x0 = this.c;
			int y0 = this.d;
			int z0 = this.e;
			
			System.err.println("Error rendering chunk: ");
			System.err.println("    at x: " + x0 + ", y: " + y0 + ", z: " + z0);
			throw new RuntimeException("Rendering chunk", e);
		}finally {
			CustomTessellator.CRASH_WHEN_DISABLED = false;
			CustomTessellator.ENABLE_ALL = false;
			CustomTessellator.DONT_DRAW = false;
			
			currentRenderer = null;
		}
	}

	public void updateChunkMultiDraw() {
		if(this.a == null || !this.q) {
			return;
		}
		
		if(texturesBeingUsed.size() > 0) {
			throw new RuntimeException("Used texture list not cleared!");
		}
		
		final CustomTessellator originalTessellator = (CustomTessellator) baz.a;
		
		this.t = true;
		this.isVisibleFromPosition = false;
		this.q = false;
		int x0 = this.c;
		int y0 = this.d;
		int z0 = this.e;
		int x1 = this.c + 16;
		int y1 = this.d + 16;
		int z1 = this.e + 16;
		
		terrainRenderer.deleteChunk(this);
		
		for(int var7 = 0; var7 < 2; ++var7) {
			this.m[var7] = true;
		}
		
		if(Reflector.LightCache.exists()) {
			Object var26 = Reflector.getFieldValue(Reflector.LightCache_cache);
			Reflector.callVoid(var26, Reflector.LightCache_clear, new Object[0]);
			Reflector.callVoid(Reflector.BlockCoord_resetPool, new Object[0]);
		}

		zz.a = false;
		
		Set oldTileEntities = new HashSet<>();
		oldTileEntities.addAll(this.x);
		
		this.x.clear();
		byte var8 = 1;
		ys chunkCache = new ys(this.a, x0 - var8, y0 - var8, z0 - var8, x1 + var8, y1 + var8, z1 + var8);
		if(!chunkCache.Q()) {
			++b;
			bbb renderBlocks = new bbb(chunkCache);
			this.C = 0;
			
			for(int renderPass = 0; renderPass < 2; ++renderPass) {
				renderOtherPass = false;
				renderedSomething = false;
				
				for(int y = y0; y < y1; y++) {
					for(int z = z0; z < z1; z++) {
						for(int x = x0; x < x1; x++) {
							try {
								if(baz.a == null) {
									throw new NullPointerException("Tessellator is null before rendering block!");
								}
								renderBlock(chunkCache, renderBlocks, x, y, z, renderPass);
								if(baz.a == null) {
									throw new NullPointerException("Tessellator is null after rendering block!");
								}
							}catch (Exception e) {
								Minecraft mc = NatriumMod.getMinecraft();
								yc world = mc.e;
								int blockId = world.a(x, y, z);
								amq block = amq.p[blockId];
								String name = null;
								if(block != null) {
									name = block.a();
								}
								System.err.println("Error while rendering block");
								System.err.println("    Position: " + x + ", " + y + ", " + z);
								System.err.println("    ID: " + blockId);
								System.err.println("    Block: " + block);
								if(name != null) {
									System.err.println("    Name: " + name);	
								}
								System.err.println("    Render Pass: " + renderPass);
								throw new RuntimeException("Rendering block", e);
							}
						}
					}
				}
				
				if(texturesBeingUsed.size() > 0) {
					CustomTessellator.DONT_DRAW = false;
					
					ForgeHooksClient.afterRenderPass(renderPass);
					
					for(int i=0; i < texturesBeingUsed.size(); i++) {
						Integer textureId = texturesBeingUsed.get(i);
						CustomTessellator tessellator = textureIdToTessellator.get(textureId);

						this.C += tessellator.getVertexCount();
						
						VBOPool.Entry[] meshesForTexture = allMeshes.get(textureId);
						if(meshesForTexture == null) {
							meshesForTexture = new VBOPool.Entry[2];
							allMeshes.put(textureId, meshesForTexture);
						}
						
						VBOPool.Entry entry = terrainRenderer.getRenderList(textureId, renderPass).add(tessellator, l);
						if(entry != null) {
							this.m[renderPass] = false;
						}
						meshesForTexture[renderPass] = entry;
						tessellator.setRenderingChunk(false);
						tessellator.b(0.0, 0.0, 0.0);
						tessellator.clearSubTessellators();
						if(baz.a == null) {
							throw new NullPointerException("Tessellator is null after finishing rendering of texture " + textureId);
						}
					}
					
					texturesBeingUsed.clear();
				}
				if(!renderOtherPass) {
					break;
				}
			}
		}

		Set currentTileEntities = new HashSet<>();
		currentTileEntities.addAll(this.x);
		currentTileEntities.removeAll(oldTileEntities);
		this.B.addAll(currentTileEntities);
		oldTileEntities.removeAll(this.x);
		this.B.removeAll(oldTileEntities);
		this.w = zz.a;
		this.A = true;
		
		baz.a = originalTessellator;
		originalTessellator.setDrawing(false);
	}
	
	public void renderBlock(ys chunkCache, bbb renderBlocks, int x, int y, int z, int renderPass) {
		int blockId = chunkCache.a(x, y, z);
		if(blockId <= 0) {
			return;
		}
		amq block = amq.p[blockId];
		if(block == null) {
			return;
		}
		
		if(!renderedSomething) {
			renderedSomething = true;
			ForgeHooksClient.beforeRenderPass(renderPass);
		}
		
		String texture = block.getTextureFile();
		bindTexture(texture);
		
		if(!baz.a.z) {
			throw new RuntimeException("Tessellator is not drawing after binding texture: " + texture);
		}
		
		if(renderPass == 0 && block.u()) {
			any tileEntity = chunkCache.q(x, y, z);
			if(bdw.a.a(tileEntity)) {
				this.x.add(tileEntity);
			}
		}

		int blockRenderPass = block.n();
		boolean renderInPass = true;
		if(blockRenderPass != renderPass) {
			renderOtherPass = true;
			renderInPass = false;
		}
		
		renderInPass = block.canRenderInPass(renderPass);
		if(renderInPass) {
			currentTessellator.setEntity(blockId, 0);
			renderBlocks.b(block, x, y, z);
			currentTessellator.setEntity(-1, 0);
		}
	}
	
	public void bindTexture(String texture) {
		bba renderEngine = NatriumMod.getMinecraft().o;
		int blockTextureId = renderEngine.b(texture);
		bindTexture(blockTextureId);
	}
	
	public void bindCTMTexture(int texture) {
		bindTexture(texture);
	}
	
	public CustomTessellator getSubTessellator(CustomTessellator prev, int texture) {
		if(prev == null) {
			throw new NullPointerException("Previous tessellator is null!");
		}
		
		CustomTessellator tessellator = getOrCreateTessellator(texture);
		startRenderingTerrain(tessellator, texture);
		tessellator.setParent(prev);
		return tessellator;
	}
	
	private void bindTexture(int texture) {
		final CustomTessellator previousTessellator = (CustomTessellator) baz.a;
		if(previousTessellator == null) {
			throw new NullPointerException("Previous tessellator is null!");
		}
		
		// change tessellator if texture changed
		if(currentTessellator == null || lastBlockTexture == -1 || texture != lastBlockTexture) {
			currentTessellator = getOrCreateTessellator(texture);
			lastBlockTexture = texture;
		}
		
		startRenderingTerrain(currentTessellator, texture);
		
		baz.a = currentTessellator;
	}
	
	private CustomTessellator getOrCreateTessellator(int texture) {
		CustomTessellator tessellator = textureIdToTessellator.get(texture);
		if(tessellator == null) {
			tessellator = new CustomTessellator(65536);
			textureIdToTessellator.put(texture, tessellator);
		}
		return tessellator;
	}
	
	private void startRenderingTerrain(CustomTessellator tessellator, int texture) {
		if(!tessellator.isDrawing()) {
			tessellator.setRenderingChunk(true);
			
			// startDrawingQuads
			tessellator.b();
			// setRenderOffset
			tessellator.b(-globalChunkOffsetX, 0.0, -globalChunkOffsetZ);
			// setNormal
			tessellator.b(0.0f, 1.0f, 0.0f);
			// setLightmapCoord
			tessellator.c(0);
			// setColor
			tessellator.a(255, 255, 255, 255);
			
			texturesBeingUsed.add(texture);
		}
	}
	
	/**
	 * setDontDraw
	 */
	@Override
	public void b() {
		super.b();
//		
		if(allMeshes == null) {
			// can be null during initialization
			return;
		}
		
		terrainRenderer.deleteChunk(this);
	}
	
	/**
	 * updateVisibility
	 */
	@Override
	public void a(bbd par1iCamera) {
		boolean wasInFrustum = this.l;
		super.a(par1iCamera);
		boolean isInFrustum = this.l;
		if(isInFrustum != wasInFrustum) {
			terrainRenderer.visibilityChanged(this, isInFrustum);
		}
	}
}

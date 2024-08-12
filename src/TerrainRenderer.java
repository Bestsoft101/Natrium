import static org.lwjgl.opengl.GL11.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;

public class TerrainRenderer {
	
	public Minecraft mc;
	
	public VBOPool vboPool;
	
	private Map<Integer, MultiDrawRenderList[]> textureToRenderList = new HashMap<>();
	private final List<Integer> allTextures = new ArrayList<>();
	private List<Integer> allTexturesImmutable = Collections.unmodifiableList(allTextures);
	private Map<Integer, TextureSpecificConfig> textureSpecificConfigs = null;
	
	public double renderPosX;
	public double renderPosY;
	public double renderPosZ;

	public double prevSortX;
	public double prevSortY;
	public double prevSortZ;
	
	public int drawCalls = 0;
	
	public TerrainRenderer() {
		mc = Minecraft.x();
		
		vboPool = new VBOPool(1073741824);
	}
	
	public void initTextureSpecificConfigs() {
		textureSpecificConfigs = new HashMap<>();
		
		bba renderEngine = mc.o;
		
		textureSpecificConfigs.put(renderEngine.b("/powercrystals/minefactoryreloaded/textures/terrain_0.png"), new MineFactoryGlassFix());
		textureSpecificConfigs.put(renderEngine.b("/eloraam/lighting/lighting1.png"), new RedPowerLampFix());
	}

	public void renderTerrain(/*EntityLiving*/ md viewEntity, float partialTicks) {
		drawCalls = 0;
		
		if(textureSpecificConfigs == null) {
			initTextureSpecificConfigs();
		}
		
		bav renderGlobal = mc.f;
		ban entityRenderer = mc.t;
		
		// posXYZ = t u v
		// lastTickPosXYZ = T U V 
		renderPosX = viewEntity.T + (viewEntity.t - viewEntity.T) * partialTicks;
		renderPosY = viewEntity.U + (viewEntity.u - viewEntity.U) * partialTicks;
		renderPosZ = viewEntity.V + (viewEntity.v - viewEntity.V) * partialTicks;
		
		double dX = renderPosX - prevSortX;
		double dY = renderPosY - prevSortY;
		double dZ = renderPosZ - prevSortZ;
		
		if(dX * dX + dY * dY + dZ * dZ > 16.0) {
			prevSortX = renderPosX;
			prevSortY = renderPosY;
			prevSortZ = renderPosZ;
			
			int blockX = NatriumUtils.floor_double(renderPosX);
			int blockY = NatriumUtils.floor_double(renderPosY);
			int blockZ = NatriumUtils.floor_double(renderPosZ);
			
			// markRenderersForNewPosition
			renderGlobal.c(blockX, blockY, blockZ);
		}
		
//		if(mc.gameSettings.ambientOcclusion) {
			glShadeModel(GL_SMOOTH);	
//		}
		
		// enableLightmap
		entityRenderer.b((double) partialTicks);
		
		glPushMatrix();
		glTranslated(-renderPosX, -renderPosY, -renderPosZ);
		
		for(int i=0; i < allTextures.size(); i++) {
			Integer textureId = allTextures.get(i);
			if(NatriumDebugRender.textureOverrideIndex != -1 && textureId != NatriumDebugRender.textureOverrideTexture) {
				continue;
			}
			MultiDrawRenderList[] renderLists = textureToRenderList.get(textureId);
			TextureSpecificConfig config = textureSpecificConfigs.get(textureId);
			glBindTexture(GL_TEXTURE_2D, textureId);
			if(renderLists[0].size() == 0) {
				continue;
			}
			
			if(config != null) {
				config.beforeRender(0);
			}
			
			renderLists[0].draw();
			drawCalls++;
			
			if(config != null) {
				config.afterRender(0);
			}
		}
		
		glPopMatrix();
		
		// disableLightmap
		entityRenderer.a((double) partialTicks);

		renderGlobal.N = 0;
		renderGlobal.K = 0;
		for(int i=0; i < allTextures.size(); i++) {
			Integer textureId = allTextures.get(i);
			MultiDrawRenderList[] renderLists = textureToRenderList.get(textureId);
			renderGlobal.N += renderLists[0].visibleEntries.size() + renderLists[1].visibleEntries.size();
			renderGlobal.K += renderLists[0].entries.size() + renderLists[1].entries.size();
		}
	}

	public void renderTranslucentTerrain(/*EntityLiving*/ md viewEntity, float partialTicks) {
//		if(mc.gameSettings.ambientOcclusion) {
			glShadeModel(GL_SMOOTH);	
//		}

		ban entityRenderer = mc.t;

		// enableLightmap
		entityRenderer.b((double) partialTicks);
		glPushMatrix();
		glTranslated(-renderPosX, -renderPosY, -renderPosZ);
		
		if(Config.isWaterFancy()) {
			glColorMask(false, false, false, false);
			for(int i=0; i < allTextures.size(); i++) {
				Integer textureId = allTextures.get(i);
				if(NatriumDebugRender.textureOverrideIndex != -1 && textureId != NatriumDebugRender.textureOverrideTexture) {
					continue;
				}
				TextureSpecificConfig config = textureSpecificConfigs.get(textureId);
				MultiDrawRenderList[] renderLists = textureToRenderList.get(textureId);
				if(renderLists[1].size() == 0) {
					continue;
				}
				
				glBindTexture(GL_TEXTURE_2D, textureId);
				
				if(config != null) {
					config.beforeRender(1);
				}
				
				renderLists[1].draw();
				drawCalls++;
				
				if(config != null) {
					config.afterRender(1);
				}
			}
			glColorMask(true, true, true, true);	
		}
		
		for(int i=0; i < allTextures.size(); i++) {
			Integer textureId = allTextures.get(i);
			if(NatriumDebugRender.textureOverrideIndex != -1 && textureId != NatriumDebugRender.textureOverrideTexture) {
				continue;
			}
			TextureSpecificConfig config = textureSpecificConfigs.get(textureId);
			MultiDrawRenderList[] renderLists = textureToRenderList.get(textureId);
			if(renderLists[1].size() == 0) {
				continue;
			}
			
			
			glBindTexture(GL_TEXTURE_2D, textureId);
			
			if(config != null) {
				config.beforeRender(1);
			}
			
			renderLists[1].draw();
			drawCalls++;
			
			if(config != null) {
				config.afterRender(1);
			}
		}
		
		glPopMatrix();
		// disableLightmap
		entityRenderer.a((double) partialTicks);
	}
	
	public void deleteChunk(ChunkRendererMultiDraw chunkRenderer) {
		for(Integer textureId : chunkRenderer.allMeshes.keySet()) {
			VBOPool.Entry[] meshesForTexture = chunkRenderer.allMeshes.get(textureId);
			
			for(int renderPass=0; renderPass < meshesForTexture.length; renderPass++) {
				VBOPool.Entry entry = meshesForTexture[renderPass];
				if(entry != null) {
					boolean removed = getRenderList(textureId, renderPass).remove(entry);
					if(!removed) {
						throw new RuntimeException("Not removed!");
					}
					meshesForTexture[renderPass] = null;
				}
			}	
		}
	}
	
	public void visibilityChanged(ChunkRendererMultiDraw chunkRenderer, boolean visible) {
		for(Integer textureId : chunkRenderer.allMeshes.keySet()) {
			VBOPool.Entry[] meshesForTexture = chunkRenderer.allMeshes.get(textureId);
			for(int renderPass=0; renderPass < meshesForTexture.length; renderPass++) {
				VBOPool.Entry entry = meshesForTexture[renderPass];
				if(entry != null) {
					getRenderList(textureId, renderPass).setVisible(entry, visible);
				}
			}
		}
	}
	
	public MultiDrawRenderList getRenderList(Integer texture, int renderPass) {
		MultiDrawRenderList[] renderLists = this.textureToRenderList.get(texture);
		
		if(renderLists == null) {
			renderLists = new MultiDrawRenderList[2];
			renderLists[0] = new MultiDrawRenderList(vboPool);
			renderLists[1] = new MultiDrawRenderList(vboPool);
			this.textureToRenderList.put(texture, renderLists);
			this.allTextures.add(texture);
		}
		
		return renderLists[renderPass];
	}
	
	public int textureCount() {
		return allTextures.size();
	}
	
	public List<Integer> getAllTextures() {
		return allTexturesImmutable;
	}
	
	class MineFactoryGlassFix implements TextureSpecificConfig {

		@Override
		public void beforeRender(int renderPass) {
			if(renderPass == 1) {
				glEnable(GL_CULL_FACE);
				glCullFace(GL_BACK);
			}
		}

		@Override
		public void afterRender(int renderPass) {
			if(renderPass == 1) {
				glDisable(GL_CULL_FACE);	
			}
		}
		
	}
	
	class RedPowerLampFix implements TextureSpecificConfig {

		@Override
		public void beforeRender(int renderPass) {
			if(renderPass == 1) {
				glBlendFunc(770, 1);
				glDepthMask(false);
				glBindTexture(GL_TEXTURE_2D, 0);
			}
		}

		@Override
		public void afterRender(int renderPass) {
			if(renderPass == 1) {
				glBlendFunc(770, 771);
				glDepthMask(true);
			}
			
		}
		
	}
	
}

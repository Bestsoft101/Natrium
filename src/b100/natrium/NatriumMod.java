package b100.natrium;

public class NatriumMod {
	
	public static CustomTessellator customTessellator;
	
	public static TerrainRenderer terrainRenderer;

	public static final int vanillaRenderPassCount = 2;
	public static int renderListCount = 2;
	public static int renderListToUpdateCount = 2;
	public static int renderListRenderOffset = 0;
	
	public static void log(String string) {
		System.out.println("[Natrium] " + string);
	}

}

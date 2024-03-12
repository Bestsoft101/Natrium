package b100.natrium;

public class NatriumMod {
	
	public static CustomTessellator customTessellator = new CustomTessellator();
	
	public static TerrainRenderer terrainRenderer = new TerrainRenderer();
	
	public static void log(String string) {
		System.out.println("[Natrium] " + string);
	}

}

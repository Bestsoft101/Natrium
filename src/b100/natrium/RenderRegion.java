package b100.natrium;

public class RenderRegion {
	
	public final int posX;
	public final int posZ;
	
	public RenderList[] renderLists = new RenderList[2];
	
	public RenderRegion(int posX, int posZ) {
		this.posX = posX;
		this.posZ = posZ;
	}
	
	public boolean hasRenderData() {
		for(int i=0; i < renderLists.length; i++) {
			RenderList renderList = renderLists[i];
			if(renderList != null && renderList.size() > 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "[" + posX + "," + posZ + "]";
	}
	
}
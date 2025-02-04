package b100.natrium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionBuffer {
	
	public final int regionSizeShift;
	
	private final Map<Integer, RenderRegion> regionMap = new HashMap<>();
	private final List<RenderRegion> regionList = new ArrayList<>();
	private final List<RenderRegion> immutableRegionList = Collections.unmodifiableList(regionList);
	
	public RegionBuffer(int regionSizeShift) {
		this.regionSizeShift = regionSizeShift;
	}
	
	public RenderRegion getRegionAtBlockPos(int blockX, int blockZ) {
		int x = blockX >> regionSizeShift;
		int z = blockZ >> regionSizeShift;
		return getRegion(x, z);
	}
	
	public RenderRegion getRegion(int x, int z) {
		int index = getIndex(x, z);
		
		RenderRegion region = regionMap.get(index);
		if(region == null) {
			region = new RenderRegion(x, z);
			regionMap.put(index, region);
			regionList.add(region);
		}else {
			if(region.posX != x || region.posZ != z) {
				throw new RuntimeException(region + " != " + new RenderRegion(x, z));
			}	
		}
		return region;
	}
	
	public void remove(RenderRegion region) {
		int index = getIndex(region.posX, region.posZ);
		
		RenderRegion existingRegion = regionMap.get(index);
		if(existingRegion != region) {
			throw new RuntimeException(region + " != " + existingRegion);
		}
		
		regionMap.remove(index);
		regionList.remove(region);
	}
	
	public int getIndex(int x, int z) {
		return ((x & 0xFFFF) << 16) | (z & 0xFFFF);
	}
	
	public List<RenderRegion> getAllRegions() {
		return immutableRegionList;
	}
	
	public int size() {
		return regionMap.size();
	}
	
}
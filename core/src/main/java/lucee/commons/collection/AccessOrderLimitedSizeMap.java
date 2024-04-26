package lucee.commons.collection;

import java.util.LinkedHashMap;
import java.util.Map;

public class AccessOrderLimitedSizeMap<K, V> extends LinkedHashMap<K, V> {
	private static final long serialVersionUID = -1255247883699115518L;
	private final int maxSize;

	public AccessOrderLimitedSizeMap(int maxSize) {
		this(maxSize, 16);
	}

	public AccessOrderLimitedSizeMap(int maxSize, int initalCapacity) {
		super(initalCapacity, 0.75f, true); // Initialize with access order based on last access (not insertion)
		this.maxSize = maxSize;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxSize; // Remove the eldest entry (least recently accessed) if size exceeds the maximum
	}
}
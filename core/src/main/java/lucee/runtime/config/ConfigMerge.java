package lucee.runtime.config;

import lucee.runtime.type.Collection;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.Array;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;

public class ConfigMerge {

	// A map to store array keys and their unique identifier keys for dynamic merging.
	private static final Map<String, String> MERGEABLE_ARRAYS = new HashMap<>();

	static {
		MERGEABLE_ARRAYS.put("extensions", "id");
		MERGEABLE_ARRAYS.put("resourceProviders", "scheme");
		MERGEABLE_ARRAYS.put("cacheClasses", "class");
		MERGEABLE_ARRAYS.put("scheduledTasks", "name");
		MERGEABLE_ARRAYS.put("dumpWriters", "name");
	}

	public static void merge(Collection a, Collection b) {
		Iterator<Entry<Key, Object>> it = b.entryIterator();
		while (it.hasNext()) {
			Entry<Key, Object> e = it.next();
			Object incomingValue = e.getValue();
			Key incomingKey = e.getKey();

			if (incomingValue instanceof Array) {
				String mergeKey = findCaseInsensitiveKey(MERGEABLE_ARRAYS, incomingKey.getString() );
				if (mergeKey != null) {
					mergeArrayByKey(a, (Array) incomingValue, mergeKey, MERGEABLE_ARRAYS.get(mergeKey));
				} else {
					Object existingValue = a.get(incomingKey, null);
					mergeCollection((Collection) existingValue, (Collection) incomingValue);
					//a.setEL(incomingKey, incomingValue);
				}
			}
			else if (incomingValue instanceof Collection) {
				Object existingValue = a.get(incomingKey, null);
				if (existingValue instanceof Collection) {
					mergeCollection((Collection) existingValue, (Collection) incomingValue);
				} else {
					a.setEL(incomingKey, incomingValue);
				}
			}
			else {
				a.setEL(incomingKey, incomingValue);
			}
		}
	}

	private static void mergeCollection(Collection a, Collection b) {
		Iterator<Entry<Key, Object>> it = b.entryIterator();
		while (it.hasNext()) {
			Entry<Key, Object> e = it.next();
			Object incomingValue = e.getValue();
			Key incomingKey = e.getKey();

			if (incomingValue instanceof Collection) {
				Object existingValue = a.get(incomingKey, null);
				if (existingValue instanceof Collection) {
					mergeCollection((Collection) existingValue, (Collection) incomingValue);
				} else {
					a.setEL(incomingKey, incomingValue);
				}
			} else {
				a.setEL(incomingKey, incomingValue);
			}
		}
	}

	private static String findCaseInsensitiveKey(Map<String, String> mergeArrays, String keyToFind) {
		for (String key : mergeArrays.keySet()) {
			if (key.equalsIgnoreCase(keyToFind)) {
				return key;
			}
		}
		return null;
	}

	private static void mergeArrayByKey(Collection src, Array incoming, String key, String uniqueIdKey) {
		Object existingObj = src.get(key, null);
		
		if (!(existingObj instanceof Array)) {
			src.setEL(key, incoming);
			return;
		}

		Array existing = (Array) existingObj;
		
		for (int j = 0; j < incoming.size(); j++) {
			Object incomingItem = incoming.get(j + 1, null);
			if (incomingItem instanceof Collection) {
				Collection incomingItemMap = (Collection) incomingItem;
				Object incomingId = incomingItemMap.get(uniqueIdKey, null);

				if (incomingId == null) {
					continue;
				}
				
				boolean found = false;
				for (int i = 0; i < existing.size(); i++) {
					Object existingItem = existing.get(i + 1, null); // Lucee arrays are 1-indexed
					if (existingItem instanceof Collection) {
						Collection existingItemMap = (Collection) existingItem;
						Object existingId = existingItemMap.get(uniqueIdKey, null);
						if (incomingId.equals(existingId)) {
							// Update the existing item using a Key object for the index
							existing.setEL(i + 1, incomingItemMap);
							found = true;
							break;
						}
					}
				}

				if (!found) {
					// Add the new item
					existing.appendEL(incomingItemMap);
				}
			}
		}
	}
}
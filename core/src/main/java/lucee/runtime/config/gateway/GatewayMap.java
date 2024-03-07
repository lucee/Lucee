package lucee.runtime.config.gateway;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import lucee.commons.digest.HashUtil;
import lucee.runtime.gateway.GatewayEntry;

public class GatewayMap extends LinkedHashMap<String, GatewayEntry> {
	private static final long serialVersionUID = -1357224833049360465L;
	private String id;

	public String getId() {
		if (id == null) id = createId();
		return id;
	}

	private String createId() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, GatewayEntry> e: entrySet()) {
			sb.append(e.getKey()).append('=').append(e.getValue().toString());
		}
		return HashUtil.create64BitHashAsString(sb.toString(), Character.MAX_RADIX);
	}
}

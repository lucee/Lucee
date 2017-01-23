package lucee.runtime.type.scope.storage;

import java.io.Serializable;

public class IKStorageScopeItem implements Serializable {

	private static final long serialVersionUID = -8187816208907138226L;

	private Object value;
	private long lastModifed;
	private boolean removed;

	public IKStorageScopeItem(Object value) {
		this.value=value;
		this.lastModifed=System.currentTimeMillis();
	}

	public Object getValue() {
		return value;
	}
	
	// needed for containsValue
	public boolean equals(Object o) {
		return value.equals(o);
	}
	
	public Object remove() {
		return remove(System.currentTimeMillis());
	}
	
	public Object remove(long lastMod) {
		this.lastModifed=lastMod;
		Object v = value;
		value=null;
		removed=true;
		return v;
	}

	public boolean removed() {
		return removed;
	}
	public long lastModified() {
		return lastModifed;
	}

}

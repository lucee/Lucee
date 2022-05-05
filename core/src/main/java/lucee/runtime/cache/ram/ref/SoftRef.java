package lucee.runtime.cache.ram.ref;

import java.lang.ref.SoftReference;

public class SoftRef<T> extends SoftReference<T> implements Ref<T> {

	public SoftRef(T referent) {
		super(referent);
	}
}

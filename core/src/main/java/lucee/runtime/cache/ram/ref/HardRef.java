package lucee.runtime.cache.ram.ref;

import lucee.print;

public class HardRef<T> implements Ref<T> {

	private T ref;

	public HardRef(T referent) {
		print.e("--- hard ----");
		this.ref = referent;
	}

	@Override
	public T get() {
		return ref;
	}

}

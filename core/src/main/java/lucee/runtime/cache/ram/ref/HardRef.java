package lucee.runtime.cache.ram.ref;

public class HardRef<T> implements Ref<T> {

	private T ref;

	public HardRef(T referent) {
		this.ref = referent;
	}

	@Override
	public T get() {
		return ref;
	}
}

package lucee.runtime.config;

import lucee.runtime.type.Struct;

/**
 * Lazy-loading wrapper for config field values.
 *
 * Replaces the nullable object double-checked locking pattern that caused
 * repeated JIT unstable_if deoptimisations on ConfigServerImpl getters.
 * Uses a volatile boolean init flag instead of a null sentinel — primitive
 * boolean checks are stable under JIT speculation; nullable references are not.
 *
 * LDEV-6362
 */
public class ConfigValue<T> {

	private volatile boolean init = true;
	private T value;
	private final Prop<T> meta;

	public ConfigValue(Prop<T> meta) {
		this.meta = meta;
		this.value = meta.defaultValue; // never null — eager default
	}

	public T get(ConfigServerImpl config, Struct root) {
		if (init) {
			synchronized (this) {
				if (init) {
					value = meta.get(config, root);
					init = false;
				}
			}
		}
		return value;
	}

	public void reset() {
		synchronized (this) {
			value = meta.defaultValue;
			init = true;
		}
	}

	/**
	 * @return whether the value has been loaded (mirrors the old "field != null" check)
	 */
	boolean isInitialized() {
		return !init;
	}

	/**
	 * @return the currently held value without triggering a load
	 */
	T peek() {
		return value;
	}
}

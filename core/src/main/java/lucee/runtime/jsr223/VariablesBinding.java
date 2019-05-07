package lucee.runtime.jsr223;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;

import lucee.runtime.type.scope.Variables;
import lucee.runtime.type.scope.VariablesImpl;

public class VariablesBinding implements Bindings {

	private VariablesImpl var;

	public VariablesBinding() {
		this.var = new VariablesImpl();
	}

	public Variables getVaraibles() {
		return var;
	}

	@Override
	public int size() {
		return var.size();
	}

	@Override
	public boolean isEmpty() {
		return var.isEmpty();
	}

	@Override
	public boolean containsValue(Object value) {
		return var.containsValue(value);
	}

	@Override
	public void clear() {
		var.clear();
	}

	@Override
	public Set<String> keySet() {
		return var.keySet();
	}

	@Override
	public Collection<Object> values() {
		return var.values();
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return var.entrySet();
	}

	@Override
	public Object put(String name, Object value) {
		return var.put(name, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> toMerge) {
		var.putAll(toMerge);
	}

	@Override
	public boolean containsKey(Object key) {
		return var.containsKey(key);
	}

	@Override
	public Object get(Object key) {
		return var.get(key);
	}

	@Override
	public Object remove(Object key) {
		return var.remove(key);
	}

}

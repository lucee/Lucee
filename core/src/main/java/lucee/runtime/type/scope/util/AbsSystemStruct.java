package lucee.runtime.type.scope.util;

import java.util.Iterator;

import lucee.runtime.exp.PageException;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.it.EntryIterator;
import lucee.runtime.type.it.KeyIterator;
import lucee.runtime.type.it.ValueIterator;
import lucee.runtime.type.util.StructSupport;

public abstract class AbsSystemStruct extends StructSupport {

	@Override
	public final Object remove(Key key) throws PageException {
		return removeEL(key);
	}

	@Override
	public final Collection duplicate(boolean deepCopy) {
		Struct sct = new StructImpl();
		StructImpl.copy(this, sct, deepCopy);
		return sct;
	}

	@Override
	public final Iterator<Key> keyIterator() {
		return new KeyIterator(keys());
	}

	@Override
	public final Iterator<Object> valueIterator() {
		return new ValueIterator(this, keys());
	}

	@Override
	public final Iterator<Entry<Key, Object>> entryIterator() {
		return new EntryIterator(this, keys());
	}
}

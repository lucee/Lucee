package lucee.runtime.type.it;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

public class CollectionAsEntryIterator implements Iterator<Entry> {

	private Iterator it;

	public CollectionAsEntryIterator(Collection coll) {
		it = coll.iterator();
	}

	public CollectionAsEntryIterator(Iterator<?> it) {
		this.it = it;
	}

	@Override
	public boolean hasNext() {
		return it.hasNext();
	}

	@Override
	public Entry next() {
		Object o = it.next();
		if (o instanceof Entry) return (Entry) o;
		return new _Entry(o);
	}

	private static class _Entry implements Entry {

		private Object value;

		public _Entry(Object value) {
			this.value = value;
		}

		@Override
		public Object getKey() {
			return value;
		}

		@Override
		public Object getValue() {
			return value;
		}

		@Override
		public Object setValue(Object value) {
			Object tmp = this.value;
			this.value = value;
			return tmp;
		}

	}
}

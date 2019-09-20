package lucee.runtime.type;

import java.util.ArrayList;
import java.util.Iterator;

import lucee.commons.lang.CFTypes;
import lucee.runtime.PageContext;
import lucee.runtime.dump.DumpData;
import lucee.runtime.dump.DumpProperties;
import lucee.runtime.dump.DumpTable;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;

public class ArrayTyped extends ArrayImpl {

	private static final long serialVersionUID = 2416933826309884176L;

	private final String strType;
	private final short type;

	public ArrayTyped(String type) {
		super();
		this.strType = type;
		this.type = CFTypes.toShort(type, false, (short) 0);
	}

	public ArrayTyped(String type, int initalCap) {
		super(initalCap);
		this.strType = type;
		this.type = CFTypes.toShort(type, false, (short) 0);
	}

	public ArrayTyped(String type, Object[] objects) {
		super(objects.length);
		this.strType = type;
		this.type = CFTypes.toShort(type, false, (short) 0);

		for (int i = 0; i < objects.length; i++) {
			appendEL(objects[i]);
		}
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp) {
		DumpTable dt = (DumpTable) super.toDumpData(pageContext, maxlevel, dp);
		dt.setTitle("Array (type:" + strType + ")");

		return dt;
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		return super.duplicate(new ArrayTyped(strType), deepCopy);
	}

	@Override
	public Object append(Object o) throws PageException {
		return super.append(checkType(o));
	}

	@Override
	public Object appendEL(Object o) {
		return super.appendEL(checkTypeEL(o));
	}

	@Override
	public boolean insert(int key, Object value) throws PageException {
		return super.insert(key, checkType(value));
	}

	@Override
	public Object prepend(Object o) throws PageException {
		return super.prepend(checkType(o));
	}

	@Override
	public Object setE(int key, Object value) throws PageException {
		return super.setE(key, checkType(value));
	}

	@Override
	public Object setEL(int key, Object value) {
		return super.setEL(key, checkTypeEL(value));
	}

	@Override
	public Object set(String key, Object value) throws PageException {
		return super.set(key, checkType(value));
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		return super.set(key, checkType(value));
	}

	@Override
	public Object setEL(String key, Object value) {
		return super.setEL(key, checkTypeEL(value));
	}

	@Override
	public Object setEL(Key key, Object value) {
		return super.setEL(key, checkTypeEL(value));
	}

	@Override
	public boolean add(Object o) {
		return super.add(checkTypeEL(o));
	}

	@Override
	public boolean addAll(int index, java.util.Collection c) {
		java.util.List list = new ArrayList();
		Iterator it = c.iterator();
		while (it.hasNext()) {
			list.add(checkTypeEL(it.next()));
		}
		return super.addAll(index, list);
	}

	private Object checkTypeEL(Object o) {
		try {
			return Caster.castTo(null, type, strType, o);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	private Object checkType(Object o) throws PageException {
		return Caster.castTo(null, type, strType, o);
	}
}

package lucee.runtime.functions.struct;

import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.type.Collection.Key;
import lucee.runtime.type.FunctionValue;
import lucee.runtime.type.FunctionValueImpl;
import lucee.runtime.type.KeyImpl;
import lucee.runtime.type.SpreadValue;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public final class LiteralSpreadOrderedStruct extends BIF {

	private static final long serialVersionUID = 7212350071443285190L;

	public static Struct call(PageContext pc, Object[] objArr) throws PageException {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		for (Object obj: objArr) {
			if (obj instanceof SpreadValue) {
				((SpreadValue) obj).addTo(sct);
			}
			else if (obj instanceof FunctionValue) {
				FunctionValueImpl fv = (FunctionValueImpl) obj;
				if (fv.getNames() == null) {
					sct.set(fv.getNameAsKey(), fv.getValue());
				}
				else {
					String[] arr = fv.getNames();
					Struct s = sct;
					for (int y = 0; y < arr.length - 1; y++) {
						Key key = KeyImpl.init(arr[y].trim());
						Object child = s.get(key, null);
						if (child instanceof Struct) s = (Struct) child;
						else {
							Struct tmp = new StructImpl();
							s.setEL(key, tmp);
							s = tmp;
						}
					}
					s.set(KeyImpl.init(arr[arr.length - 1]), fv.getValue());
				}
			}
			else {
				throw new ExpressionException("invalid argument for literal struct, only named arguments and the spread operator are allowed");
			}
		}
		return sct;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		return call(pc, (Object[]) args[0]);
	}
}

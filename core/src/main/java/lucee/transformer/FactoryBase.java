package lucee.transformer;

import lucee.runtime.op.Caster;
import lucee.transformer.expression.literal.Literal;

public abstract class FactoryBase extends Factory {

	@Override
	public Literal createLiteral(Object obj, Literal defaultValue) {
		if (obj instanceof Boolean) return createLitBoolean(((Boolean) obj).booleanValue());
		if (obj instanceof Number) {
			if (obj instanceof Float) return createLitFloat(((Float) obj).floatValue());
			else if (obj instanceof Integer) return createLitInteger(((Integer) obj).intValue());
			else if (obj instanceof Long) return createLitLong(((Long) obj).longValue());
			else return createLitDouble(((Number) obj).doubleValue());
		}
		String str = Caster.toString(obj, null);
		if (str != null) return createLitString(str);
		return defaultValue;
	}
}

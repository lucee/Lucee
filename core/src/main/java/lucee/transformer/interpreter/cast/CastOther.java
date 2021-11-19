package lucee.transformer.interpreter.cast;

import java.io.File;
import java.util.Locale;
import java.util.TimeZone;

import org.w3c.dom.Node;

import lucee.commons.lang.StringUtil;
import lucee.runtime.Component;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Array;
import lucee.runtime.type.Collection;
import lucee.runtime.type.Query;
import lucee.runtime.type.Struct;
import lucee.runtime.type.dt.DateTime;
import lucee.runtime.type.dt.TimeSpan;
import lucee.transformer.cast.Cast;
import lucee.transformer.expression.Expression;
import lucee.transformer.interpreter.InterpreterContext;
import lucee.transformer.interpreter.expression.ExpressionBase;

/**
 * cast an Expression to a Double
 */
public final class CastOther extends ExpressionBase implements Cast {
	// TODO support short type
	private ExpressionBase expr;
	private String type;
	private String lcType;

	private CastOther(Expression expr, String type, String lcType) {
		super(expr.getFactory(), expr.getStart(), expr.getEnd());
		this.expr = (ExpressionBase) expr;
		this.type = type;
		this.lcType = lcType;

	}

	public static Expression toExpression(Expression expr, String type) {
		if (type == null) return expr;

		String lcType = StringUtil.toLowerCase(type);
		switch (lcType.charAt(0)) {
		case 'a':
			if ("any".equals(lcType)) {
				return expr;
			}
			break;
		case 'b':
			if ("boolean".equals(type) || "bool".equals(lcType)) return expr.getFactory().toExprBoolean(expr);
			break;
		case 'd':
			if ("double".equals(type)) {
				return expr.getFactory().toExprNumber(expr);
			}
			break;
		case 'f':
			if ("float".equals(type)) return expr.getFactory().toExprNumber(expr);
			break;
		case 'i':
			if ("int".equals(lcType)) return expr.getFactory().toExprInt(expr);
		case 'n':
			if ("number".equals(lcType) || "numeric".equals(lcType)) {

				return expr.getFactory().toExprNumber(expr);
			}
			break;
		case 'o':
			if ("object".equals(lcType)) {
				return expr;
			}
			break;
		case 's':
			if ("string".equals(lcType)) return expr.getFactory().toExprString(expr);
			break;
		case 'u':
			if ("uuid".equals(lcType)) return expr.getFactory().toExprString(expr);
			break;
		case 'v':
			/*
			 * TODO if("variablename".equals(lcType)) return VariableString.toExprString(expr);
			 * if("variable_name".equals(lcType)) return VariableString.toExprString(expr);
			 * if("variablestring".equals(lcType)) return VariableString.toExprString(expr);
			 * if("variable_string".equals(lcType)) return VariableString.toExprString(expr);
			 */
			if ("void".equals(lcType)) return expr;
			break;
		}
		return new CastOther(expr, type, lcType);
	}

	@Override
	public Class<?> _writeOut(InterpreterContext ic, int mode) throws PageException {

		if ("java.lang.boolean".equals(lcType)) lcType = "boolean";
		else if ("java.lang.double".equals(lcType)) lcType = "double";
		else if ("java.lang.string".equals(lcType)) lcType = "string";
		else if ("java.lang.byte".equals(lcType)) lcType = "byte";
		else if ("java.lang.character".equals(lcType)) lcType = "character";
		else if ("java.lang.short".equals(lcType)) lcType = "short";
		else if ("java.lang.integer".equals(lcType)) lcType = "integer";
		else if ("java.lang.long".equals(lcType)) lcType = "long";
		else if ("java.lang.float".equals(lcType)) lcType = "float";
		else if ("java.io.file".equals(lcType)) lcType = "file";
		else if ("java.lang.object".equals(lcType)) lcType = "object";
		else if ("java.util.date".equals(lcType)) lcType = "date";

		char first = lcType.charAt(0);
		Object val;
		switch (first) {
		case 'a':
			if ("array".equals(lcType)) {
				val = ic.getValue(expr);
				if (!(val instanceof Array)) val = Caster.toArray(val);
				ic.stack(val);
				return Array.class;
			}
			break;
		case 'b':
			if ("base64".equals(lcType)) {
				ic.stack(Caster.toBase64(ic.getValue(expr)));
				return String.class;
			}
			if ("binary".equals(lcType) || "byte".equals(lcType)) {
				if (mode == MODE_VALUE) {
					ic.stack(ic.getValueAsByteValue(expr));
					return byte.class;
				}
				ic.stack(ic.getValueAsByte(expr));
				return Byte.class;
			}
			if ("boolean".equals(lcType)) {
				if (mode == MODE_VALUE) {
					ic.stack(ic.getValueAsBooleanValue(expr));
					return boolean.class;
				}
				ic.stack(ic.getValueAsBoolean(expr));
				return Boolean.class;
			}
			break;
		case 'c':
			if ("char".equals(lcType) || "character".equals(lcType)) {
				if (mode == MODE_VALUE) {
					ic.stack(ic.getValueAsCharValue(expr));
					return char.class;
				}
				ic.stack(ic.getValueAsCharacter(expr));
				return Character.class;
			}
			if ("collection".equals(lcType)) {
				val = ic.getValue(expr);
				if (!(val instanceof Collection)) val = Caster.toCollection(val);
				ic.stack(val);
				return Collection.class;
			}
			if ("component".equals(lcType) || "class".equals(lcType)) {
				val = ic.getValue(expr);
				if (!(val instanceof Component)) val = Caster.toComponent(val);
				ic.stack(val);
				return Component.class;
			}
			break;
		case 'd':
			if ("double".equals(lcType)) {
				if (mode == MODE_VALUE) {
					ic.stack(ic.getValueAsDoubleValue(expr));
					return double.class;
				}
				ic.stack(ic.getValueAsDouble(expr));
				return Double.class;
			}

			if ("date".equals(lcType) || "datetime".equals(lcType)) {
				val = ic.getValue(expr);
				if (!(val instanceof DateTime)) val = Caster.toDate(val, ThreadLocalPageContext.getTimeZone(ic.getPageContext()));
				ic.stack(val);
				return DateTime.class;
			}
			if ("decimal".equals(lcType)) {
				ic.stack(Caster.toDecimal(ic.getValue(expr), true));
				return String.class;
			}
			break;
		case 'f':
			if ("file".equals(lcType)) {
				val = ic.getValue(expr);
				if (!(val instanceof File)) val = Caster.toFile(val);
				ic.stack(val);
				return File.class;
			}
			if ("float".equals(lcType)) {
				if (mode == MODE_VALUE) {
					ic.stack(ic.getValueAsFloatValue(expr));
					return float.class;
				}
				ic.stack(ic.getValueAsFloat(expr));
				return Float.class;
			}
			break;
		case 'i':
			if ("int".equals(lcType) || "integer".equals(lcType)) {
				if (mode == MODE_VALUE) {
					ic.stack(ic.getValueAsIntValue(expr));
					return int.class;
				}
				ic.stack(ic.getValueAsInteger(expr));
				return Integer.class;
			}
			break;
		case 'j':
			if ("java.lang.stringbuffer".equals(lcType)) {
				val = ic.getValue(expr);
				if (!(val instanceof StringBuffer)) val = Caster.toStringBuffer(val);
				ic.stack(val);
				return StringBuffer.class;
			}
			break;
		case 'l':
			if ("long".equals(lcType)) {
				if (mode == MODE_VALUE) {
					ic.stack(ic.getValueAsLongValue(expr));
					return long.class;
				}
				ic.stack(ic.getValueAsLong(expr));
				return Long.class;
			}
			else if ("locale".equals(lcType)) {
				ic.stack(Caster.toLocale(ic.getValue(expr)));
				return Locale.class;
			}
			break;
		case 'n':
			if ("node".equals(lcType)) {
				val = ic.getValue(expr);
				if (!(val instanceof Node)) val = Caster.toNode(val);
				ic.stack(val);
				return Node.class;
			}
			else if ("null".equals(lcType)) {
				ic.stack(Caster.toNull(ic.getValue(expr)));
				// TODO gibt es einen null typ?
				return Object.class;
			}
			break;
		case 'o':
			if ("object".equals(lcType) || "other".equals(lcType)) {
				ic.stack(ic.getValue(expr));
				return Object.class;
			}
			break;
		case 't':
			if ("timezone".equals(lcType)) {
				ic.stack(Caster.toTimeZone(ic.getValue(expr)));
				return TimeZone.class;
			}
			else if ("timespan".equals(lcType)) {
				ic.stack(Caster.toTimespan(ic.getValue(expr)));
				return TimeSpan.class;
			}
			break;
		case 's':
			if ("struct".equals(lcType)) {
				val = ic.getValue(expr);
				if (!(val instanceof Struct)) val = Caster.toStruct(val);
				ic.stack(val);
				return Struct.class;
			}
			if ("short".equals(lcType)) {
				if (mode == MODE_VALUE) {
					ic.stack(ic.getValueAsShortValue(expr));
					return short.class;
				}
				ic.stack(ic.getValueAsShort(expr));
				return Short.class;
			}
			if ("stringbuffer".equals(lcType)) {
				val = ic.getValue(expr);
				if (!(val instanceof StringBuffer)) val = Caster.toStringBuffer(val);
				ic.stack(val);
				return StringBuffer.class;
			}
			break;

		case 'x':
			if ("xml".equals(lcType)) {
				val = ic.getValue(expr);
				if (!(val instanceof Node)) val = Caster.toNode(val);
				ic.stack(val);
				return Node.class;
			}
			break;
		default:
			if ("query".equals(lcType)) {
				val = ic.getValue(expr);
				if (!(val instanceof Query)) val = Caster.toQuery(val);
				ic.stack(val);
				return Query.class;
			}
			/*
			 * TODO if("querycolumn".equals(lcType)) { rtn=(expr instanceof
			 * Variable)?((VariableImpl)expr).writeOutCollectionAsType(ic, mode):expr.writeOut(ic,MODE_REF);
			 * if(!rtn.equals(Types.QUERY_COLUMN)) {
			 * 
			 * adapter.loadArg(0); adapter.invokeStatic(Types.CASTER,Methods_Caster.TO_QUERY_COLUMN); } return
			 * Types.QUERY_COLUMN; }
			 */
		}

		ic.stack(Caster.castTo(ic.getPageContext(), type, ic.getValue(expr), false));
		return Object.class;
	}

	@Override
	public Expression getExpr() {
		return expr;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
}
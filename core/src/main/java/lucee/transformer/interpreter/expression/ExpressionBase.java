package lucee.transformer.interpreter.expression;

import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.transformer.Context;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.TransformerException;
import lucee.transformer.expression.Expression;
import lucee.transformer.interpreter.InterpreterContext;

/**
 * An Expression (Operation, Literal aso.)
 */
public abstract class ExpressionBase implements Expression {

	private Position start;
	private Position end;
	private Factory factory;

	public ExpressionBase(Factory factory, Position start, Position end) {
		this.start = start;
		this.end = end;
		this.factory = factory;
	}

	public final Class<?> writeOut(Context c, int mode) throws TransformerException {
		try {
			return _writeOut((InterpreterContext) c, mode);
		}
		catch (PageException e) {
			// MUST make better different exception type with interface
			throw new PageRuntimeException(e);
		}
	}

	public abstract Class<?> _writeOut(InterpreterContext ic, int mode) throws PageException;

	@Override
	public Factory getFactory() {
		return factory;
	}

	@Override
	public Position getStart() {
		return start;
	}

	@Override
	public Position getEnd() {
		return end;
	}

	@Override
	public void setStart(Position start) {
		this.start = start;
	}

	@Override
	public void setEnd(Position end) {
		this.end = end;
	}

	public static class Result {
		public Class<?> type;
		public Object value;

		public Result(Class<?> type, Object value) {
			this.type = type;
			this.value = value;
		}

	}
}
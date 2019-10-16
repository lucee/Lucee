package lucee.runtime.type;

import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.DoubleBinaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.ObjDoubleConsumer;
import java.util.function.ObjIntConsumer;
import java.util.function.ObjLongConsumer;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToIntBiFunction;
import java.util.function.ToLongBiFunction;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.scope.Variables;

public abstract class EnvUDFMulti extends EnvUDF implements Comparator, ToIntBiFunction, ToLongBiFunction, ToDoubleBiFunction, BiConsumer, BiFunction, BiPredicate, BinaryOperator,
		LongBinaryOperator, DoubleBinaryOperator, IntBinaryOperator, ObjDoubleConsumer, ObjIntConsumer, ObjLongConsumer {

	public EnvUDFMulti() {
		super();
	}

	EnvUDFMulti(UDFProperties properties) {
		super(properties);
	}

	EnvUDFMulti(UDFProperties properties, Variables variables) {
		super(properties, variables);
	}

	@Override
	public int compare(Object o1, Object o2) {
		try {
			return Caster.toIntValue(call(ThreadLocalPageContext.get(), new Object[] { o1, o2 }, true));
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	@Override
	public double applyAsDouble(Object t, Object u) {
		try {
			return Caster.toDoubleValue(call(ThreadLocalPageContext.get(), new Object[] { t, u }, true));
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	@Override
	public long applyAsLong(Object t, Object u) {
		try {
			return Caster.toLongValue(call(ThreadLocalPageContext.get(), new Object[] { t, u }, true));
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	@Override
	public long applyAsLong(long left, long right) {
		return applyAsLong(Long.valueOf(left), Long.valueOf(right));
	}

	@Override
	public int applyAsInt(Object t, Object u) {
		try {
			return Caster.toIntValue(call(ThreadLocalPageContext.get(), new Object[] { t, u }, true));
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	@Override
	public int applyAsInt(int left, int right) {
		return applyAsInt(Double.valueOf(left), Double.valueOf(right));
	}

	@Override
	public void accept(Object t, Object u) {
		try {
			call(ThreadLocalPageContext.get(), new Object[] { t, u }, true);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	@Override
	public void accept(Object t, double u) {
		accept(t, Double.valueOf(u));
	}

	@Override
	public void accept(Object t, int u) {
		accept(t, Integer.valueOf(u));
	}

	@Override
	public void accept(Object t, long u) {
		accept(t, Long.valueOf(u));
	}

	@Override
	public Object apply(Object t, Object u) {
		try {
			return call(ThreadLocalPageContext.get(), new Object[] { t, u }, true);
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	@Override
	public double applyAsDouble(double left, double right) {
		try {
			return Caster.toDoubleValue(call(ThreadLocalPageContext.get(), new Object[] { left, right }, true));
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

	@Override
	public boolean test(Object t, Object u) {
		try {
			return Caster.toBooleanValue(call(ThreadLocalPageContext.get(), new Object[] { t, u }, true));
		}
		catch (PageException pe) {
			throw new PageRuntimeException(pe);
		}
	}

}

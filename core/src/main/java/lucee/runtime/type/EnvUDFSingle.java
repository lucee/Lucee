package lucee.runtime.type;

import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;

import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.exp.PageRuntimeException;
import lucee.runtime.op.Caster;
import lucee.runtime.type.scope.Variables;

// TODO DoublePredicate,IntPredicate,LongPredicate
public abstract class EnvUDFSingle extends EnvUDF implements ToIntFunction, ToLongFunction, ToDoubleFunction, Consumer, LongConsumer, IntConsumer, UnaryOperator,
	DoubleUnaryOperator, IntUnaryOperator, IntFunction, Function, LongFunction, Predicate, DoubleConsumer, DoubleFunction, DoubleToIntFunction, DoubleToLongFunction,
	IntToDoubleFunction, IntToLongFunction, LongToDoubleFunction, LongToIntFunction, LongUnaryOperator {

    public EnvUDFSingle() {
	super();
    }

    EnvUDFSingle(UDFProperties properties) {
	super(properties);
    }

    EnvUDFSingle(UDFProperties properties, Variables variables) {
	super(properties, variables);
    }

    @Override
    public int applyAsInt(Object value) {
	try {
	    return Caster.toIntValue(call(ThreadLocalPageContext.get(), new Object[] { value }, true));
	}
	catch (PageException pe) {
	    throw new PageRuntimeException(pe);
	}
    }

    @Override
    public int applyAsInt(double value) {
	return applyAsInt(Double.valueOf(value));
    }

    @Override
    public int applyAsInt(int value) {
	return applyAsInt(Integer.valueOf(value));
    }

    @Override
    public int applyAsInt(long value) {
	return applyAsInt(Long.valueOf(value));
    }

    @Override
    public double applyAsDouble(Object value) {
	try {
	    return Caster.toDoubleValue(call(ThreadLocalPageContext.get(), new Object[] { value }, true));
	}
	catch (PageException pe) {
	    throw new PageRuntimeException(pe);
	}
    }

    @Override
    public double applyAsDouble(double value) {
	return applyAsDouble(Double.valueOf(value));
    }

    @Override
    public double applyAsDouble(int value) {
	return applyAsDouble(Integer.valueOf(value));
    }

    @Override
    public double applyAsDouble(long value) {
	return applyAsDouble(Long.valueOf(value));
    }

    @Override
    public long applyAsLong(Object value) {
	try {
	    return Caster.toLongValue(call(ThreadLocalPageContext.get(), new Object[] { value }, true));
	}
	catch (PageException pe) {
	    throw new PageRuntimeException(pe);
	}
    }

    @Override
    public long applyAsLong(double value) {
	return applyAsLong(Double.valueOf(value));
    }

    @Override
    public long applyAsLong(int value) {
	return applyAsLong(Integer.valueOf(value));
    }

    @Override
    public long applyAsLong(long value) {
	return applyAsLong(Long.valueOf(value));
    }

    @Override
    public void accept(Object t) {
	try {
	    call(ThreadLocalPageContext.get(), new Object[] { t }, true);
	}
	catch (PageException pe) {
	    throw new PageRuntimeException(pe);
	}
    }

    @Override
    public void accept(int value) {
	accept(Integer.valueOf(value));
    }

    @Override
    public void accept(long value) {
	accept(Long.valueOf(value));
    }

    @Override
    public void accept(double value) {
	accept(Double.valueOf(value));
    }

    @Override
    public Object apply(Object t) {
	try {
	    return call(ThreadLocalPageContext.get(), new Object[] { t }, true);
	}
	catch (PageException pe) {
	    throw new PageRuntimeException(pe);
	}
    }

    @Override
    public Object apply(double value) {
	return apply(Double.valueOf(value));
    }

    @Override
    public Object apply(long value) {
	return apply(Long.valueOf(value));
    }

    @Override
    public Object apply(int value) {
	return apply(Integer.valueOf(value));
    }

    @Override
    public boolean test(Object t) {
	try {
	    return Caster.toBooleanValue(call(ThreadLocalPageContext.get(), new Object[] { t }, true));
	}
	catch (PageException pe) {
	    throw new PageRuntimeException(pe);
	}
    }

}

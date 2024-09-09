package lucee.runtime.functions.math;

import java.math.BigDecimal;

import lucee.runtime.PageContext;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class RandRange implements Function {

	private static final long serialVersionUID = -695896210547038106L;

	public static Number call(PageContext pc, Number number1, Number number2) throws ExpressionException {
		return call(pc, number1, number2, "cfmx_compat");
	}

	public static Number call(PageContext pc, Number number1, Number number2, String algo) throws ExpressionException {
		// Handle BigDecimal for precise math
		if (ThreadLocalPageContext.preciseMath(pc)) {
			BigDecimal min = Caster.toBigDecimal(number1);
			BigDecimal max = Caster.toBigDecimal(number2);

			if (min.compareTo(max) > 0) {
				BigDecimal tmp = min;
				min = max;
				max = tmp;
			}

			BigDecimal diff = max.subtract(min);
			BigDecimal randValue = Caster.toBigDecimal(Rand.call(pc, algo)).multiply(diff.add(BigDecimal.ONE));

			return randValue.add(min); // Can return as BigDecimal
		}

		// Handle double for non-precise math
		double min = number1.doubleValue();
		double max = number2.doubleValue();

		// Swap min and max if necessary
		if (min > max) {
			double tmp = min;
			min = max;
			max = tmp;
		}

		// Calculate the difference and random value
		int diff = (int) (max - min);
		return ((int) (Rand.call(pc, algo).doubleValue() * (diff + 1))) + (int) min;

	}

	public static int invoke(int min, int max) throws ExpressionException {

		if (min > max) {
			int tmp = min;
			min = max;
			max = tmp;
		}

		// Calculate the difference and random value
		int diff = max - min;
		return ((int) (Rand.call(null, "cfmx_compat").doubleValue() * (diff + 1))) + min;
	}
}

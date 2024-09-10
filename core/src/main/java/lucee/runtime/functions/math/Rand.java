/**
 *
 * Copyright (c) 2014, the Railo Company Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package lucee.runtime.functions.math;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import lucee.runtime.PageContext;
import lucee.runtime.crypt.CFMXCompat;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class Rand implements Function {

	private static final long serialVersionUID = -9153653138698137803L;
	private static Map<String, Random> randoms = new HashMap<String, Random>();

	public static Number call(PageContext pc) throws ExpressionException {
		return call(pc, CFMXCompat.ALGORITHM_NAME);
	}

	public static Number call(PageContext pc, String algorithm) throws ExpressionException {
		if (ThreadLocalPageContext.preciseMath(pc)) {
			return Caster.toBigDecimal(getRandom(algorithm, Double.NaN).nextDouble());
		}
		return getRandom(algorithm, Double.NaN).nextDouble();
	}

	// Helper method to get the Random instance based on the algorithm
	static Random getRandom(String algorithm, Double seed) throws ExpressionException {
		algorithm = algorithm.toLowerCase();

		Random result = randoms.get(algorithm);

		if (result == null || !seed.isNaN()) {
			if (CFMXCompat.ALGORITHM_NAME.equalsIgnoreCase(algorithm)) {
				result = new Random();
			}
			else {
				try {
					result = SecureRandom.getInstance(algorithm);
				}
				catch (NoSuchAlgorithmException e) {
					throw new ExpressionException("random algorithm [" + algorithm + "] is not installed on the system", e.getMessage());
				}
			}

			if (!seed.isNaN()) result.setSeed(seed.longValue());

			randoms.put(algorithm, result);
		}

		return result;
	}
}

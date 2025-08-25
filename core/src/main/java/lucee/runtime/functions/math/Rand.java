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
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import lucee.commons.lang.ExceptionUtil;
import lucee.runtime.PageContext;
import lucee.runtime.crypt.CFMXCompat;
import lucee.runtime.crypt.Cryptor;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.type.util.ListUtil;

public final class Rand implements Function {

	private static final long serialVersionUID = -9153653138698137803L;
	private static Map<String, Random> randoms = new ConcurrentHashMap<String, Random>();

	public static Number call(PageContext pc) throws ExpressionException {
		return call(pc, Cryptor.DEFAULT_ALGORITHM);
	}

	public static Number call(PageContext pc, String algorithm) throws ExpressionException {
		if (ThreadLocalPageContext.preciseMath(pc)) {
			return Caster.toBigDecimal(getRandom(pc, algorithm, Double.NaN).nextDouble());
		}
		return getRandom(pc, algorithm, Double.NaN).nextDouble();
	}

	// Helper method to get the Random instance based on the algorithm
	static Random getRandom(PageContext pc, String algorithm, Double seed) throws ExpressionException {
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
					if (pc != null) {
						FunctionException fe = new FunctionException(pc, "Rand", 1, "algorithm", "The random alogrithm [" + algorithm + "] is not supported. Supported algorithms are [ " + getAvailableRandomAlgorithms() + " ]", e.getMessage());
						ExceptionUtil.initCauseEL(fe, e);
						throw fe;
					}
					throw new ExpressionException("random algorithm [" + algorithm + "] is not available", e.getMessage());
				}
			}

			if (!seed.isNaN()) result.setSeed(seed.longValue());

			randoms.put(algorithm, result);
		}

		return result;
	}

	private static String getAvailableRandomAlgorithms() {
		ArrayList<String> algorithms = new ArrayList<>();
		for (Provider provider : Security.getProviders()) {
			for (Provider.Service service : provider.getServices()) {
				if ("SecureRandom".equalsIgnoreCase(service.getType())) {
					algorithms.add(service.getAlgorithm());
				}
			}
		}
		algorithms.add(CFMXCompat.ALGORITHM_NAME);
		return ListUtil.toList(algorithms, ", ");
	}
}

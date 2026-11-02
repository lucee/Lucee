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
/**
 * Implements the CFML Function hash
 */
package lucee.runtime.functions.string;

import java.security.MessageDigest;
import java.util.Base64;

import lucee.commons.digest.HashUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;
import lucee.runtime.op.Decision;

public final class Hash extends BIF implements Function {

	private static final long serialVersionUID = 1161445102079248547L;

	private static final String OUTPUT_HEX = "hex";
	private static final String OUTPUT_BASE64 = "base64";
	private static final String OUTPUT_BASE64URL = "base64url";

	// function for old code in ra files calling this function
	public static String call(PageContext pc, String input) throws PageException {
		return invoke(pc.getConfig(), input, null, null, 1);
	}

	public static String call(PageContext pc, String input, String algorithm) throws PageException {
		return invoke(pc.getConfig(), input, algorithm, null, 1);
	}

	public static String call(PageContext pc, String input, String algorithm, String encoding) throws PageException {
		return invoke(pc.getConfig(), input, algorithm, encoding, 1);
	}

	public static String call(PageContext pc, Object input) throws PageException {
		return invoke(pc.getConfig(), input, null, null, 1);
	}

	public static String call(PageContext pc, Object input, String algorithm) throws PageException {
		return invoke(pc.getConfig(), input, algorithm, null, 1);
	}

	public static String call(PageContext pc, Object input, String algorithm, String encoding) throws PageException {
		return invoke(pc.getConfig(), input, algorithm, encoding, 1);
	}

	public static String call(PageContext pc, Object input, String algorithm, String encoding, Number numIterations) throws PageException {
		return invoke(pc.getConfig(), input, algorithm, encoding, Caster.toIntValue(numIterations));
	}

	// Lucee order is (input, algorithm, encoding, numIterations, outputEncoding), Adobe order is (input, algorithm, encoding,
	// outputEncoding, iterations). Both are supported: when the 4th positional argument is not a number it is the outputEncoding.
	public static String call(PageContext pc, Object input, String algorithm, String encoding, Object numIterations) throws PageException {
		return call(pc, input, algorithm, encoding, numIterations, null);
	}

	public static String call(PageContext pc, Object input, String algorithm, String encoding, Object numIterations, Object outputEncoding) throws PageException {
		if (isOutputEncoding(numIterations)) {
			Object tmp = numIterations;
			numIterations = outputEncoding;
			outputEncoding = tmp;
		}
		int iterations = 1;
		if (!isEmpty(numIterations)) iterations = Caster.toIntValue(numIterations);
		return invoke(pc.getConfig(), input, algorithm, encoding, iterations, isEmpty(outputEncoding) ? null : Caster.toString(outputEncoding));
	}

	private static boolean isEmpty(Object obj) {
		return obj == null || (obj instanceof String && StringUtil.isEmpty((String) obj, true));
	}

	private static boolean isOutputEncoding(Object obj) {
		return obj instanceof CharSequence && !isEmpty(obj) && !Decision.isNumber(obj);
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 5) return call(pc, args[0], Caster.toString(args[1]), Caster.toString(args[2]), args[3], args[4]);
		if (args.length == 4) return call(pc, args[0], Caster.toString(args[1]), Caster.toString(args[2]), args[3], null);
		if (args.length == 3) return call(pc, args[0], Caster.toString(args[1]), Caster.toString(args[2]));
		if (args.length == 2) return call(pc, args[0], Caster.toString(args[1]));
		if (args.length == 1) return call(pc, args[0]);
		throw new FunctionException(pc, "Hash", 1, 5, args.length);
	}

	public static String invoke(Config config, Object input, String algorithm, String encoding, int numIterations) throws PageException {
		return invoke(config, input, algorithm, encoding, numIterations, null);
	}

	public static String invoke(Config config, Object input, String algorithm, String encoding, int numIterations, String outputEncoding) throws PageException {
		if (numIterations < 1) numIterations = 1;
		outputEncoding = toOutputEncoding(outputEncoding);

		if (StringUtil.isEmpty(algorithm)) algorithm = "md5";
		else algorithm = algorithm.trim().toLowerCase();
		if ("cfmx_compat".equals(algorithm)) algorithm = "md5";
		else if ("quick".equals(algorithm)) {
			if (numIterations > 1) throw new ExpressionException("for algorithm [quick], argument [numIterations] makes no sense, because this algorithm has no security in mind");
			if (!OUTPUT_HEX.equals(outputEncoding))
				throw new ExpressionException("for algorithm [quick], argument [outputEncoding] only supports [" + OUTPUT_HEX + "], but received [" + outputEncoding + "]");
			return HashUtil.create64BitHashAsString(Caster.toString(input), 16);
		}

		if (StringUtil.isEmpty(encoding)) encoding = config.getWebCharset().name();
		byte[] data = null;

		try {
			if (input instanceof byte[]) data = (byte[]) input;
			else data = Caster.toString(input).getBytes(encoding);

			MessageDigest md = MessageDigest.getInstance(algorithm);
			md.reset();
			for (int i = 0; i < numIterations; i++) {
				data = md.digest(data);
			}
			if (OUTPUT_BASE64.equals(outputEncoding)) return Base64.getEncoder().encodeToString(data);
			if (OUTPUT_BASE64URL.equals(outputEncoding)) return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
			return lucee.commons.digest.Hash.toHexString(data, true);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
	}

	private static String toOutputEncoding(String outputEncoding) throws ExpressionException {
		if (StringUtil.isEmpty(outputEncoding, true)) return OUTPUT_HEX;
		String oe = outputEncoding.trim().toLowerCase();
		if (OUTPUT_HEX.equals(oe) || OUTPUT_BASE64.equals(oe) || OUTPUT_BASE64URL.equals(oe)) return oe;
		throw new ExpressionException("invalid value [" + outputEncoding + "] for argument [outputEncoding], valid values are [" + OUTPUT_HEX + ", " + OUTPUT_BASE64 + ", "
				+ OUTPUT_BASE64URL + "]");
	}

}

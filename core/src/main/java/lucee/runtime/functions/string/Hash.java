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

import lucee.commons.digest.HashUtil;
import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.config.Config;
import lucee.runtime.engine.ThreadLocalPageContext;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class Hash implements Function {

	private static final long serialVersionUID = 1161445102079248547L;

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

	public static String call(PageContext pc, Object input, String algorithm, String encoding, double numIterations) throws PageException {
		return invoke(pc.getConfig(), input, algorithm, encoding, (int) numIterations);
	}

	public static String invoke(Config config, Object input, String algorithm, String encoding, int numIterations) throws PageException {
		if (numIterations < 1) numIterations = 1;

		if (StringUtil.isEmpty(algorithm)) algorithm = "md5";
		else algorithm = algorithm.trim().toLowerCase();
		if ("cfmx_compat".equals(algorithm)) algorithm = "md5";
		else if ("quick".equals(algorithm)) {
			if (numIterations > 1) LogUtil.log(ThreadLocalPageContext.getConfig(config), Log.LEVEL_INFO, Hash.class.getName(),
					"for algorithm [quick], argument numIterations makes no sense, because this algorithm has no security in mind");
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
			return lucee.commons.digest.Hash.toHexString(data, true);
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			throw Caster.toPageException(t);
		}
	}

}
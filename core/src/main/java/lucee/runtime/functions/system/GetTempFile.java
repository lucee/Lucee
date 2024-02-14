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
 * Implements the CFML Function gettempfile
 */
package lucee.runtime.functions.system;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import lucee.commons.io.SystemUtil;
import lucee.commons.io.res.Resource;
import lucee.commons.io.res.util.ResourceUtil;
import lucee.commons.lang.StringUtil;
import lucee.runtime.PageContext;
import lucee.runtime.exp.ExpressionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class GetTempFile implements Function {

	private static final long serialVersionUID = -166719554831864953L;
	private static char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();;

	public static String call(PageContext pc, String strDir, String prefix) throws PageException {
		return call(pc, strDir, prefix, ".tmp");
	}

	public static String call(PageContext pc, String strDir, String prefix, String extension) throws PageException {
		Resource dir = ResourceUtil.toResourceExisting(pc, strDir);
		pc.getConfig().getSecurityManager().checkFileLocation(dir);
		if (!dir.isDirectory()) throw new ExpressionException("[" + strDir + "] is not a directory");
		Resource file;

		final int MAX_RETRY = 2;
		boolean fileCreated = false;
		if (StringUtil.isEmpty(extension, true)) {
			extension = ".tmp";
		}
		else if (extension.charAt(0) != '.') {
			extension = "." + extension;
		}

		String randomPart = "" + getRandomChar();
		do {
			file = dir.getRealResource(prefix + pc.getId() + randomPart + extension);
			synchronized (SystemUtil.createToken("", file.getAbsolutePath())) {
				fileCreated = file.createNewFile();
				if (fileCreated) {
					try {
						return file.getCanonicalPath();
					}
					catch (IOException e) {
						// File was created, yet, we have an exception this is probably pretty bad
						throw Caster.toPageException(e);
					}
				}
				randomPart += getRandomChar();
			}
		}
		while (randomPart.length() < MAX_RETRY);
		throw new ExpressionException("Unable to create temporary file in [" + strDir + "] after " + MAX_RETRY + " tries", "IOException");
	}

	private static char getRandomChar() {
		return CHARS[ThreadLocalRandom.current().nextInt(CHARS.length)];
	}
}

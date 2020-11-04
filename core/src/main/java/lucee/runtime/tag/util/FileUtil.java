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
package lucee.runtime.tag.util;

import lucee.commons.lang.StringUtil;
import lucee.runtime.exp.ApplicationException;

public class FileUtil {

	public static final int NAMECONFLICT_UNDEFINED = 1; // can't start at 0 because we need to be able to do a bitmask test
	public static final int NAMECONFLICT_ERROR = 2;
	public static final int NAMECONFLICT_SKIP = 4; // same as IGNORE
	public static final int NAMECONFLICT_OVERWRITE = 8; // same as MERGE
	public static final int NAMECONFLICT_MAKEUNIQUE = 16;
	public static final int NAMECONFLICT_FORCEUNIQUE = 32;
	// public static final int NAMECONFLICT_CLOSURE = 32; // FUTURE

	public static int toNameConflict(String nameConflict) throws ApplicationException {

		if (StringUtil.isEmpty(nameConflict, true)) return NAMECONFLICT_UNDEFINED;
		nameConflict = nameConflict.trim().toLowerCase();

		if ("error".equals(nameConflict)) return NAMECONFLICT_ERROR;

		if ("skip".equals(nameConflict) || "ignore".equals(nameConflict)) return NAMECONFLICT_SKIP;

		if ("merge".equals(nameConflict) || "overwrite".equals(nameConflict)) return NAMECONFLICT_OVERWRITE;

		if ("makeunique".equals(nameConflict) || "unique".equals(nameConflict)) return NAMECONFLICT_MAKEUNIQUE;

		if ("forceunique".equals(nameConflict)) return NAMECONFLICT_FORCEUNIQUE;
		
		throw new ApplicationException("Invalid value for attribute nameConflict [" + nameConflict + "]", "valid values are [" + fromNameConflictBitMask(Integer.MAX_VALUE) + "]");
	}

	/**
	 *
	 * @param nameConflict
	 * @param allowedValuesMask
	 * @return
	 * @throws ApplicationException
	 */
	public static int toNameConflict(String nameConflict, int allowedValuesMask) throws ApplicationException {

		int result = toNameConflict(nameConflict);

		if ((allowedValuesMask & result) == 0) {

			throw new ApplicationException("Invalid value for attribute nameConflict [" + nameConflict + "]",
					"valid values are [" + fromNameConflictBitMask(allowedValuesMask) + "]");
		}

		return result;
	}

	/**
	 *
	 * @param nameConflict
	 * @param allowedValuesMask
	 * @param defaultValue
	 * @return
	 * @throws ApplicationException
	 */
	public static int toNameConflict(String nameConflict, int allowedValuesMask, int defaultValue) throws ApplicationException {

		int result = toNameConflict(nameConflict, allowedValuesMask);

		if (result == NAMECONFLICT_UNDEFINED) return defaultValue;

		return result;
	}

	public static String fromNameConflictBitMask(int bitmask) {

		StringBuilder sb = new StringBuilder();

		if ((bitmask & NAMECONFLICT_ERROR) > 0) sb.append("error").append(',');
		if ((bitmask & NAMECONFLICT_MAKEUNIQUE) > 0) sb.append("makeunique (unique)").append(',');
		if ((bitmask & NAMECONFLICT_FORCEUNIQUE) > 0) sb.append("forceunique").append(',');
		if ((bitmask & NAMECONFLICT_OVERWRITE) > 0) sb.append("overwrite (merge)").append(',');
		if ((bitmask & NAMECONFLICT_SKIP) > 0) sb.append("skip (ignore)").append(',');

		if (sb.length() > 0) sb.setLength(sb.length() - 1); // remove last ,

		return sb.toString();
	}

}
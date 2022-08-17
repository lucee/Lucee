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
package lucee.intergral.fusiondebug.server.util;

import java.util.Iterator;

import com.intergral.fusiondebug.server.IFDValue;
import com.intergral.fusiondebug.server.IFDVariable;

import lucee.commons.io.log.Log;
import lucee.commons.io.log.LogUtil;
import lucee.runtime.PageContext;

public class FDDump {

	public static void dump(IFDVariable var) {
		LogUtil.log((PageContext) null, Log.LEVEL_INFO, FDDump.class.getName(), toString(var));
	}

	public static String toString(Object value) {
		StringBuffer sb = new StringBuffer();
		dump(sb, value, 0);
		return sb.toString();
	}

	public static String toString(IFDVariable var) {
		StringBuffer sb = new StringBuffer();
		dump(sb, var, 0);
		return sb.toString();
	}

	private static void dump(StringBuffer sb, Object value, int level) {
		if (value instanceof IFDValue) dump(sb, (IFDValue) value, level);
		else dump(sb, (IFDVariable) value, level);
	}

	private static void dump(StringBuffer sb, IFDValue value, int level) {
		for (int i = 0; i < level; i++) {
			sb.append(" - ");
		}

		sb.append(value.toString());
		sb.append("\n");
		if (value.hasChildren()) {
			Iterator it = value.getChildren().iterator();
			while (it.hasNext()) {
				Object o = it.next();
				dump(sb, (IFDVariable) o, level + 1);
			}
		}
	}

	private static void dump(StringBuffer sb, IFDVariable var, int level) {
		for (int i = 0; i < level; i++) {
			sb.append(" - ");
		}
		sb.append(var.getName());
		sb.append(":");
		IFDValue value = var.getValue();

		sb.append(value.toString());
		sb.append("\n");
		// print.err(value.getClass().getName());
		if (value.hasChildren()) {
			Iterator it = value.getChildren().iterator();
			while (it.hasNext()) {
				Object o = it.next();
				// print.err(o.getClass().getName());
				dump(sb, (IFDVariable) o, level + 1);
				// dump(sb,(IFDVariable) it.next(),level+1);
			}
		}
	}

}
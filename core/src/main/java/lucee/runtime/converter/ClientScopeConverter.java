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
package lucee.runtime.converter;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import lucee.commons.lang.ParserString;
import lucee.commons.lang.StringUtil;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;
import lucee.runtime.type.dt.DateTime;

public class ClientScopeConverter {

	public static Struct unserialize(String str) {
		Struct sct = new StructImpl();
		ParserString ps = new ParserString(str);

		StringBuilder sb = new StringBuilder();
		String key = null;
		while (!ps.isAfterLast()) {
			if (ps.isCurrent('#')) {
				if (ps.isNext('=')) {
					ps.next();
					sb.append('=');
				}
				else if (ps.isNext('#')) {
					ps.next();
					sb.append('#');
				}
				else {
					sct.setEL(key, sb.toString());
					sb = new StringBuilder();
				}
			}
			else if (ps.isCurrent('=')) {
				key = sb.toString();
				sb = new StringBuilder();
			}
			else sb.append(ps.getCurrent());
			ps.next();
		}

		if (!StringUtil.isEmpty(key) && !StringUtil.isEmpty(sb)) {
			sct.setEL(key, sb.toString());
		}
		return sct;

		/*
		 * int index=0,last=0; while((index=str.indexOf('#',last))!=-1) { outer:while(str.length()+1>index)
		 * { c=str.charAt(index+1); if(c=='#' || c=='=') { last=index+1; continue; } }
		 * _unserialize(str.substring(last,index)); last=index+1; } _unserialize(str.substring(last));
		 */

	}

	public static String serialize(Struct sct) throws ConverterException {
		// TODO Auto-generated method stub
		return serialize(sct, null);
	}

	public static String serialize(Struct sct, Set ignoreSet) throws ConverterException {
		StringBuilder sb = new StringBuilder();
		Iterator it = sct.keyIterator();
		boolean doIt = false;
		Object oKey;
		while (it.hasNext()) {
			oKey = it.next();
			if (ignoreSet != null && ignoreSet.contains(oKey)) continue;
			String key = Caster.toString(oKey, "");
			if (doIt) sb.append('#');
			doIt = true;
			sb.append(escape(key));
			sb.append('=');
			sb.append(_serialize(sct.get(key, "")));
		}
		return sb.toString();
	}

	private static String escape(String str) {
		int len = str.length();
		StringBuilder sb = new StringBuilder();
		char c;
		for (int i = 0; i < len; i++) {
			c = str.charAt(i);
			if (c == '=') sb.append("#=");
			else if (c == '#') sb.append("##");
			else sb.append(c);
		}
		return sb.toString();
	}

	private static String _serialize(Object object) throws ConverterException {

		if (object == null) return "";

		// String
		else if (object instanceof String) return escape(object.toString());

		// Number
		else if (object instanceof Number) return Caster.toString(((Number) object));

		// Boolean
		else if (object instanceof Boolean) return Caster.toString(((Boolean) object).booleanValue());

		// DateTime
		else if (object instanceof DateTime) return Caster.toString(object, null);

		// Date
		else if (object instanceof Date) return Caster.toString(object, null);

		throw new ConverterException("Can't convert complex value [" + Caster.toTypeName(object) + "] to a simple value");
	}
}

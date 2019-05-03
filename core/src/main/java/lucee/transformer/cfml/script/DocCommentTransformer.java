/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Assosication Switzerland
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
 */
package lucee.transformer.cfml.script;

import lucee.commons.lang.ExceptionUtil;
import lucee.commons.lang.ParserString;
import lucee.commons.lang.StringUtil;
import lucee.transformer.Factory;
import lucee.transformer.bytecode.statement.tag.Attribute;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitBoolean;

public class DocCommentTransformer {

	public DocComment transform(Factory f, String str) {
		try {
			DocComment dc = new DocComment();
			str = str.trim();
			if (str.startsWith("/**")) str = str.substring(3);
			if (str.endsWith("*/")) str = str.substring(0, str.length() - 2);
			ParserString ps = new ParserString(str);
			transform(f, dc, ps);
			dc.getHint();// TODO do different -> make sure internal structure is valid
			return dc;
		}
		catch (Throwable t) {
			ExceptionUtil.rethrowIfNecessary(t);
			return null;
		}
	}

	private void transform(Factory factory, DocComment dc, ParserString ps) {
		while (ps.isValidIndex()) {
			asterix(ps);
			ps.removeSpace();
			// param
			if (ps.forwardIfCurrent('@')) {
				dc.addParam(param(factory, ps));
			}
			// hint
			else {
				while (ps.isValidIndex() && ps.getCurrent() != '\n') {
					dc.addHint(ps.getCurrent());
					ps.next();
				}
				dc.addHint('\n');
			}
			ps.removeSpace();
		}
	}

	private Attribute param(Factory factory, ParserString ps) {
		String name = paramName(ps);
		if (name == null) return new Attribute(true, "@", factory.TRUE(), "boolean");

		// white space
		while (ps.isValidIndex() && ps.isCurrentWhiteSpace()) {
			if (ps.getCurrent() == '\n') return new Attribute(true, name, factory.TRUE(), "boolean");
			ps.next();
		}
		Expression value = paramValue(factory, ps);
		return new Attribute(true, name, value, value instanceof LitBoolean ? "boolean" : "string");
	}

	private String paramName(ParserString ps) {
		StringBuilder sb = new StringBuilder();
		while (ps.isValidIndex() && !ps.isCurrentWhiteSpace()) {
			sb.append(ps.getCurrent());
			ps.next();
		}
		if (sb.length() == 0) return null;
		return sb.toString();
	}

	private Expression paramValue(Factory factory, ParserString ps) {
		StringBuilder sb = new StringBuilder();
		while (ps.isValidIndex() && ps.getCurrent() != '\n') {
			sb.append(ps.getCurrent());
			ps.next();
		}
		if (sb.length() == 0) return factory.TRUE();
		return factory.createLitString(StringUtil.unwrap(sb.toString()));
	}

	private void asterix(ParserString ps) {
		do {
			ps.removeSpace();
		}
		while (ps.forwardIfCurrent('*'));

	}

}
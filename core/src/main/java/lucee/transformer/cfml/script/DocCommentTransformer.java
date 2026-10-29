/**
 * Copyright (c) 2014, the Railo Company Ltd.
 * Copyright (c) 2015, Lucee Association Switzerland
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
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitBoolean;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.statement.tag.Attribute;

public final class DocCommentTransformer {

	public DocComment transform(Factory f, String str) {
		try {
			DocComment dc = new DocComment();
			dc.setRawText(str); // preserve raw docblock for AST round-tripping
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
		// the last tag that is allowed to continue on the following lines (like in Javadoc)
		Attribute continuable = null;
		while (ps.isValidIndex()) {
			asterix(ps);
			ps.removeSpace();
			// param
			if (ps.forwardIfCurrent('@')) {
				Attribute attr = param(factory, ps);
				dc.addParam(attr);
				continuable = isMultiLine(attr) ? attr : null;
			}
			else {
				StringBuilder line = new StringBuilder();
				while (ps.isValidIndex() && ps.getCurrent() != '\n') {
					line.append(ps.getCurrent());
					ps.next();
				}
				// text after a hint or return tag belongs to that tag up to the next tag or an empty line
				String text = line.toString().trim();
				if (continuable != null && text.length() > 0) {
					continuable = new Attribute(true, continuable.getName(), factory.createLitString(((LitString) continuable.getValue()).getString() + "\n" + text), "string");
					dc.addParam(continuable);
				}
				// hint
				else {
					continuable = null;
					for (int i = 0; i < line.length(); i++) {
						dc.addHint(line.charAt(i));
					}
					dc.addHint('\n');
				}
			}
			ps.removeSpace();
		}
	}

	/**
	 * only the tags describing something (hint of the function or an argument, return) can have a
	 * value over multiple lines, other tags like "inject" are used as simple values
	 */
	private static boolean isMultiLine(Attribute attr) {
		if (!(attr.getValue() instanceof LitString)) return false;
		String name = attr.getName().toLowerCase();
		return "hint".equals(name) || "return".equals(name) || name.endsWith(".hint");
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
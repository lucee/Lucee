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
package lucee.transformer.cfml.expression;

import lucee.runtime.exp.TemplateException;
import lucee.transformer.Factory;
import lucee.transformer.Position;
import lucee.transformer.cfml.Data;
import lucee.transformer.cfml.ExprTransformer;
import lucee.transformer.expression.Expression;
import lucee.transformer.expression.literal.LitString;
import lucee.transformer.util.SourceCode;

/**
 * Zum lesen von Attributen bei dem CFML expressions nicht geparst werden sollen (cfloop condition)
 */
public final class SimpleExprTransformer implements ExprTransformer {

	private char specialChar;

	public SimpleExprTransformer(char specialChar) {
		this.specialChar = specialChar;
	}

	@Override
	public Expression transformAsString(Data data) throws TemplateException {
		return transform(data);
	}

	@Override
	public Expression transform(Data data) throws TemplateException {
		Expression expr = null;
		// String
		if ((expr = string(data.factory, data.srcCode)) != null) {
			return expr;
		}
		// Simple
		return simple(data.factory, data.srcCode);
	}

	/**
	 * Liest den String ein
	 * 
	 * @return Element
	 * @throws TemplateException
	 */
	public Expression string(Factory f, SourceCode cfml) throws TemplateException {
		cfml.removeSpace();
		char quoter = cfml.getCurrentLower();
		if (quoter != '"' && quoter != '\'') return null;
		StringBuffer str = new StringBuffer();
		boolean insideSpecial = false;

		Position line = cfml.getPosition();
		while (cfml.hasNext()) {
			cfml.next();
			// check special
			if (cfml.isCurrent(specialChar)) {
				insideSpecial = !insideSpecial;
				str.append(specialChar);

			}
			// check quoter
			else if (!insideSpecial && cfml.isCurrent(quoter)) {
				// Ecaped sharp
				if (cfml.isNext(quoter)) {
					cfml.next();
					str.append(quoter);
				}
				// finsish
				else {
					break;
				}
			}
			// all other character
			else {
				str.append(cfml.getCurrent());
			}
		}

		if (!cfml.forwardIfCurrent(quoter)) throw new TemplateException(cfml, "Invalid Syntax Closing [" + quoter + "] not found");

		LitString rtn = f.createLitString(str.toString(), line, cfml.getPosition());
		cfml.removeSpace();
		return rtn;
	}

	/**
	 * Liest ein
	 * 
	 * @return Element
	 * @throws TemplateException
	 */
	public Expression simple(Factory f, SourceCode cfml) throws TemplateException {
		StringBuffer sb = new StringBuffer();
		Position line = cfml.getPosition();
		while (cfml.isValidIndex()) {
			if (cfml.isCurrent(' ') || cfml.isCurrent('>') || cfml.isCurrent("/>")) break;
			else if (cfml.isCurrent('"') || cfml.isCurrent('#') || cfml.isCurrent('\'')) {
				throw new TemplateException(cfml, "simple attribute value can't contain [" + cfml.getCurrent() + "]");
			}
			else sb.append(cfml.getCurrent());
			cfml.next();
		}
		cfml.removeSpace();

		return f.createLitString(sb.toString(), line, cfml.getPosition());
	}

}
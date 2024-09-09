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
 * Implements the CFML Function htmleditformat
 */
package lucee.runtime.functions.displayFormatting;

import lucee.commons.lang.HTMLEntities;
import lucee.runtime.PageContext;
import lucee.runtime.ext.function.Function;
import lucee.runtime.op.Caster;

public final class HTMLEditFormat implements Function {
	private static final long serialVersionUID = 5405529396560613367L;

	public static String call(PageContext pc, String html) {
		return HTMLEntities.escapeHTML(html, HTMLEntities.HTMLV20);
	}

	public static String call(PageContext pc, String html, Number version) {
		double ver = Caster.toDoubleValue(version);
		short v = HTMLEntities.HTMLV20;
		if (ver == 2D) v = HTMLEntities.HTMLV20;
		else if (ver == 3.2D) v = HTMLEntities.HTMLV32;
		else if (ver == 4D) v = HTMLEntities.HTMLV40;
		else if (ver <= 0D) v = HTMLEntities.HTMLV40;

		return HTMLEntities.escapeHTML(html, v);
	}

}
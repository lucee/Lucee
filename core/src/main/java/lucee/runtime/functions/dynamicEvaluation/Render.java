/**
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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
package lucee.runtime.functions.dynamicEvaluation;

import lucee.commons.lang.StringUtil;
import lucee.loader.engine.CFMLEngine;
import lucee.runtime.PageContext;
import lucee.runtime.compiler.Renderer;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.Function;

public final class Render implements Function {

	private static final long serialVersionUID = 669811806780804244L;

	public static String call(PageContext pc, String cfml) throws PageException {
		return Renderer.tag(pc, cfml, pc.getCurrentTemplateDialect(), false, pc.ignoreScopes()).getOutput();
	}

	public static String call(PageContext pc, String cfml, String dialect) throws PageException {
		if (StringUtil.isEmpty(dialect, true)) return call(pc, cfml);
		return Renderer.tag(pc, cfml, ConfigWebUtil.toDialect(dialect.trim(), CFMLEngine.DIALECT_CFML), false, pc.ignoreScopes()).getOutput();
	}

}
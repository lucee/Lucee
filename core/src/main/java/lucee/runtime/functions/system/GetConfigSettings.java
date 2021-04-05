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
package lucee.runtime.functions.system;

import lucee.runtime.PageContext;
import lucee.runtime.config.ConfigPro;
import lucee.runtime.config.ConfigWebUtil;
import lucee.runtime.exp.FunctionException;
import lucee.runtime.exp.PageException;
import lucee.runtime.ext.function.BIF;
import lucee.runtime.op.Caster;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class GetConfigSettings extends BIF {

	public static Struct call(PageContext pc) throws PageException {
		return call(pc, false);
	}

	public static Struct call(PageContext pc, boolean suppressFunctions) throws PageException {
		Struct sct = new StructImpl(Struct.TYPE_LINKED);
		sct.setEL("mode", ConfigWebUtil.toAdminMode(((ConfigPro) pc.getConfig()).getAdminMode(), "single"));

		return sct;
	}

	@Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
		if (args.length == 1) return call(pc, Caster.toBooleanValue(args[0]));
		if (args.length == 0) return call(pc);
		throw new FunctionException(pc, "GetApplicationSettings", 0, 1, args.length);
	}
}
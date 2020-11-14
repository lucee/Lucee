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
package lucee.runtime.engine;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import lucee.commons.lang.ClassUtil;
import lucee.runtime.PageContext;
import lucee.runtime.type.Struct;
import lucee.runtime.type.StructImpl;

public class ExecutionLogFactory {

	private Class clazz;
	private Map<String, String> arguments;
	// private ExecutionLog executionLog;

	public ExecutionLogFactory(Class clazz, Map<String, String> arguments) {
		this.clazz = clazz;
		this.arguments = arguments;
	}

	public ExecutionLog getInstance(PageContext pc) {
		ExecutionLog el;
		try {
			el = (ExecutionLog) ClassUtil.newInstance(clazz);
		}
		catch (Exception e) {
			el = new ConsoleExecutionLog();
		}
		el.init(pc, arguments);
		return el;
	}

	@Override
	public String toString() {
		return super.toString() + ":" + clazz.getName();
	}

	public Class getClazz() {
		return clazz;
	}

	public Struct getArgumentsAsStruct() {
		StructImpl sct = new StructImpl();
		if (arguments != null) {
			Iterator<Entry<String, String>> it = arguments.entrySet().iterator();
			Entry<String, String> e;
			while (it.hasNext()) {
				e = it.next();
				sct.setEL(e.getKey(), e.getValue());
			}
		}
		return sct;
	}
}
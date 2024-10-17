/**
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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
package lucee.runtime.script;

import javax.naming.directory.InvalidAttributesException;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.servlet.ServletException;

import lucee.loader.engine.CFMLEngine;

public class CFMLScriptEngineFactory extends BaseScriptEngineFactory {

	public CFMLScriptEngineFactory() throws ServletException {
		super(false, CFMLEngine.DIALECT_CFML);
	}

	public static void main(String[] args) throws ServletException, ScriptException, InvalidAttributesException {
		try {
			// no args
			if (args.length < 2) {
				System.err.println("you need to provide argumens following this pattern {String <method-name>,String <methodArguments>[,String <methodArguments>]}");
				System.exit(1);
			}

			String methodName = args[0].trim().toLowerCase();
			String arg1 = args[1].trim();

			ScriptEngine eng = new CFMLScriptEngineFactory().getScriptEngine();

			// get
			if ("get".equals(methodName)) {
				Object res = eng.get(arg1);
				if (res != null) System.err.print(res.toString());
			}
			if ("eval".equals(methodName)) {
				Object res = eng.eval(arg1);
				if (res != null) System.err.print(res.toString());
			}
			if ("put".equals(methodName)) {
				if (args.length < 3) {
					System.err.println("you need to provide argumens following this pattern {\"put\",String key>,String <value>}");
					System.exit(1);
				}
				String val = args[2].trim();

				eng.put(arg1, val);
				System.err.println();
			}

		}
		finally {
			System.exit(0);
		}
	}
}
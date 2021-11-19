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
package lucee.runtime.debug;

public class DebugDumpImpl implements DebugDump {

	private final String template;
	private final int line;
	private final String output;

	public DebugDumpImpl(String template, int line, String output) {
		this.template = template;
		this.line = line;
		this.output = output;
	}

	@Override
	public int getLine() {
		return line;
	}

	@Override
	public String getTemplate() {
		return template;
	}

	@Override
	public String getOutput() {
		return output;
	}

}
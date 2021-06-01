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

import lucee.runtime.debug.DebugTimerPro;

public final class DebugTimerImpl implements DebugTimerPro {

	private static final long serialVersionUID = -4552972253450654830L;

	private String label;
	private long time;
	private String template;
	private int line;

	public DebugTimerImpl(String label, long time, String template, int line) {
		this.label = label;
		this.time = time;
		this.template = template;
		this.line = line;
	}

	/**
	 * @return the label
	 */
	@Override
	public String getLabel() {
		return label;
	}

	/**
	 * @return the template
	 */
	@Override
	public String getTemplate() {
		return template;
	}

	/**
	 * @return the time
	 */
	@Override
	public long getTime() {
		return time;
	}

	/**
	 * @return the line number
	*/	
	public int getLine() {
		return line;
	}
}
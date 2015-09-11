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

import lucee.commons.io.SystemUtil.TemplateLine;

public class DebugTextFragment {
	public final String text;
	public final String template;
	public final int line;
	
	public DebugTextFragment(String text, String template, int line){
		this.text=text;
		this.template=template;
		this.line=line;
	}

	public DebugTextFragment(String text, TemplateLine tl) {
		this.text=text;
		this.template=tl.template;
		this.line=tl.line;
	}
}
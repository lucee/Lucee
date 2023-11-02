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
package lucee.runtime.debug;

public class ImplicitAccessImpl implements ImplicitAccess {

	private int count = 1;
	private String scope;
	private String template;
	private int line;
	private String name;

	public ImplicitAccessImpl(String scope, String name, String template, int line) {
		this.scope = scope;
		this.name = name;
		this.template = template;
		this.line = line;
	}

	@Override
	public void inc() {
		count++;
	}

	/**
	 * @return the used
	 */
	@Override
	public int getCount() {
		return count;
	}

	/**
	 * @return the scope
	 */
	@Override
	public String getScope() {
		return scope;
	}

	/**
	 * @return the template
	 */
	@Override
	public String getTemplate() {
		return template;
	}

	/**
	 * @return the line
	 */
	@Override
	public int getLine() {
		return line;
	}

	/**
	 * @return the name
	 */
	@Override
	public String getName() {
		return name;
	}
}
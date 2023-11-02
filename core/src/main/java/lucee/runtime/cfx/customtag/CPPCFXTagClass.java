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
/*
 * Created on Jan 20, 2005
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package lucee.runtime.cfx.customtag;

import com.allaire.cfx.CustomTag;

import lucee.runtime.cfx.CFXTagException;

/**
 *
 *
 * To change the template for this generated type comment go to Window>Preferences>Java>Code
 * Generation>Code and Comments
 */
public final class CPPCFXTagClass implements CFXTagClass {

	private String name;
	private boolean readonly = false;
	private String serverLibrary;
	private String procedure;
	private boolean keepAlive;

	/**
	 * @param name
	 * @param readonly
	 * @param serverLibrary
	 * @param procedure
	 * @param keepAlive
	 */
	private CPPCFXTagClass(String name, boolean readonly, String serverLibrary, String procedure, boolean keepAlive) {
		super();
		this.name = name;
		this.readonly = readonly;
		this.serverLibrary = serverLibrary;
		this.procedure = procedure;
		this.keepAlive = keepAlive;
	}

	public CPPCFXTagClass(String name, String serverLibrary, String procedure, boolean keepAlive) {
		if (name.startsWith("cfx_")) name = name.substring(4);
		this.name = name;
		this.serverLibrary = serverLibrary;
		this.procedure = procedure;
		this.keepAlive = keepAlive;
	}

	/**
	 * @return the serverLibrary
	 */
	public String getServerLibrary() {
		return serverLibrary;
	}

	/**
	 * @return the procedure
	 */
	public String getProcedure() {
		return procedure;
	}

	@Override
	public CustomTag newInstance() throws CFXTagException {
		return new CPPCustomTag(serverLibrary, procedure, keepAlive);

	}

	@Override
	public boolean isReadOnly() {
		return readonly;
	}

	@Override
	public CFXTagClass cloneReadOnly() {
		return new CPPCFXTagClass(name, true, serverLibrary, procedure, keepAlive);
	}

	@Override
	public String getDisplayType() {
		return "cpp";
	}

	@Override
	public String getSourceName() {
		return serverLibrary;
	}

	@Override
	public boolean isValid() {
		return false;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the keepAlive
	 */
	public boolean getKeepAlive() {
		return keepAlive;
	}

}
<!--- 
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
 --->
 component {

	variables.logFileName = "DirectoryWatcher";

	public void function onAdd(required struct data) output=false {
		writeLog( text="File added: #serializeJson(data)#", file=variables.logFileName, type="information" );
	}

	public void function onDelete(required struct data) output=false {
		writeLog( text="File deleted: #serializeJson(data)#", file=variables.logFileName, type="information" );
	}

	public void function onChange(required struct data) output=false {
		writeLog ( text="File changed: #serializeJson(data)#", file=variables.logFileName, type="information" );
	}

}

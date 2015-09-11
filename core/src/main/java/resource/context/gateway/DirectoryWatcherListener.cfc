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
 ---><cfcomponent>
	
	<cffunction name="onAdd" access="public" output="no" returntype="void">
    	<cfargument name="data" type="struct" required="yes">
		<cflog text="add:#serialize(data)#" type="information" file="DirectoryWatcher">
	</cffunction>
	<cffunction name="onDelete" access="public" output="no" returntype="void">
    	<cfargument name="data" type="struct" required="yes">
		<cflog text="delete:#serialize(data)#" type="information" file="DirectoryWatcher">
	</cffunction>
	<cffunction name="onChange" access="public" output="no" returntype="void">
    	<cfargument name="data" type="struct" required="yes">
		<cflog text="change:#serialize(data)#" type="information" file="DirectoryWatcher">
	</cffunction>

</cfcomponent>
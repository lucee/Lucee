<!--- 
 *
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
 ---><cfcomponent extends="org.lucee.cfml.test.LuceeTestCase">
	<!---
	<cffunction name="beforeTests"></cffunction>
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	<cffunction name="testRelpath" localMode="modern">
		<cfscript>

			local.path={};
			path.abs=GetDirectoryFromPath(GetCurrentTemplatePath());
			path.real="../"& ListLast(path.abs,"/\");
			assertEquals(false,evaluate('directoryExists(path.real,false)'));
			assertEquals(true,evaluate('directoryExists(path.real,true)'));
		</cfscript>
	</cffunction>
	
	<cffunction name="testExistingDirectory" localMode="modern">
		<cfscript>
			local.path={};
			path.abs=GetDirectoryFromPath(GetCurrentTemplatePath());
			path.real="../"& ListLast(path.abs,"/\");

			assertEquals(true,directoryExists(path.abs));
			assertEquals(true,directoryExists(path.real));
		</cfscript>
	</cffunction>

	<cffunction name="testDirectoryNotExists" localMode="modern">
		<cfscript>
			assertEquals(false,DirectoryExists("/does/not/exist/"));
		</cfscript>
	</cffunction>
	
</cfcomponent>
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
	<cffunction name="testDirectoryExists" localMode="modern">

<!--- begin old test code --->
<cfset valueEquals(left="#DirectoryExists("/Users")#", right="#true#")>
<cfset valueEquals(left="#DirectoryExists("/users")#", right="#true#")>
<cfset valueEquals(left="#DirectoryExists("/Users/susi")#", right="#false#")>
<cfset valueEquals(left="#DirectoryExists("/Users/peter/temp")#", right="#false#")>

<cfset path=structNew()>
<cfset path.abs=GetDirectoryFromPath(GetCurrentTemplatePath())>
<cfset path.real="../"& ListLast(path.abs,"/\")>

<cfset valueEquals(left="#directoryExists(path.abs)#", right="true")>

<cfset valueEquals(left="#directoryExists(path.real)#", right="true")>

<cfif server.ColdFusion.ProductName EQ "Lucee">
    <cfset valueEquals(left="#evaluate('directoryExists(path.real,false)')#", right="false")>
    <cfset valueEquals(left="#evaluate('directoryExists(path.real,true)')#", right="true")>
</cfif>
<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
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
	<cffunction name="testArrayIndexExists">
		<cfif server.ColdFusion.ProductName EQ "lucee">
			<cfset var a=array(1)>
			<cfset var a[3]=1>
			<cfset valueEquals(left="#ArrayIndexExists(array("a","b","c","d"),2)#", right="#true#")>
			<cfset valueEquals(left="#ArrayIndexExists(array("a","b","c","d"),5)#", right="#false#")>
			<cfset valueEquals(left="#ArrayIndexExists(a,2)#", right="#false#")>
			<cfset valueEquals(left="#ArrayIndexExists(a,3)#", right="#true#")>
		</cfif>
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
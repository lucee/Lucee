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
	<cffunction name="testBeat" localMode="modern">

		<cfset valueEquals(left="#beat(createDateTime(2000,1,1,12,0,0,0,"CET"))#", right="500")>
		<cfset valueEquals(left="#beat() GTE 0#", right="true")>
		<cfset valueEquals(left="#beat(parseDateTime('01/01/2001 12:00:00+0'))#", right="541.666")>
		<cfset valueEquals(left="#beat(parseDateTime('01/01/2001 12:00:00+1'))#", right="500")>
		<cfset valueEquals(left="#beat(parseDateTime('30/06/2001 12:00:00+1'))#", right="500")>
		<cfset valueEquals(left="#beat(parseDateTime('01/01/2001 12:00:00+2'))#", right="458.333")>
		<cfset valueEquals(left="#beat(parseDateTime('01/01/2001 12:00:00+3'))#", right="416.666")>
		<cfset valueEquals(left="#beat(parseDateTime('01/01/2001 12:00:00+4'))#", right="375")>


<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
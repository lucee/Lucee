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
	<cffunction name="testCJustify" localMode="modern">

<!--- begin old test code --->
<cfset valueEquals(left="#Cjustify("abc",1)#", right="abc")>
<cfset valueEquals(left="#Cjustify("abc",4)#", right="abc ")>
<cfset valueEquals(left="#Cjustify("abc",5)#", right=" abc ")>
<cfset valueEquals(left="#Cjustify("abc",6)#", right=" abc  ")>
<cftry>
	<cfset valueEquals(left="#Cjustify("abc",0)#", right="abc")>
	<cfset fail("must throw:Cjustify('abc',0)")>
	<cfcatch></cfcatch>
</cftry>
<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
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

	<cffunction name="testCreateDateOptionalArgs" localMode="modern">
		<cfset assertEquals("{ts '2000-12-01 00:00:00'}x","#CreateDate(2000, 12)#x")> 
		<cfset assertEquals("{ts '2000-01-01 00:00:00'}x","#CreateDate(2000)#x")>  
		<cfset assertEquals("{ts '2000-01-03 00:00:00'}x","#CreateDate(year:2000,day:03)#x")> 
	</cffunction>

	<cffunction name="testCreateDate" localMode="modern">

<!--- begin old test code --->
<cfset valueEquals(left="#CreateDate(2000, 12, 1)#x", right="{ts '2000-12-01 00:00:00'}x")> 
<cfset valueEquals(
	left="{d '1899-12-31'}x",
	right="#CreateODBCDate(1)#x")>
	
	
	
<cfset d = CreateDate(2007,11,30)>
<cfset d1 = d - 0>
<cfset valueEquals(left="#d1#", right="39416")>

<cfset d = CreateDate(2007,5,1)>
<cfset d1 = d - 0>
<cfset valueEquals(left="#round(d1)#", right="39203")>
<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
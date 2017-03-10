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

	<cffunction name="testCreateDateTimeOptionalArgs" localMode="modern">
		<cfset assertEquals("{ts '2000-12-11 00:00:00'}x","#CreateDateTime(2000, 12, 11)#x")> 
		<cfset assertEquals("{ts '2000-12-01 00:00:00'}x","#CreateDateTime(2000, 12)#x")> 
		<cfset assertEquals("{ts '2000-01-01 00:00:00'}x","#CreateDateTime(2000)#x")> 
		<cfset assertEquals("{ts '2000-01-23 00:05:00'}x","#CreateDateTime(year:2000,day:23,minute:5)#x")> 
	</cffunction>


	<cffunction name="testCreateDateTime" localMode="modern">

<!--- begin old test code --->
<cfset valueEquals(left="#CreateDateTime(2000, 12, 1,12,11,10)#x", right="{ts '2000-12-01 12:11:10'}x")> 

<cfset valueEquals(
	left="{ts '1899-12-31 00:00:00'}x" ,
	right="#CreateODBCDateTime(1)#x")>

<cfset valueEquals(
	left="{ts '1899-12-31 02:24:00'}x" ,
	right="#CreateODBCDateTime(1.1)#x")>


<cfset valueEquals( 
	left="{ts '1899-12-31 02:38:24'}x" ,
	right="#CreateODBCDateTime(1.11)#x")>
<cfset valueEquals(
	left="{ts '1899-12-31 02:39:50'}x" ,
	right="#CreateODBCDateTime(1.111)#x")>
<cfset valueEquals(
	left="{ts '1899-12-31 02:39:59'}x" ,
	right="#CreateODBCDateTime(1.1111)#x")>
<cfset valueEquals( 
	left="{ts '1899-12-31 02:39:59'}x" ,
	right="#CreateODBCDateTime(1.11111)#x")>
    
    
<!--- has not really something to do with createDateTime but with date/calendar objects in general --->
<cfset tz = createObject('java','java.util.TimeZone').getTimeZone("America/Mexico_City")>
<cfset c = createObject('java','java.util.Calendar').getInstance()>
<cfset c.setTimeZone(tz)>
<cfset dt=CreateDateTime(2000,1,1,1,1,1)>
<cfset c.setTime(dt)>

<cfset valueEquals(left="#c#x", right="#dt#x")>
<cfset valueEquals(left="#c#x", right="#dt#x")>
<cfset valueEquals(left="#c#x", right="#dt&""#x")>
<cfset valueEquals(left="#c EQ dt#", right="#true#")>
<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
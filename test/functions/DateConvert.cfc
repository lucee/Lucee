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
	
	<cffunction name="testDateConvert" localMode="modern">
<!--- no longer supported with Lucee 5
<cfset valueEquals(left="{ts '2006-01-26 00:00:00'}x", right="#DateConvert("Local2utc", "{ts '2006-01-26 01:00:00'}")#x")>
<cfset valueEquals(left="{ts '2006-07-26 00:00:00'}x", right="#DateConvert("Local2utc", "{ts '2006-07-26 02:00:00'}")#x")>
<cfset valueEquals(left="{ts '2006-03-26 03:00:00'}x", right="#DateConvert("Local2utc", "{ts '2006-03-26 05:00:00'}")#x")>
<cfset valueEquals(left="{ts '2006-03-26 03:00:00'}x", right="#DateConvert("Local2utc", "{ts '2006-03-26 05:00:00'}")#x")>
<cfset valueEquals(left="{ts '2006-03-26 00:59:59'}x", right="#DateConvert("Local2utc", "{ts '2006-03-26 01:59:59'}")#x")>
<cfset valueEquals(left="{ts '2006-03-26 00:00:00'}x", right="#DateConvert("Local2utc", "{ts '2006-03-26 01:00:00'}")#x")>
<cfset valueEquals(left="{ts '2006-01-26 01:00:00'}", right="#DateConvert("Local2utc", "{ts '2006-01-26 02:00:00'}")#")>
<cfset valueEquals(left="{ts '2006-03-26 00:59:00'}", right="#DateConvert("Local2utc", "{ts '2006-03-26 01:59:00'}")#")>
<cfset valueEquals(left="{ts '2006-03-26 03:59:00'}", right="#DateConvert("utc2local", "{ts '2006-03-26 01:59:00'}")#")>
<cfset valueEquals(left="{ts '2006-03-26 05:00:00'}", right="#DateConvert("utc2local", "{ts '2006-03-26 02:00:00'}")#")>
--->
	
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
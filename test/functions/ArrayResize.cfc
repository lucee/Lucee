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
	<cffunction name="testArrayResize" localMode="modern">

<!--- begin old test code --->
<cfset arr=arrayNew(1)>
<cfset valueEquals(left="#arrayLen(arr)#", right="0")>
<cfset ArrayResize(arr, 20)>
<cfset valueEquals(left="#arrayLen(arr)#", right="20")>
<cfset ArrayResize(arr, 10)>
<cfset valueEquals(left="#arrayLen(arr)#", right="20")>
 
<cfset arr=arrayNew(1)>
<cfset arr[2]=2>
<cfset arr[4]=4>
<cfset ArrayResize(arr, 10)>
<cfset valueEquals(left="#arrayLen(arr)#", right="10")>
<cfset valueEquals(left="#arr[2]#", right="2")>
<cfset valueEquals(left="#arr[4]#", right="4")>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
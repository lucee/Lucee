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
	<cffunction name="testArrayLen">

<!--- begin old test code --->
<cfset var arr=arrayNew(1)>
<cfset valueEquals(left="#arrayLen(arr)#", right="0")>
<cfset ArrayAppend( arr, 1 )>
<cfset valueEquals(left="#arrayLen(arr)#", right="1")>
<cfset arr[9]=9>
<cfset valueEquals(left="#arrayLen(arr)#", right="9")>
<cfset ArrayResize(arr, 20)>
<cfset valueEquals(left="#arrayLen(arr)#", right="20")>

<cfset arr=arrayNew(2)>
<cfset arr[1][1]=11>
<cfset arr[1][2]=12>
<cfset arr[1][3]=13>
<cfset arr[2][1]=21>
<cfset arr[2][2]=22>
<cfset arr[2][3]=23>

<cfset valueEquals(left="#arrayLen(arr)#", right="2")>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
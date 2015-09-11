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
	<cffunction name="testArrayPrepend" localMode="modern">

<!--- begin old test code --->
<cfset arr=arrayNew(1)>
<cfset arr[1]=1>
<cfset arr[2]=2>
<cfset ArrayPrepend( arr, 'a' )>
<cfset ArrayPrepend( arr, 'b' )>
<cfset valueEquals(left="#arr[1]#", right="b")>
<cfset valueEquals(left="#arr[2]#", right="a")>
<cfset valueEquals(left="#arr[3]#", right="1")>
<cfset valueEquals(left="#arr[4]#", right="2")>
<cfset valueEquals(left="#arrayLen(arr)#", right="4")>

<cfset arr=arrayNew(1)>
<cfset arr[20]=20>
<cfset ArrayPrepend( arr, 'a' )>
<cfset ArrayPrepend( arr, 'b' )>
<cfset valueEquals(left="#arr[1]#", right="b")>
<cfset valueEquals(left="#arr[2]#", right="a")>
<cfset valueEquals(left="#arr[22]#", right="20")>
<cfset valueEquals(left="#arrayLen(arr)#", right="22")>
 
<cfset arr=arrayNew(1)>
<cfset arr[2]=2>
<cfset arr[4]=4>
<cfset ArrayPrepend( arr, 'a' )>
<cfset ArrayPrepend( arr, 'b' )>
<cfset valueEquals(left="#arr[1]#", right="b")>
<cfset valueEquals(left="#arr[2]#", right="a")>
<cfset valueEquals(left="#arr[4]#", right="2")>
<cfset valueEquals(left="#arr[6]#", right="4")>
<cftry>
<cfset valueEquals(left="#arr[5]#", right="null")>
	<cfset fail("must throw:Array at position 5 is empty")>
	<cfcatch></cfcatch>
</cftry>
<cfset valueEquals(left="#arrayLen(arr)#", right="6")>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
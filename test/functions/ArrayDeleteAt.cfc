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
	<cffunction name="testArrayDeleteAt">
		
<cfset var arr=arrayNew(1)>
<cfset ArrayAppend( arr, 1 )>
<cfset ArrayAppend( arr, 2 )>
<cfset ArrayAppend( arr, 3 )>
<cfset ArrayDeleteAt( arr, 1 )>

<cfset valueEquals( left="#arrayLen(arr)#", right="2")>
<cfset valueEquals(left="#arr[1]#", right="2")>
<cfset valueEquals(left="#arr[2]#", right="3")>

<cfset ArrayDeleteAt( arr, 1 )>
<cfset valueEquals(left="#arrayLen(arr)#", right="1")>
<cfset valueEquals(left="#arr[1]#", right="3")>

<cftry>
	<cfset ArrayDeleteAt( arr, 10)>
	<cfset fail("must throw:Cannot insert/delete at position 10.")>
	<cfcatch></cfcatch>
</cftry>

<cfset arr=arrayNew(1)>
<cfset arr[1]=1>
<cfset arr[2]=1>
<cfset arr[3]=1>
<cfset arr[7]=7>
<cfset ArrayDeleteAt( arr, 3 )>
<cfset valueEquals(left="#arr[1]#", right="1")>
<cfset valueEquals(left="#arr[2]#", right="1")>
<cfset valueEquals(left="#arr[6]#", right="7")>
<cfset valueEquals(left="#arrayLen(arr)#", right="6")>

<cftry>
	<cfset test=arr[3]>
	<cfset fail("must throw:Element 3 is undefined in a Java object of type class coldfusion.runtime.Array referenced as ")>
	<cfcatch></cfcatch>
</cftry>
<cfset valueEquals(left="#arr[6]#", right="7")>

<cfset arr=arrayNew(1)>
<cfset arr[1]=1>
<cfset arr[2]=1>
<cfset arr[3]=1>
<cfset arr[7]=7>
<cfset ArrayDeleteAt( arr, 4 )>
<cfset valueEquals(left="#arr[1]#", right="1")>
<cfset valueEquals(left="#arr[2]#", right="1")>
<cfset valueEquals(left="#arr[3]#", right="1")>
<cfset valueEquals(left="#arr[6]#", right="7")>
<cfset valueEquals(left="#arrayLen(arr)#", right="6")>
		
		
		
		
		
	</cffunction>
	
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
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
	<cffunction name="testArrayMax">

<!--- begin old test code --->
<cfset var arr=arrayNew(1)>
<cfset ArrayAppend( arr, 1 )>
<cfset ArrayAppend( arr, 2 )>
<cfset ArrayAppend( arr, 3 )>
<cfset valueEquals(left="#ArrayMax(arr)#", right="3")>

<cfset arr=arrayNew(1)>
<cfset ArrayAppend( arr, 1 )>
<cfset ArrayAppend( arr, 2.5 )>
<cfset ArrayAppend( arr, 33.5 )>
<cfset valueEquals(left="#ArrayMax(arr)#", right="33.5")>

<cfset arr=arrayNew(1)>
<cfset ArrayAppend( arr, 1 )>
<cfset ArrayAppend( arr, true )>
<cfset ArrayAppend( arr, 2 )>
<cfset valueEquals(left="#ArrayMax(arr)#", right="2")>

<cfset arr=arrayNew(1)>
<cfset ArrayAppend( arr, 1 )>
<cfset ArrayAppend( arr, "hans" )>
<cfset ArrayAppend( arr, 2 )>
<cftry>
	<cfset valueEquals(left="#ArrayMax(arr)#", right="2")>
	<cfset fail("must throw:Non-numeric value found")>
	<cfcatch></cfcatch>
</cftry>

<cfset arr=arrayNew(1)>
<cfset arr[3]=0>
<cfset ArrayAppend( arr, 1 )>
<cfset ArrayAppend( arr, 2 )>
<cftry>
	<cfset valueEquals(left="#ArrayMax(arr)#", right="2")>
	<cfset fail("must throw:Non-numeric value found")>
	<cfcatch></cfcatch>
</cftry>

<cfset arr=arrayNew(2)>
<cfset arr[1][1]=1>
<cfset arr[1][2]=2>
<cfset arr[1][3]=3>
<cftry>
	<cfset valueEquals(left="#ArrayMax(arr)#", right="3")>
	<cfset fail("must throw:The array passed cannot contain more than one dimension.")>
	<cfcatch></cfcatch>
</cftry>

<cfscript> 
myNumberArray = []; 
myNumberArray[1]	= -1; 
myNumberArray[2]	= -100; 
myNumberArray[3]	= -200; 
</cfscript> 

<cfset valueEquals(left="#ArrayMax(myNumberArray)#", right="-1")>
<cfset valueEquals(left="#ArrayMax(arrayNew(1))#", right="0")>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
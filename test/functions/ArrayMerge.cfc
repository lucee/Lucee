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
	<cffunction name="testArrayMerge">

<!--- begin old test code --->
<cfif server.ColdFusion.ProductName EQ "lucee">
<cfset var arr1=arrayNew(1)>
<cfset ArrayAppend( arr1, 1 )>
<cfset ArrayAppend( arr1, 2 )>
<cfset ArrayAppend( arr1, 3 )>

<cfset var arr2=arrayNew(1)>
<cfset ArrayAppend( arr2, 4 )>
<cfset ArrayAppend( arr2, 5 )>
<cfset ArrayAppend( arr2, 6 )>

<cfset var arr=arrayMerge(arr1,arr2)>
<cfset valueEquals(left="#arrayLen(arr)#", right="6")>
<cfset valueEquals(left="#arr[1]#", right="1")>
<cfset valueEquals(left="#arr[2]#", right="2")>
<cfset valueEquals(left="#arr[3]#", right="3")>
<cfset valueEquals(left="#arr[4]#", right="4")>
<cfset valueEquals(left="#arr[5]#", right="5")>
<cfset valueEquals(left="#arr[6]#", right="6")>


<cfset arr=arrayMerge(arr1,arr2,true)>
<cfset valueEquals(left="#arrayLen(arr)#", right="3")>
<cfset valueEquals(left="#arr[1]#", right="1")>
<cfset valueEquals(left="#arr[2]#", right="2")>
<cfset valueEquals(left="#arr[3]#", right="3")>

</cfif>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
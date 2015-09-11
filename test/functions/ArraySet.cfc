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
	<cffunction name="testArraySet" localMode="modern">

<!--- begin old test code --->
<cfset arr=arrayNew(1)>
<cfset ArraySet(arr, 3, 5, "val")>
<cfset valueEquals(left="#arrayLen(arr)#", right="5")>
<cfset valueEquals(left="#arr[3]#", right="val")>
<cfset valueEquals(left="#arr[4]#", right="val")>
<cfset valueEquals(left="#arr[5]#", right="val")>
<cftry>
<cfset valueEquals(left="#arr[2]#", right="val")>
	<cfset fail("must throw:Array at position 2 is empty")>
	<cfcatch></cfcatch>
</cftry>

<cfset arr=arrayNew(1)>
<cfset sub=arrayNew(1)>
<cfset ArraySet(arr, 3, 5, sub)>
<cfset sub[1]=1>
<!---
@todo clone dont work --->
<cfset valueEquals(left="#arrayLen(arr[3])#", right="0")>
<cfset valueEquals(left="#arrayLen(arr[4])#", right="0")>
<cfset valueEquals(left="#arrayLen(arr[5])#", right="0")>
<cfset arr[3][1]=1>
<cfset arr[4][2]=1>
<cfset arr[5][3]=1>
<!--- 
@todo clone dont work --->
<cfset valueEquals(left="#arrayLen(arr[3])#", right="1")>
<cfset valueEquals(left="#arrayLen(arr[4])#", right="2")>
<cfset valueEquals(left="#arrayLen(arr[5])#", right="3")>

<cfset arr=arrayNew(2)>
<cftry>
<cfset ArraySet(arr, 3, 5, "")>
	<cfset fail("must throw:Array dimension error. ")>
	<cfcatch></cfcatch>
</cftry>

<cfset arr=arrayNew(1)>
<cfset arr[3]=3>
<cfset ArraySet(arr, 1, 3, "")>
<cftry>
	<cfset ArraySet(arr, 3, 1, "")>
	<cfset fail("must throw:3 is not greater than zero or less than or equal to 1 The range passed to ArraySet must begin with a number greater than zero and less than or equal to the second number ")>
	<cfcatch></cfcatch>
</cftry>
<cftry>
	<cfset ArraySet(arr, -3, 1, "")>
	<cfset fail("must throw:3 is not greater than zero or less than or equal to 1 The range passed to ArraySet must begin with a number greater than zero and less than or equal to the second number ")>
	<cfcatch></cfcatch>
</cftry>

<cfset ax=arrayNew(1)>
<cfset ax[1]=1>
<cfset ax[2]=2>
<cfset ax[4]=4>
<cfset inner=arrayNew(1)>
<cfset inner[1]=1>
<cfset ArraySet(ax,1,3,inner)>
<cfset inner[1]=2>
<cfset valueEquals(left="#ax[1][1]#", right="1")>
<cfset ax[2][1]=3>
<cfset valueEquals(left="#ax[1][1]#", right="1")>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
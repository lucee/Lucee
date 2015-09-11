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
	<cffunction name="testArrayToList" localMode="modern">

<!--- begin old test code --->
<cfset arr=arrayNew(1)> 
<cfset arr[1]=111>
<cfset arr[2]=22>
<cfset arr[3]=3.5>

<cfset valueEquals(left="#arrayToList(arr)#", right="111,22,3.5")>
<cfset valueEquals(left="#arrayToList(arr,'')#", right="111223.5")>
<cfset valueEquals(left="#arrayToList(arr,',;')#", right="111,;22,;3.5")>

<cfset arr[6]="ee">
<cfset valueEquals(left="#arrayToList(arr)#", right="111,22,3.5,,,ee")>

<cfset arr[7]="e,e">
<cfset valueEquals(left="#arrayToList(arr)#", right="111,22,3.5,,,ee,e,e")>


<cfset valueEquals(left="#arrayToList(arr,";")#", right="111;22;3.5;;;ee;e,e")>

<cfset arr=arrayNew(1)>
<cfset arr[1]="a">

<cfset ArrayResize(arr, 10)>
<cfset valueEquals(left="#arrayToList(arr)#", right="a,,,,,,,,,")>

<cfset arr=arrayNew(1)>
<cfset arr[1]="a">
<cfset arr[2]="b">

<cfset valueEquals(left="#arrayToList(arr,"{}")#", right="a{}b")>

<cfset arr=arrayNew(1)>
<cfset arr[4]=111>
<cfset arr[5]=22>
<cfset arr[6]=3.5>

<cfset valueEquals(left="#arrayToList(arr)#", right=",,,111,22,3.5")>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
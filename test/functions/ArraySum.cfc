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
	<cffunction name="testArraySum" localMode="modern">

<!--- begin old test code --->
<cfset arr=arrayNew(1)>
<cfset arr[1]=111>
<cfset arr[2]=22>
<cfset arr[3]=3.5>

<cfset valueEquals(left="#arraySum(arr)#", right="136.5")>

<cfset arr[4]="susi">
<cftry>
	<cfset valueEquals(left="#arraySum(arr)#", right="136.5")>
	<cfset fail("must throw:Non-numeric value found.")>
	<cfcatch></cfcatch>
</cftry>
 
<cfset arr=arrayNew(1)>
<cfset valueEquals(left="#arraySum(arr)#", right="0")>

<cfset arr=arrayNew(2)>

<cftry>
	<cfset valueEquals(left="#arraySum(arr)#", right="0")>
	<cfset fail("must throw:The array passed cannot contain more than one dimension. ")>
	<cfcatch></cfcatch>
</cftry>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
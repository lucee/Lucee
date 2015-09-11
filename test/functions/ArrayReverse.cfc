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
	<cffunction name="testArrayReverse" localMode="modern">
 	
<!--- begin old test code --->
<cfif server.ColdFusion.ProductName EQ "lucee">
<cfset arr1=arrayNew(1)>
<cfset ArrayAppend( arr1, 1 )>
<cfset ArrayAppend( arr1, 2 )>
<cfset ArrayAppend( arr1, 3 )>
<cfset arr1[6]="6">

<cfset arr=arrayReverse(arr1)>
	<cfset assertEquals(6,arrayLen(arr))>
	<cfset assertEquals(6,arr[1])>
	<cfset assertEquals(3,arr[4])>
	<cfset assertEquals(2,arr[5])>
	<cfset assertEquals(1,arr[6])>
</cfif>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
</cfcomponent>
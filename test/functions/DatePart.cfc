<!--- 
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
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

	<cffunction name="testDatePartMember" localMode="modern">
		<cfset d1=CreateDateTime(2001, 12, 1, 4, 10, 1)> 
		<cfset valueEquals(left="#d1.part("m")#", right="12")>
	</cffunction>


	<cffunction name="testDatePart" localMode="modern">

<!--- begin old test code --->
<cfset d1=CreateDateTime(2001, 12, 1, 4, 10, 1)> 
<cfset valueEquals(left="#datePart("m",d1)#", right="12")>

<cfset valueEquals(
	left="1899" ,
	right="#DatePart("yyyy", 1)#")>
    
<cfset valueEquals(left="7", right="#DatePart("w", d1)#")>
<cfset valueEquals(left="48", right="#DatePart("ww", d1)#")>
<cfset valueEquals(left="4", right="#DatePart("q", d1)#")>
<cfset valueEquals(left="12", right="#DatePart("m", d1)#")>
<cfset valueEquals(left="335", right="#DatePart("y", d1)#")>
<cfset valueEquals(left="1", right="#DatePart("d", d1)#")>
<cfset valueEquals(left="4", right="#DatePart("h", d1)#")>
<cfset valueEquals(left="10", right="#DatePart("n", d1)#")>
<cfset valueEquals(left="1", right="#DatePart("s", d1)#")>
<cfset valueEquals(left="0", right="#DatePart("l", d1)#")>
<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
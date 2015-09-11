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
	<cffunction name="testCreateODBCDate" localMode="modern">

<!--- begin old test code --->
<cfset fixDate=CreateDateTime(2001, 11, 1, 4, 10, 4)> 

<cfset assertEquals("{d '2001-11-01'}",CreateODBCDate(fixDate))>
<cfset assertEquals("{d '2001-11-01'}",toString(CreateODBCDate(fixDate)))>
<cfset assertEquals(0,hour(CreateODBCDate(fixDate)))>
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
</cfcomponent>
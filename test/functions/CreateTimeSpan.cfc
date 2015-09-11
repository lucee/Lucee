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
	<cffunction name="testCreateTimeSpan" localMode="modern">

<!--- begin old test code --->
<cfset valueEquals(left="#CreateTimeSpan(1, 1, 1, 1)#:" ,right="1.042372685185:")> 
<!--- <cfset valueEquals(left="#timeFormat(CreateTimeSpan(0,0,30,0),"hh:mm:ss")#", right="12:30:00")> 
<cfset valueEquals(left="#dateFormat(CreateTimeSpan(0,0,30,0),"dd.mm.yyyy")#", right="30.12.1899")> 

<cfapplication sessiontimeout="{ts '2005-08-29 01:54:20'}" sessionmanagement="yes"> --->

<!--- 
<cffunction name="toNumeric">
	<cfargument name="value" type="numeric">
	<cfdump var="#value+0#">
</cffunction>
<cffunction name="toTS">
	<cfargument name="value" type="timespan">
	<cfdump var="#value#">
</cffunction>
<cfset toNumeric(CreateTimeSpan(0,0,30,0))>
<cfset toNumeric(now())>
<cfset toTS(0.02083333333333333)>
<cfset toTS(38593.40849148148)> --->

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
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
	<cffunction name="testDateCompare" localMode="modern">

<!--- begin old test code --->
<cfset valueEquals(left="#DateCompare("{ts '2007-10-10 00:00:00'}","{ts '2007-10-09 23:59:59'}","d")#", right="1")>
	
<cfset valueEquals(left="#DateCompare("{ts '2007-10-10 00:00:00'}","{ts '2007-10-09 23:59:59'}","h")#", right="1")>
	
<cfset valueEquals(left="#DateCompare("{ts '2007-10-10 00:00:00'}","{ts '2007-10-09 23:59:59'}","n")#", right="1")>
	
<cfset valueEquals(left="#DateCompare("{ts '2007-10-10 00:00:00'}","{ts '2007-10-09 23:59:59'}","m")#", right="0")>
	
<cfset valueEquals(left="#DateCompare("{ts '2007-10-10 00:00:00'}","{ts '2007-10-09 23:59:59'}","s")#", right="1")>
	
<cfset valueEquals(left="#DateCompare("{ts '2007-10-10 00:00:00'}","{ts '2007-10-09 00:00:00'}","d")#", right="1")>
	
<cfset valueEquals(left="#DateCompare("{ts '2007-10-09 00:00:00'}","{ts '2007-10-10 00:00:00'}","d")#", right="-1")>
<cfset valueEquals(left="-1", right="#dateCompare(1,2)#")>



<cfset d1=CreateDateTime(2001, 11, 1, 4, 10, 1)> 
<cfset d2=CreateDateTime(2001, 11, 1, 4, 10, 4)> 
<cfset valueEquals(left="#DateCompare(d1, d2)#", right="-1")> 
<cfset valueEquals(left="#DateCompare(d1, d2,"s")#", right="-1")> 
<cfset valueEquals(left="#DateCompare(d1, d2,"n")#", right="0")> 
<cfset valueEquals(left="#DateCompare(d1, d2,"h")#", right="0")> 
<cfset valueEquals(left="#DateCompare(d1, d2,"yyyy")#", right="0")> 
<cfset d2=CreateDateTime(2001, 11, 1, 5, 10, 4)> 
<cfset valueEquals(left="#DateCompare(d1, d2,"m")#", right="0")> 
<cfset valueEquals(left="#DateCompare(d1, d2,"d")#", right="0")> 
<cfset valueEquals(left="#DateCompare(d1, d2,"d")#", right="0")> 
<cftry> 
        <cfset valueEquals(left="#DateCompare(d1, d2,"w")#", right="0")> 
        <cfset fail("must throw:DateCompare w")> 
        <cfcatch></cfcatch> 
</cftry> 
<cftry> 
        <cfset valueEquals(left="#DateCompare(d1, d2,"ww")#", right="0")> 
        <cfset fail("must throw:DateCompare ww")> 
        <cfcatch></cfcatch> 
</cftry> 
<cftry> 
        <cfset valueEquals(left="#DateCompare(d1, d2,"q")#", right="0")> 
        <cfset fail("must throw:DateCompare q")> 
        <cfcatch></cfcatch> 
</cftry> 
<cftry> 
        <cfset valueEquals(left="#DateCompare(d1, d2,"susi")#", right="0")> 
        <cfset fail("must throw:DateCompare susi")> 
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
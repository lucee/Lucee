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
	<cffunction name="testDateTimeFormat" localMode="modern">

	<cfset d=CreateDateTime(2000,1,2,3,4,5,0,"CET")>
	<cfapplication action="update" timezone="CET"/>
	<cfset valueEquals(left="#DateTimeFormat(d, "yyyy.MM.dd G 'at' HH:nn:ss z")#", right="2000.01.02 AD at 03:04:05 CET")>


<cfset valueEquals(left="#DateTimeFormat(d)#", right="02-Jan-2000 03:04:05")>
<cfset valueEquals(left="#DateTimeFormat(d, "yyyy.MM.dd G 'at' HH:nn:ss z")#", right="2000.01.02 AD at 03:04:05 CET")>
<cfset valueEquals(left="#DateTimeFormat(d, "EEE, MMM d, ''yy")#", right="Sun, Jan 2, '00")>
<cfset valueEquals(left="#DateTimeFormat(d, "h:nn a")#", right="3:04 AM")>
<cfset valueEquals(left="#DateTimeFormat(d, "hh 'o''clock' a, zzzz")#", right="03 o'clock AM, Central European Time")>
<cfset valueEquals(left="#DateTimeFormat(d, "K:nn a, z")#", right="3:04 AM, CET")>
<cfset valueEquals(left="#DateTimeFormat(d, "yyyyy.MMMMM.dd GGG hh:nn aaa")#", right="02000.January.02 AD 03:04 AM")>
<cfset valueEquals(left="#DateTimeFormat(d, "EEE, d MMM yyyy HH:nn:ss Z")#", right="Sun, 2 Jan 2000 03:04:05 +0100")>
<cfset valueEquals(left="#DateTimeFormat(d, "yyMMddHHnnssZ", "GMT")#", right="000102020405+0000")>
<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
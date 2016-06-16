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

	<cffunction name="testDateAddMember" localMode="modern">
		<cfset fixDate=CreateDateTime(2001, 11, 1, 4, 10, 4)> 
		<cfset valueEquals(left="#fixDate.Add("yyyy", 1)#", right="{ts '2002-11-01 04:10:04'}")> 
		<cfset valueEquals(left="#fixDate.Add("yyyy", 10)#", right="{ts '2011-11-01 04:10:04'}")> 
		<cfset valueEquals(left="#fixDate.Add("yyyy", 123456789)#", right="{ts '123458790-11-01 04:10:04'}")> 

	</cffunction>

	<cffunction name="testDateAdd" localMode="modern">
		<cfset setTimeZone('Europe/Berlin')>
<!--- begin old test code --->
<cfset fixDate=CreateDateTime(2001, 11, 1, 4, 10, 4)> 
<cfset valueEquals(left="#DateAdd("yyyy", 1, fixDate)#", right="{ts '2002-11-01 04:10:04'}")> 
<cfset valueEquals(left="#DateAdd("yyyy", 10, fixDate)#", right="{ts '2011-11-01 04:10:04'}")> 
<cfset valueEquals(left="#DateAdd("yyyy", 123456789, fixDate)#", right="{ts '123458790-11-01 04:10:04'}")> 

<cfset valueEquals(left="#DateAdd("q", 1, fixDate)#", right="{ts '2002-02-01 04:10:04'}")> 
<cfset valueEquals(left="#DateAdd("q", 10, fixDate)#", right="{ts '2004-05-01 04:10:04'}")> 
<cfset valueEquals(left="#DateAdd("q", 123456789, fixDate)#", right="{ts '30866199-02-01 04:10:04'}")> 

<cfset valueEquals(left="#DateAdd("m", 1, fixDate)#", right="{ts '2001-12-01 04:10:04'}")>
<cfset valueEquals(left="#DateAdd("m", 10, fixDate)#", right="{ts '2002-09-01 04:10:04'}")>
<cfset valueEquals(left="#DateAdd("m", 123456789, fixDate)#", right="{ts '10290067-08-01 04:10:04'}")>
 
<cfset valueEquals(left="#DateAdd("y", 1, fixDate)#", right="{ts '2001-11-02 04:10:04'}")> 
<cfset valueEquals(left="#DateAdd("y", 10, fixDate)#", right="{ts '2001-11-11 04:10:04'}")> 
<cfset valueEquals(left="#DateAdd("y", 123456789, fixDate)#", right="{ts '340015-01-16 04:10:04'}")> 

<cfset valueEquals(left="#DateAdd("d", 1, fixDate)#", right="{ts '2001-11-02 04:10:04'}")> 
<cfset valueEquals(left="#DateAdd("d", 10, fixDate)#", right="{ts '2001-11-11 04:10:04'}")> 
<cfset valueEquals(left="#DateAdd("d", 1123456789, fixDate)#", right="{ts '3077922-01-19 04:10:04'}")> 

<cfset valueEquals(left="#DateAdd("w", 1, fixDate)#", right="{ts '2001-11-02 04:10:04'}")> 
<cfset valueEquals(left="#DateAdd("w", 10, fixDate)#", right="{ts '2001-11-15 04:10:04'}")>
<cfset valueEquals(left="#DateAdd("w", 1123456789, fixDate)#", right="{ts '4308290-02-19 04:10:04'}")> 

<cfset valueEquals(left="#DateAdd("ww", 1, fixDate)#", right="{ts '2001-11-08 04:10:04'}")> 
<cfset valueEquals(left="#DateAdd("ww", 10, fixDate)#", right="{ts '2002-01-10 04:10:04'}")> 
<cfset valueEquals(left="#DateAdd("ww", 11234, fixDate)#", right="{ts '2217-02-20 04:10:04'}")> 

<cfset valueEquals(left="#DateAdd("h", 1, fixDate)#", right="{ts '2001-11-01 05:10:04'}")> 
<cfset valueEquals(left="#DateAdd("h", 10, fixDate)#", right="{ts '2001-11-01 14:10:04'}")> 
<cfset valueEquals(left="#DateAdd("h", 1123456789, fixDate)#", right="{ts '130165-03-05 17:10:04'}")> 

<cfset valueEquals(left="#DateAdd("n", 1, fixDate)#", right="{ts '2001-11-01 04:11:04'}")> 
<cfset valueEquals(left="#DateAdd("n", 10, fixDate)#", right="{ts '2001-11-01 04:20:04'}")> 
<cfset valueEquals(left="#DateAdd("n", 1123456789, fixDate)#", right="{ts '4137-11-21 11:59:04'}")> 

<cfset valueEquals(left="#DateAdd("s", 1, fixDate)#", right="{ts '2001-11-01 04:10:05'}")> 
<cfset valueEquals(left="#DateAdd("s", 10, fixDate)#", right="{ts '2001-11-01 04:10:14'}")> 

<cfset valueEquals(left="#DateAdd("s", 1021385053, fixDate)#", right="{ts '2034-03-14 18:14:17'}")> 
<cftry> 
        <cfset valueEquals(left="#DateAdd("peter", 1, fixDate)#", right="{ts '2001-11-01 04:10:05'}")> 
        <cfset fail("must throw:DateAdd(""peter"", 1, fixDate)")> 
        <cfcatch></cfcatch> 
</cftry>


<cfset valueEquals(left="#DateAdd("m", 1, "{ts '1900-01-31 00:00:00'}")&""#", right="{ts '1900-02-28 00:00:00'}") >
<cfset valueEquals(left="#DateAdd("yyyy", 1, "{ts '1900-01-31 00:00:00'}")&""#",  right="{ts '1901-01-31 00:00:00'}") >
<cfset valueEquals(left="#DateAdd("m", 1, 32)&""#",  right="{ts '1900-02-28 00:00:00'}" )>
<cfset valueEquals(left="#DateAdd("yyyy", 1, 32)&""#",  right="{ts '1901-01-31 00:00:00'}") >

<cfset valueEquals(left="#DateAdd("m", 1, "{ts '1899-12-31 00:00:00'}")&""#",  right="{ts '1900-01-31 00:00:00'}" )>


<cfset valueEquals(left="#parseDateTime("{ts '1901-12-31 00:00:00'}").getTime()#",  
	right="#DateAdd("yyyy", 2, 1).getTime()#")>
	
<cfset valueEquals(left="#parseDateTime("{ts '1901-12-31 00:00:00'}")#" , 
	right="#DateAdd("yyyy", 2, 1)#")>
	
<cfset valueEquals(left="{ts '1901-12-31 00:00:00'}" , 
	right="#DateAdd("yyyy", 2, 1)#")>
	
<cfset date=CreateDateTime(2008,10,28,0,0,0)>
<cfset valueEquals(left="+#date#+", right="+{ts '2008-10-28 00:00:00'}+")>
<cfset valueEquals(left="+#date+1#+", right="+39750+")>
<cfset valueEquals(left="+#DateAdd('d',0,0)#+", right="+{ts '1899-12-30 00:00:00'}+")>
<cfset valueEquals(left="+#DateAdd('d',0,date+1)#+", right="+{ts '2008-10-29 00:00:00'}+")>


<cfset date1=CreateDate(2009, 1, 1)>
<cfset date2=DateAdd('m', 1, date1)>
<cfset valueEquals(left="#date1#x", right="{ts '2009-01-01 00:00:00'}x")>
<cfset valueEquals(left="#date2#x", right="{ts '2009-02-01 00:00:00'}x")>
<cfset valueEquals(left="#DateDiff('m', date1, date2)#x", right="1x")>
<cfset valueEquals(left="#DateDiff('d', date1, date2)#x", right="31x")>
                 
<cfset date1=CreateDate(2009, 2, 1)>
<cfset date2=DateAdd('m', 1, date1)>
<cfset valueEquals(left="#date1#x", right="{ts '2009-02-01 00:00:00'}x")>
<cfset valueEquals(left="#date2#x", right="{ts '2009-03-01 00:00:00'}x")>
<cfset valueEquals(left="#DateDiff('m', date1, date2)#x", right="1x")>
<cfset valueEquals(left="#DateDiff('d', date1, date2)#x", right="28x")>
                
<cfset date1=CreateDate(2009, 3, 1)>
<cfset date2=DateAdd('m', 1, date1)>
<cfset valueEquals(left="#date1#x", right="{ts '2009-03-01 00:00:00'}x")>
<cfset valueEquals(left="#date2#x", right="{ts '2009-04-01 00:00:00'}x")>
<cfset valueEquals(left="#DateDiff('m', date1, date2)#x", right="1x")>
<cfset valueEquals(left="#DateDiff('d', date1, date2)#x", right="31x")>
                
<cfset date1=CreateDate(2009, 4, 1)>
<cfset date2=DateAdd('m', 1, date1)>
<cfset valueEquals(left="#date1#x", right="{ts '2009-04-01 00:00:00'}x")>
<cfset valueEquals(left="#date2#x", right="{ts '2009-05-01 00:00:00'}x")>
<cfset valueEquals(left="#DateDiff('m', date1, date2)#x", right="1x")>
<cfset valueEquals(left="#DateDiff('d', date1, date2)#x", right="30x")>
                
<cfset date1=CreateDate(2009, 5, 1)>
<cfset date2=DateAdd('m', 1, date1)>
<cfset valueEquals(left="#date1#x", right="{ts '2009-05-01 00:00:00'}x")>
<cfset valueEquals(left="#date2#x", right="{ts '2009-06-01 00:00:00'}x")>
<cfset valueEquals(left="#DateDiff('m', date1, date2)#x", right="1x")>
<cfset valueEquals(left="#DateDiff('d', date1, date2)#x", right="31x")>
                
<cfset date1=CreateDate(2009, 6, 1)>
<cfset date2=DateAdd('m', 1, date1)>
<cfset valueEquals(left="#date1#x", right="{ts '2009-06-01 00:00:00'}x")>
<cfset valueEquals(left="#date2#x", right="{ts '2009-07-01 00:00:00'}x")>
<cfset valueEquals(left="#DateDiff('m', date1, date2)#x", right="1x")>
<cfset valueEquals(left="#DateDiff('d', date1, date2)#x", right="30x")>
                
<cfset date1=CreateDate(2009, 7, 1)>
<cfset date2=DateAdd('m', 1, date1)>
<cfset valueEquals(left="#date1#x", right="{ts '2009-07-01 00:00:00'}x")>
<cfset valueEquals(left="#date2#x", right="{ts '2009-08-01 00:00:00'}x")>
<cfset valueEquals(left="#DateDiff('m', date1, date2)#x", right="1x")>
<cfset valueEquals(left="#DateDiff('d', date1, date2)#x", right="31x")>
                
<cfset date1=CreateDate(2009, 8, 1)>
<cfset date2=DateAdd('m', 1, date1)>
<cfset valueEquals(left="#date1#x", right="{ts '2009-08-01 00:00:00'}x")>
<cfset valueEquals(left="#date2#x", right="{ts '2009-09-01 00:00:00'}x")>
<cfset valueEquals(left="#DateDiff('m', date1, date2)#x", right="1x")>
<cfset valueEquals(left="#DateDiff('d', date1, date2)#x", right="31x")>
               
<cfset date1=CreateDate(2009, 9, 1)>
<cfset date2=DateAdd('m', 1, date1)>
<cfset valueEquals(left="#date1#x", right="{ts '2009-09-01 00:00:00'}x")>
<cfset valueEquals(left="#date2#x", right="{ts '2009-10-01 00:00:00'}x")>
<cfset valueEquals(left="#DateDiff('m', date1, date2)#x", right="1x")>
<cfset valueEquals(left="#DateDiff('d', date1, date2)#x", right="30x")>
                
<cfset date1=CreateDate(2009, 10, 1)>
<cfset date2=DateAdd('m', 1, date1)>
<cfset valueEquals(left="#date1#x", right="{ts '2009-10-01 00:00:00'}x")>
<cfset valueEquals(left="#date2#x", right="{ts '2009-11-01 00:00:00'}x")>
<cfset valueEquals(left="#DateDiff('m', date1, date2)#x", right="1x")>
<cfset valueEquals(left="#DateDiff('d', date1, date2)#x", right="31x")>
                
<cfset date1=CreateDate(2009, 11, 1)>
<cfset date2=DateAdd('m', 1, date1)>
<cfset valueEquals(left="#date1#x", right="{ts '2009-11-01 00:00:00'}x")>
<cfset valueEquals(left="#date2#x", right="{ts '2009-12-01 00:00:00'}x")>
<cfset valueEquals(left="#DateDiff('m', date1, date2)#x", right="1x")>
<cfset valueEquals(left="#DateDiff('d', date1, date2)#x", right="30x")>
                
<cfset date1=CreateDate(2009, 12, 1)>
<cfset date2=DateAdd('m', 1, date1)>
<cfset valueEquals(left="#date1#x", right="{ts '2009-12-01 00:00:00'}x")>
<cfset valueEquals(left="#date2#x", right="{ts '2010-01-01 00:00:00'}x")>
<cfset valueEquals(left="#DateDiff('m', date1, date2)#x", right="1x")>
<cfset valueEquals(left="#DateDiff('d', date1, date2)#x", right="31x")>

<cfset valueEquals(left="#DateAdd("m", 1, 0)#x", right="{ts '1900-01-30 00:00:00'}x") >
<cfset valueEquals(left="#DateAdd("yyyy", 2, 0)&""#", right="{ts '1901-12-30 00:00:00'}") >


<cfset valueEquals(left="#DateAdd('m',0,"11/01/1975 00:00 AM")#x", right="{ts '1975-11-01 00:00:00'}x")>
<cfset valueEquals(left="#DateAdd('m',0,"11/01/1975 00:01 AM")#x", right="{ts '1975-11-01 00:01:00'}x")>
<cfset valueEquals(left="#DateAdd('m',0,"11/01/1975 11:59 AM")#x", right="{ts '1975-11-01 11:59:00'}x")>
<cfset valueEquals(left="#DateAdd('m',0,"11/01/1975 12:00 AM")#x", right="{ts '1975-11-01 00:00:00'}x")>
<cfset valueEquals(left="#DateAdd('m',0,"11/01/1975 12:01 AM")#x", right="{ts '1975-11-01 00:01:00'}x")>

<cfset valueEquals(left="#DateAdd('m',0,"11/01/1975 00:00 PM")#x", right="{ts '1975-11-01 12:00:00'}x")>
<cfset valueEquals(left="#DateAdd('m',0,"11/01/1975 00:01 PM")#x", right="{ts '1975-11-01 12:01:00'}x")>
<cfset valueEquals(left="#DateAdd('m',0,"11/01/1975 11:59 PM")#x", right="{ts '1975-11-01 23:59:00'}x")>
<cfset valueEquals(left="#DateAdd('m',0,"11/01/1975 12:00 PM")#x", right="{ts '1975-11-01 12:00:00'}x")>
<cfset valueEquals(left="#DateAdd('m',0,"11/01/1975 12:01 PM")#x", right="{ts '1975-11-01 12:01:00'}x")>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
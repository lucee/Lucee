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
	<cffunction name="testDateFormat" localMode="modern">

<!--- begin old test code --->
<cfset valueEquals(left="#dateFormat(100,'dd-mm-yyyy')#", right="09-04-1900")>
<cfset valueEquals(left="#dateFormat(0,'dd-mm-yyyy')#", right="30-12-1899")>

<cfset org=getLocale()>
<cfset setLocale("German (Swiss)")>

<cfset dt=CreateDateTime(2004,1,2,4,5,6)>
<cfset valueEquals(left="#dateFormat(dt,"yyyy")#", right="2004")>
<cfset valueEquals(left="#dateFormat(dt,"yy")#", right="04")>
<cfset valueEquals(left="#dateFormat(dt,"y")#", right="4")>
<cfset valueEquals(left="#dateFormat(dt,"MMMM")#", right="January")>

<cfset valueEquals(left="#dateFormat(dt,"mmm")#", right="Jan")>
<cfset valueEquals(left="#dateFormat(dt,"mm")#x", right="01x")>
<cfset valueEquals(left="#dateFormat(dt,"m")#x", right="1x")>
<cfset valueEquals(left="#dateFormat(dt)#", right="02-Jan-04")>
<cfset valueEquals(left="#dateFormat(dt,"dddd")#", right="Friday")>
<cfset valueEquals(left="#dateFormat(dt,"ddd")#", right="Fri")>
<cfset valueEquals(left="#dateFormat(dt,"dd")#x", right="02x")>
<cfset valueEquals(left="#dateFormat(dt,"d")#x", right="2x")>
<cfset valueEquals(left="#dateFormat(dt,"dd.mm.yyyy")#x", right="02.01.2004x")>

<cfset valueEquals(left="#dateFormat(dt,"short")#x", right="1/2/04x")>
<cfset valueEquals(left="#dateFormat(dt,"medium")#x", right="Jan 2, 2004x")>
<cfset valueEquals(left="#dateFormat(dt,"long")#x", right="January 2, 2004x")>
<cfset valueEquals(left="#dateFormat(dt,"full")#x", right="Friday, January 2, 2004x")>
<cfset setLocale("French (Swiss)")>
<cfset valueEquals(left="#dateFormat(dt,"full")#x", right="Friday, January 2, 2004x")>
<cfset setLocale(org)>


<cfset valueEquals(left="#dateFormat(' ','dd.mm.yyyy')#x", right="x")>
<cfset x='susi'>
<cftry> 
	<cfset valueEquals(left="#dateFormat(x,'dd.mm.yyyy')#x", right="x")>
    <cfset fail("must throw:The value of the parameter 1, which is currently ""susi"", must be a class java.util.Date value. ")> 
    <cfcatch></cfcatch> 
</cftry> 

<cfset valueEquals(left="#dateFormat('1234','dd.mm.yyyy')#x", right="18.05.1903x")>

<cfset valueEquals(
	left="30-Dec-99",
	right="#DateFormat(0)#")>

<cfset valueEquals(
	left="01.10.2005" ,
	right="#DateFormat('2005/10/01 00:00:00', 'dd.mm.yyyy')#")>

<cfset valueEquals(
	left="01.10.2005" ,
	right="#DateFormat(ParseDateTime('2005/10/01 00:00:00'), 'dd.mm.yyyy')#")>

<cfset valueEquals(
	left="01.10.2005" ,
	right="#DateFormat(ParseDateTime('2005-10-01 00:00:00'), 'dd.mm.yyyy')#")>

<cfset valueEquals(
	left="",
	right="#DateFormat('', 'dd.mm.yyyy')#")>


<cfset date2=ParseDateTime("{ts '2008-09-01 01:34:55'}")>
<cfset date=ParseDateTime("{ts '2008-09-01 01:34:55.123'}")>


<cfset valueEquals(left="#DateFormat(date, "yymmdd")#", right="080901")>
<cfset valueEquals(left="#DateFormat(date, "yy")#", right="08")>
<cfset valueEquals(left="#DateFormat(date, "mm")#", right="09")>
<cfset valueEquals(left="#DateFormat(date, "dd")#", right="01")>


<cfset valueEquals(left="#DateFormat(date, "yymmdd") & Timeformat(date, "HHmmsslll")#", right="080901013455123")>
<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
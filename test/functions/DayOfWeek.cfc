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
	<cffunction name="testDayOfWeek" localMode="modern">

<!--- begin old test code --->
<cfset d1=CreateDateTime(2001, 12, 1, 4, 10, 1)> 
<cfset valueEquals(left="#dayOfWeek(d1)#", right="7")>
<cfset valueEquals(left="1" ,right="#DayOfWeek(1)#")>
    
    
    

<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-1 17:26:03'}")#", right="7", label="1-1")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-2 17:26:03'}")#", right="1", label="1-2")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-3 17:26:03'}")#", right="2", label="1-3")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-4 17:26:03'}")#", right="3", label="1-4")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-5 17:26:03'}")#", right="4", label="1-5")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-6 17:26:03'}")#", right="5", label="1-6")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-7 17:26:03'}")#", right="6", label="1-7")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-8 17:26:03'}")#", right="7", label="1-8")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-9 17:26:03'}")#", right="1", label="1-9")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-10 17:26:03'}")#", right="2", label="1-10")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-11 17:26:03'}")#", right="3", label="1-11")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-12 17:26:03'}")#", right="4", label="1-12")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-13 17:26:03'}")#", right="5", label="1-13")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-14 17:26:03'}")#", right="6", label="1-14")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-15 17:26:03'}")#", right="7", label="1-15")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-16 17:26:03'}")#", right="1", label="1-16")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-17 17:26:03'}")#", right="2", label="1-17")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-18 17:26:03'}")#", right="3", label="1-18")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-19 17:26:03'}")#", right="4", label="1-19")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-20 17:26:03'}")#", right="5", label="1-20")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-21 17:26:03'}")#", right="6", label="1-21")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-22 17:26:03'}")#", right="7", label="1-22")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-23 17:26:03'}")#", right="1", label="1-23")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-24 17:26:03'}")#", right="2", label="1-24")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-25 17:26:03'}")#", right="3", label="1-25")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-26 17:26:03'}")#", right="4", label="1-26")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-27 17:26:03'}")#", right="5", label="1-27")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-28 17:26:03'}")#", right="6", label="1-28")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-29 17:26:03'}")#", right="7", label="1-29")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-30 17:26:03'}")#", right="1", label="1-30")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-1-31 17:26:03'}")#", right="2", label="1-31")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-1 17:26:03'}")#", right="3", label="2-1")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-2 17:26:03'}")#", right="4", label="2-2")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-3 17:26:03'}")#", right="5", label="2-3")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-4 17:26:03'}")#", right="6", label="2-4")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-5 17:26:03'}")#", right="7", label="2-5")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-6 17:26:03'}")#", right="1", label="2-6")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-7 17:26:03'}")#", right="2", label="2-7")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-8 17:26:03'}")#", right="3", label="2-8")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-9 17:26:03'}")#", right="4", label="2-9")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-10 17:26:03'}")#", right="5", label="2-10")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-11 17:26:03'}")#", right="6", label="2-11")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-12 17:26:03'}")#", right="7", label="2-12")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-13 17:26:03'}")#", right="1", label="2-13")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-14 17:26:03'}")#", right="2", label="2-14")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-15 17:26:03'}")#", right="3", label="2-15")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-16 17:26:03'}")#", right="4", label="2-16")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-17 17:26:03'}")#", right="5", label="2-17")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-18 17:26:03'}")#", right="6", label="2-18")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-19 17:26:03'}")#", right="7", label="2-19")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-20 17:26:03'}")#", right="1", label="2-20")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-21 17:26:03'}")#", right="2", label="2-21")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-22 17:26:03'}")#", right="3", label="2-22")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-23 17:26:03'}")#", right="4", label="2-23")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-24 17:26:03'}")#", right="5", label="2-24")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-25 17:26:03'}")#", right="6", label="2-25")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-26 17:26:03'}")#", right="7", label="2-26")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-27 17:26:03'}")#", right="1", label="2-27")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-28 17:26:03'}")#", right="2", label="2-28")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-2-29 17:26:03'}")#", right="3", label="2-29")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-1 17:26:03'}")#", right="4", label="3-1")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-2 17:26:03'}")#", right="5", label="3-2")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-3 17:26:03'}")#", right="6", label="3-3")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-4 17:26:03'}")#", right="7", label="3-4")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-5 17:26:03'}")#", right="1", label="3-5")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-6 17:26:03'}")#", right="2", label="3-6")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-7 17:26:03'}")#", right="3", label="3-7")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-8 17:26:03'}")#", right="4", label="3-8")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-9 17:26:03'}")#", right="5", label="3-9")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-10 17:26:03'}")#", right="6", label="3-10")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-11 17:26:03'}")#", right="7", label="3-11")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-12 17:26:03'}")#", right="1", label="3-12")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-13 17:26:03'}")#", right="2", label="3-13")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-14 17:26:03'}")#", right="3", label="3-14")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-15 17:26:03'}")#", right="4", label="3-15")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-16 17:26:03'}")#", right="5", label="3-16")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-17 17:26:03'}")#", right="6", label="3-17")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-18 17:26:03'}")#", right="7", label="3-18")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-19 17:26:03'}")#", right="1", label="3-19")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-20 17:26:03'}")#", right="2", label="3-20")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-21 17:26:03'}")#", right="3", label="3-21")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-22 17:26:03'}")#", right="4", label="3-22")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-23 17:26:03'}")#", right="5", label="3-23")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-24 17:26:03'}")#", right="6", label="3-24")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-25 17:26:03'}")#", right="7", label="3-25")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-26 17:26:03'}")#", right="1", label="3-26")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-27 17:26:03'}")#", right="2", label="3-27")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-28 17:26:03'}")#", right="3", label="3-28")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-29 17:26:03'}")#", right="4", label="3-29")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-30 17:26:03'}")#", right="5", label="3-30")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-3-31 17:26:03'}")#", right="6", label="3-31")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-1 17:26:03'}")#", right="7", label="4-1")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-2 17:26:03'}")#", right="1", label="4-2")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-3 17:26:03'}")#", right="2", label="4-3")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-4 17:26:03'}")#", right="3", label="4-4")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-5 17:26:03'}")#", right="4", label="4-5")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-6 17:26:03'}")#", right="5", label="4-6")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-7 17:26:03'}")#", right="6", label="4-7")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-8 17:26:03'}")#", right="7", label="4-8")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-9 17:26:03'}")#", right="1", label="4-9")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-10 17:26:03'}")#", right="2", label="4-10")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-11 17:26:03'}")#", right="3", label="4-11")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-12 17:26:03'}")#", right="4", label="4-12")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-13 17:26:03'}")#", right="5", label="4-13")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-14 17:26:03'}")#", right="6", label="4-14")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-15 17:26:03'}")#", right="7", label="4-15")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-16 17:26:03'}")#", right="1", label="4-16")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-17 17:26:03'}")#", right="2", label="4-17")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-18 17:26:03'}")#", right="3", label="4-18")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-19 17:26:03'}")#", right="4", label="4-19")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-20 17:26:03'}")#", right="5", label="4-20")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-21 17:26:03'}")#", right="6", label="4-21")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-22 17:26:03'}")#", right="7", label="4-22")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-23 17:26:03'}")#", right="1", label="4-23")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-24 17:26:03'}")#", right="2", label="4-24")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-25 17:26:03'}")#", right="3", label="4-25")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-26 17:26:03'}")#", right="4", label="4-26")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-27 17:26:03'}")#", right="5", label="4-27")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-28 17:26:03'}")#", right="6", label="4-28")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-29 17:26:03'}")#", right="7", label="4-29")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-4-30 17:26:03'}")#", right="1", label="4-30")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-1 17:26:03'}")#", right="2", label="5-1")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-2 17:26:03'}")#", right="3", label="5-2")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-3 17:26:03'}")#", right="4", label="5-3")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-4 17:26:03'}")#", right="5", label="5-4")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-5 17:26:03'}")#", right="6", label="5-5")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-6 17:26:03'}")#", right="7", label="5-6")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-7 17:26:03'}")#", right="1", label="5-7")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-8 17:26:03'}")#", right="2", label="5-8")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-9 17:26:03'}")#", right="3", label="5-9")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-10 17:26:03'}")#", right="4", label="5-10")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-11 17:26:03'}")#", right="5", label="5-11")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-12 17:26:03'}")#", right="6", label="5-12")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-13 17:26:03'}")#", right="7", label="5-13")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-14 17:26:03'}")#", right="1", label="5-14")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-15 17:26:03'}")#", right="2", label="5-15")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-16 17:26:03'}")#", right="3", label="5-16")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-17 17:26:03'}")#", right="4", label="5-17")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-18 17:26:03'}")#", right="5", label="5-18")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-19 17:26:03'}")#", right="6", label="5-19")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-20 17:26:03'}")#", right="7", label="5-20")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-21 17:26:03'}")#", right="1", label="5-21")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-22 17:26:03'}")#", right="2", label="5-22")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-23 17:26:03'}")#", right="3", label="5-23")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-24 17:26:03'}")#", right="4", label="5-24")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-25 17:26:03'}")#", right="5", label="5-25")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-26 17:26:03'}")#", right="6", label="5-26")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-27 17:26:03'}")#", right="7", label="5-27")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-28 17:26:03'}")#", right="1", label="5-28")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-29 17:26:03'}")#", right="2", label="5-29")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-30 17:26:03'}")#", right="3", label="5-30")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-5-31 17:26:03'}")#", right="4", label="5-31")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-1 17:26:03'}")#", right="5", label="6-1")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-2 17:26:03'}")#", right="6", label="6-2")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-3 17:26:03'}")#", right="7", label="6-3")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-4 17:26:03'}")#", right="1", label="6-4")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-5 17:26:03'}")#", right="2", label="6-5")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-6 17:26:03'}")#", right="3", label="6-6")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-7 17:26:03'}")#", right="4", label="6-7")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-8 17:26:03'}")#", right="5", label="6-8")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-9 17:26:03'}")#", right="6", label="6-9")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-10 17:26:03'}")#", right="7", label="6-10")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-11 17:26:03'}")#", right="1", label="6-11")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-12 17:26:03'}")#", right="2", label="6-12")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-13 17:26:03'}")#", right="3", label="6-13")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-14 17:26:03'}")#", right="4", label="6-14")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-15 17:26:03'}")#", right="5", label="6-15")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-16 17:26:03'}")#", right="6", label="6-16")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-17 17:26:03'}")#", right="7", label="6-17")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-18 17:26:03'}")#", right="1", label="6-18")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-19 17:26:03'}")#", right="2", label="6-19")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-20 17:26:03'}")#", right="3", label="6-20")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-21 17:26:03'}")#", right="4", label="6-21")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-22 17:26:03'}")#", right="5", label="6-22")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-23 17:26:03'}")#", right="6", label="6-23")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-24 17:26:03'}")#", right="7", label="6-24")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-25 17:26:03'}")#", right="1", label="6-25")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-26 17:26:03'}")#", right="2", label="6-26")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-27 17:26:03'}")#", right="3", label="6-27")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-28 17:26:03'}")#", right="4", label="6-28")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-29 17:26:03'}")#", right="5", label="6-29")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-6-30 17:26:03'}")#", right="6", label="6-30")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-1 17:26:03'}")#", right="7", label="7-1")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-2 17:26:03'}")#", right="1", label="7-2")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-3 17:26:03'}")#", right="2", label="7-3")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-4 17:26:03'}")#", right="3", label="7-4")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-5 17:26:03'}")#", right="4", label="7-5")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-6 17:26:03'}")#", right="5", label="7-6")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-7 17:26:03'}")#", right="6", label="7-7")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-8 17:26:03'}")#", right="7", label="7-8")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-9 17:26:03'}")#", right="1", label="7-9")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-10 17:26:03'}")#", right="2", label="7-10")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-11 17:26:03'}")#", right="3", label="7-11")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-12 17:26:03'}")#", right="4", label="7-12")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-13 17:26:03'}")#", right="5", label="7-13")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-14 17:26:03'}")#", right="6", label="7-14")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-15 17:26:03'}")#", right="7", label="7-15")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-16 17:26:03'}")#", right="1", label="7-16")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-17 17:26:03'}")#", right="2", label="7-17")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-18 17:26:03'}")#", right="3", label="7-18")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-19 17:26:03'}")#", right="4", label="7-19")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-20 17:26:03'}")#", right="5", label="7-20")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-21 17:26:03'}")#", right="6", label="7-21")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-22 17:26:03'}")#", right="7", label="7-22")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-23 17:26:03'}")#", right="1", label="7-23")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-24 17:26:03'}")#", right="2", label="7-24")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-25 17:26:03'}")#", right="3", label="7-25")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-26 17:26:03'}")#", right="4", label="7-26")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-27 17:26:03'}")#", right="5", label="7-27")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-28 17:26:03'}")#", right="6", label="7-28")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-29 17:26:03'}")#", right="7", label="7-29")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-30 17:26:03'}")#", right="1", label="7-30")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-7-31 17:26:03'}")#", right="2", label="7-31")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-1 17:26:03'}")#", right="3", label="8-1")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-2 17:26:03'}")#", right="4", label="8-2")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-3 17:26:03'}")#", right="5", label="8-3")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-4 17:26:03'}")#", right="6", label="8-4")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-5 17:26:03'}")#", right="7", label="8-5")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-6 17:26:03'}")#", right="1", label="8-6")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-7 17:26:03'}")#", right="2", label="8-7")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-8 17:26:03'}")#", right="3", label="8-8")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-9 17:26:03'}")#", right="4", label="8-9")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-10 17:26:03'}")#", right="5", label="8-10")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-11 17:26:03'}")#", right="6", label="8-11")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-12 17:26:03'}")#", right="7", label="8-12")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-13 17:26:03'}")#", right="1", label="8-13")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-14 17:26:03'}")#", right="2", label="8-14")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-15 17:26:03'}")#", right="3", label="8-15")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-16 17:26:03'}")#", right="4", label="8-16")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-17 17:26:03'}")#", right="5", label="8-17")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-18 17:26:03'}")#", right="6", label="8-18")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-19 17:26:03'}")#", right="7", label="8-19")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-20 17:26:03'}")#", right="1", label="8-20")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-21 17:26:03'}")#", right="2", label="8-21")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-22 17:26:03'}")#", right="3", label="8-22")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-23 17:26:03'}")#", right="4", label="8-23")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-24 17:26:03'}")#", right="5", label="8-24")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-25 17:26:03'}")#", right="6", label="8-25")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-26 17:26:03'}")#", right="7", label="8-26")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-27 17:26:03'}")#", right="1", label="8-27")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-28 17:26:03'}")#", right="2", label="8-28")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-29 17:26:03'}")#", right="3", label="8-29")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-30 17:26:03'}")#", right="4", label="8-30")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-8-31 17:26:03'}")#", right="5", label="8-31")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-1 17:26:03'}")#", right="6", label="9-1")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-2 17:26:03'}")#", right="7", label="9-2")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-3 17:26:03'}")#", right="1", label="9-3")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-4 17:26:03'}")#", right="2", label="9-4")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-5 17:26:03'}")#", right="3", label="9-5")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-6 17:26:03'}")#", right="4", label="9-6")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-7 17:26:03'}")#", right="5", label="9-7")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-8 17:26:03'}")#", right="6", label="9-8")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-9 17:26:03'}")#", right="7", label="9-9")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-10 17:26:03'}")#", right="1", label="9-10")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-11 17:26:03'}")#", right="2", label="9-11")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-12 17:26:03'}")#", right="3", label="9-12")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-13 17:26:03'}")#", right="4", label="9-13")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-14 17:26:03'}")#", right="5", label="9-14")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-15 17:26:03'}")#", right="6", label="9-15")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-16 17:26:03'}")#", right="7", label="9-16")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-17 17:26:03'}")#", right="1", label="9-17")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-18 17:26:03'}")#", right="2", label="9-18")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-19 17:26:03'}")#", right="3", label="9-19")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-20 17:26:03'}")#", right="4", label="9-20")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-21 17:26:03'}")#", right="5", label="9-21")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-22 17:26:03'}")#", right="6", label="9-22")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-23 17:26:03'}")#", right="7", label="9-23")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-24 17:26:03'}")#", right="1", label="9-24")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-25 17:26:03'}")#", right="2", label="9-25")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-26 17:26:03'}")#", right="3", label="9-26")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-27 17:26:03'}")#", right="4", label="9-27")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-28 17:26:03'}")#", right="5", label="9-28")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-29 17:26:03'}")#", right="6", label="9-29")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-9-30 17:26:03'}")#", right="7", label="9-30")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-1 17:26:03'}")#", right="1", label="10-1")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-2 17:26:03'}")#", right="2", label="10-2")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-3 17:26:03'}")#", right="3", label="10-3")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-4 17:26:03'}")#", right="4", label="10-4")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-5 17:26:03'}")#", right="5", label="10-5")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-6 17:26:03'}")#", right="6", label="10-6")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-7 17:26:03'}")#", right="7", label="10-7")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-8 17:26:03'}")#", right="1", label="10-8")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-9 17:26:03'}")#", right="2", label="10-9")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-10 17:26:03'}")#", right="3", label="10-10")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-11 17:26:03'}")#", right="4", label="10-11")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-12 17:26:03'}")#", right="5", label="10-12")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-13 17:26:03'}")#", right="6", label="10-13")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-14 17:26:03'}")#", right="7", label="10-14")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-15 17:26:03'}")#", right="1", label="10-15")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-16 17:26:03'}")#", right="2", label="10-16")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-17 17:26:03'}")#", right="3", label="10-17")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-18 17:26:03'}")#", right="4", label="10-18")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-19 17:26:03'}")#", right="5", label="10-19")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-20 17:26:03'}")#", right="6", label="10-20")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-21 17:26:03'}")#", right="7", label="10-21")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-22 17:26:03'}")#", right="1", label="10-22")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-23 17:26:03'}")#", right="2", label="10-23")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-24 17:26:03'}")#", right="3", label="10-24")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-25 17:26:03'}")#", right="4", label="10-25")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-26 17:26:03'}")#", right="5", label="10-26")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-27 17:26:03'}")#", right="6", label="10-27")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-28 17:26:03'}")#", right="7", label="10-28")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-29 17:26:03'}")#", right="1", label="10-29")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-30 17:26:03'}")#", right="2", label="10-30")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-10-31 17:26:03'}")#", right="3", label="10-31")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-1 17:26:03'}")#", right="4", label="11-1")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-2 17:26:03'}")#", right="5", label="11-2")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-3 17:26:03'}")#", right="6", label="11-3")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-4 17:26:03'}")#", right="7", label="11-4")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-5 17:26:03'}")#", right="1", label="11-5")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-6 17:26:03'}")#", right="2", label="11-6")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-7 17:26:03'}")#", right="3", label="11-7")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-8 17:26:03'}")#", right="4", label="11-8")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-9 17:26:03'}")#", right="5", label="11-9")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-10 17:26:03'}")#", right="6", label="11-10")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-11 17:26:03'}")#", right="7", label="11-11")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-12 17:26:03'}")#", right="1", label="11-12")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-13 17:26:03'}")#", right="2", label="11-13")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-14 17:26:03'}")#", right="3", label="11-14")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-15 17:26:03'}")#", right="4", label="11-15")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-16 17:26:03'}")#", right="5", label="11-16")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-17 17:26:03'}")#", right="6", label="11-17")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-18 17:26:03'}")#", right="7", label="11-18")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-19 17:26:03'}")#", right="1", label="11-19")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-20 17:26:03'}")#", right="2", label="11-20")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-21 17:26:03'}")#", right="3", label="11-21")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-22 17:26:03'}")#", right="4", label="11-22")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-23 17:26:03'}")#", right="5", label="11-23")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-24 17:26:03'}")#", right="6", label="11-24")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-25 17:26:03'}")#", right="7", label="11-25")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-26 17:26:03'}")#", right="1", label="11-26")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-27 17:26:03'}")#", right="2", label="11-27")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-28 17:26:03'}")#", right="3", label="11-28")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-29 17:26:03'}")#", right="4", label="11-29")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-11-30 17:26:03'}")#", right="5", label="11-30")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-1 17:26:03'}")#", right="6", label="12-1")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-2 17:26:03'}")#", right="7", label="12-2")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-3 17:26:03'}")#", right="1", label="12-3")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-4 17:26:03'}")#", right="2", label="12-4")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-5 17:26:03'}")#", right="3", label="12-5")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-6 17:26:03'}")#", right="4", label="12-6")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-7 17:26:03'}")#", right="5", label="12-7")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-8 17:26:03'}")#", right="6", label="12-8")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-9 17:26:03'}")#", right="7", label="12-9")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-10 17:26:03'}")#", right="1", label="12-10")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-11 17:26:03'}")#", right="2", label="12-11")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-12 17:26:03'}")#", right="3", label="12-12")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-13 17:26:03'}")#", right="4", label="12-13")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-14 17:26:03'}")#", right="5", label="12-14")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-15 17:26:03'}")#", right="6", label="12-15")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-16 17:26:03'}")#", right="7", label="12-16")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-17 17:26:03'}")#", right="1", label="12-17")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-18 17:26:03'}")#", right="2", label="12-18")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-19 17:26:03'}")#", right="3", label="12-19")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-20 17:26:03'}")#", right="4", label="12-20")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-21 17:26:03'}")#", right="5", label="12-21")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-22 17:26:03'}")#", right="6", label="12-22")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-23 17:26:03'}")#", right="7", label="12-23")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-24 17:26:03'}")#", right="1", label="12-24")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-25 17:26:03'}")#", right="2", label="12-25")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-26 17:26:03'}")#", right="3", label="12-26")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-27 17:26:03'}")#", right="4", label="12-27")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-28 17:26:03'}")#", right="5", label="12-28")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-29 17:26:03'}")#", right="6", label="12-29")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-30 17:26:03'}")#", right="7", label="12-30")>
<cfset valueEquals(left="#DayOfWeek("{ts '2000-12-31 17:26:03'}")#", right="1", label="12-31")>
<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
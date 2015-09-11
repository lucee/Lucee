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
	<cffunction name="testArrayMid">

<!--- begin old test code --->
<cfset var text="abcdef">
<cfset var arr=['a','b','c','d','e','f']>






<cfset valueEquals(left="#mid(text,1)#", right="abcdef")>
<cfset valueEquals(left="#mid(text,2)#", right="bcdef")>
<cfset valueEquals(left="#mid(text,1,3)#", right="abc")>
<cfset valueEquals(left="#mid(text,2,3)#", right="bcd")>
<cfset valueEquals(left="#mid(text,2,100)#", right="bcdef")>
<cfset valueEquals(left="#mid(text,200,100)#", right="")>



<cfset valueEquals(left="#arrayToList(arrayMid(arr,1))#", right="a,b,c,d,e,f")>
<cfset valueEquals(left="#arrayToList(arrayMid(arr,2))#", right="b,c,d,e,f")>
<cfset valueEquals(left="#arrayToList(arrayMid(arr,1,3))#", right="a,b,c")>
<cfset valueEquals(left="#arrayToList(arrayMid(arr,2,3))#", right="b,c,d")>
<cfset valueEquals(left="#arrayToList(arrayMid(arr,2,100))#", right="b,c,d,e,f")>
<cfset valueEquals(left="#arrayToList(arrayMid(arr,200,100))#", right="")>

<cfset arr=['a','b']>
<cfset arr[4]='d'>
<cfset arr[5]='e'>
<cfset arr[6]='f'>
<cfset valueEquals(left="#arrayToList(arrayMid(arr,1,3))#", right="a,b,")>
<cfset valueEquals(left="#arrayToList(arrayMid(arr,2,3))#", right="b,,d")>

<!--- end old test code --->
	
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
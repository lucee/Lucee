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
	<cffunction name="testArrayFilter">
		<cfscript>
// UDF

var arr=["hello","world"];
var arr2=ArrayFilter(arr,helloFilter);

valueEquals(arrayToList(arr),'hello,world');
valueEquals(arrayToList(arr2),'hello');


// closure 
var clo=function (arg1){
	return FindNoCase("hello",arg1);
};
arr2=ArrayFilter(arr,clo);
valueEquals(arrayToList(arr),'hello,world');
valueEquals(arrayToList(arr2),'hello');


// string filter (not supported by ACF)
/*if(server.ColdFusion.ProductName EQ "Railo") {
	arr2=ArrayFilter(arr,"he*");
	valueEquals(arrayToList(arr),'hello,world');
	valueEquals(arrayToList(arr2),'hello');
}*/
</cfscript>
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	<cfscript>
	private boolean function helloFilter(arg1){
		return FindNoCase("hello",arg1);
	}
	</cfscript>
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
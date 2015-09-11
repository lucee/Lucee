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
	<cffunction name="testArrayFindAll">
		<cfscript>
var arr=["aaa","bb","aaa","ccc","AAA"];
res=arrayFindAll(arr,"aaa");
valueEquals(arraytoList(res),'1,3');
res=arrayFindAll(arr,"a");
valueEquals(arraytoList(res),'');


arr=["hello","world","susi","world"];

// UDF
res=arrayFindAll(arr,doFind);
valueEquals(arrayToList(res),"2,4");

// Closure
doFind=function (value){
	return value EQ "world";
};
res=arrayFindAll(arr,doFind);
valueEquals(arrayToList(res),"2,4");

</cfscript>
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	<cfscript>
	private function doFind(value){
		return value EQ "world";
	}
	
	</cfscript>
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
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
	<cffunction name="testArrayFindNoCase">
<cfif server.ColdFusion.ProductName EQ "lucee">
<cfset valueEquals (left="#ArrayFindNoCase(listToArray('abba,bb'),'bb')#", right="2")>
<cfset valueEquals (left="#ArrayFindNoCase(listToArray('abba,bb,AABBCC,BB'),'BB')#", right="2")>
<cfset valueEquals (left="#ArrayFindNoCase(listToArray('abba,bb,AABBCC'),'ZZ')#", right="0")>
</cfif>



<!--- struct --->
<cfscript>
// create 2 structs
tony.this="that";
tony.foo="bar";
bill.test="testing";
bill.key="value ";

bill2.test="testing";
bill2.key="value ";

bill3=createObject('java','java.util.HashMap');
bill3.put('test',"testing");
bill3.put('key',"value ");

bill4.test="Testing";
bill4.key="Value ";



testarray = [ tony,bill ];
</cfscript>

<cfset valueEquals (left="#arrayFindNoCase( testArray,bill )#", right="#2#")>
<cfset valueEquals (left="#arrayFindNoCase( testArray,bill2 )#", right="#2#")>
<cfset valueEquals (left="#arrayFindNoCase( testArray,bill3 )#", right="#0#")>
<cfset valueEquals (left="#arrayFindNoCase( testArray,bill4 )#", right="#2#")>

<!--- array --->
<cfscript>
arr1=["abba1","hanna1"];
arr2=["abba2","hanna2"];
arr3=["abba2","hanna2"];

testarray = [ arr1,arr2 ];

arr4=createObject('java','java.util.ArrayList');
arr4.add('abba2');
arr4.add('hanna2');
</cfscript>


<cfset valueEquals (left="#arrayFindNoCase( testArray,arr2 )#", right="#2#")>
<cfset valueEquals (left="#arrayFindNoCase( testArray,arr3 )#", right="#2#")>
<cfset valueEquals (left="#arrayFindNoCase( testArray,arr4 )#", right="#0#")>


<!--- query --->
<cfscript>
qry1=queryNew('a,b,c');
QueryAddRow(qry1);
QuerySetCell(qry1,'a','a1');
QuerySetCell(qry1,'b','b1');
QuerySetCell(qry1,'c','c1');
qry2=queryNew('a,b,c');
QueryAddRow(qry2);
QuerySetCell(qry2,'a','a1');
QuerySetCell(qry2,'b','b1');
QuerySetCell(qry2,'c','c1');
qry3=queryNew('a,b,c');
QueryAddRow(qry3);
QuerySetCell(qry3,'a','a1');
QuerySetCell(qry3,'b','b1');
QuerySetCell(qry3,'c','c2');



testarray = [ qry1 ];

</cfscript>
<cfset valueEquals (left="#arrayFindNoCase( testArray,qry1 )#", right="#1#")>
<cfset valueEquals (left="#arrayFindNoCase( testArray,qry2 )#", right="#1#")>
<cfset valueEquals (left="#arrayFindNoCase( testArray,qry3 )#", right="#0#")>
		
		<!--- <cfset assertEquals("","")> --->
	</cffunction>
	
	<cffunction access="private" name="valueEquals">
		<cfargument name="left">
		<cfargument name="right">
		<cfset assertEquals(arguments.right,arguments.left)>
	</cffunction>
</cfcomponent>
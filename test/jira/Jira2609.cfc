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
	
	<cffunction name="beforeTests">
		<!---create a file in the ram resource --->
		<cfset variables.filePath="ram:///jira2609.txt">
		<cffile action="write" file="#variables.filePath#" output="a
b
c
d
e">
	</cffunction>
	
	<cffunction name="afterTests">
		<cffile action="delete" file="#variables.filePath#">
	</cffunction>
	
	<cffunction name="testIndex">
		<cfset counter=0>
		<cfloop file="#variables.filePath#" index="indexName" from="2" to="2">
			<cfset counter++>
			<cfset assertEquals("b",indexName)>
		</cfloop>
		<cfset assertEquals(1,counter)>
	</cffunction>
	
	<cffunction name="testItem">
		<cfset counter=0>
		<cfloop file="#variables.filePath#" item="itemName" from="2" to="2">
			<cfset counter++>
			<cfset assertEquals("b",itemName)>
		</cfloop>
		<cfset assertEquals(1,counter)>
	</cffunction>
	
	<cffunction name="testIndexAndItem">
		<cfset counter=0>
		<cfloop file="#variables.filePath#"  index="indexName" item="itemName" from="2" to="2">
			<cfset counter++>
			<cfset assertEquals(2,indexName)>
			<cfset assertEquals("b",itemName)>
		</cfloop>
		<cfset assertEquals(1,counter)>
	</cffunction>
	
</cfcomponent>
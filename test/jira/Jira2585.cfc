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
 ---><cfcomponent extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq">
	
	<cffunction name="setUp"></cffunction>
	<cffunction name="testNull">
		<cfscript>
		local.dataQuery = queryNew("colA,colB", "Integer,VarChar");
		queryAddRow(dataQuery, 4);
		querySetCell(dataQuery, "colA", "3",1);
		querySetCell(dataQuery, "colB", "dummy1",1);
		querySetCell(dataQuery, "colA", "1", 2);
		querySetCell(dataQuery, "colB", "dummy2", 2);
		querySetCell(dataQuery, "colA", "11", 3);
		querySetCell(dataQuery, "colB", "dummy3", 3);
		querySetCell(dataQuery, "colA", nullValue(), 4);
		querySetCell(dataQuery, "colB", "dummy3", 4);
		</cfscript>
		
		
		<cfquery name="dataQuery" dbtype="query">
		select	 *
		from	 dataQuery
		order by	 colA
		</cfquery>
		<cfset assertEquals(",1,3,11",arrayToList(queryColumnData(local.dataQuery,'colA')))>
	</cffunction>
	
	<cffunction name="testEmptyString">
		<cfscript>
		local.dataQuery = queryNew("colA,colB", "Integer,VarChar");
		queryAddRow(dataQuery, 4);
		querySetCell(dataQuery, "colA", "3",1);
		querySetCell(dataQuery, "colB", "dummy1",1);
		querySetCell(dataQuery, "colA", "1", 2);
		querySetCell(dataQuery, "colB", "dummy2", 2);
		querySetCell(dataQuery, "colA", "11", 3);
		querySetCell(dataQuery, "colB", "dummy3", 3);
		querySetCell(dataQuery, "colA", "", 4);
		querySetCell(dataQuery, "colB", "dummy3", 4);
		</cfscript>
		
		
		<cfquery name="dataQuery" dbtype="query">
		select	 *
		from	 dataQuery
		order by	 colA
		</cfquery>
		<cfset assertEquals(",1,3,11",arrayToList(queryColumnData(local.dataQuery,'colA')))>
	</cffunction>
</cfcomponent>
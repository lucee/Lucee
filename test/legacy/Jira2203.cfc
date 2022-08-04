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
	<!---
	<cffunction name="afterTests"></cffunction>
	<cffunction name="setUp"></cffunction>
	--->
	
	<cffunction name="beforeTests">
		<cfset variables.qry=query(
				_varchar:["a","b","c"],
				_integer:[1,2,3]
				)>
	
	</cffunction>

	
	<cffunction name="testArraySimple">
		<cfquery name="local.q" dbtype="query" params="#["b",2]#" result="local.r">
			select * from qry where _varchar=? and _integer=?
		</cfquery>
		
		<cfset assertEquals(1,q.recordcount)>
		<cfset assertEquals("b",q._varchar)>
		<cfset assertEquals(2,q._integer)>
		
		<cfset assertEquals("_varchar,_integer",r.COLUMNLIST)>
		<cfset assertEquals("b",r.sqlparameters[1])>
		<cfset assertEquals(2,r.sqlparameters[2])>
	</cffunction>
	
	<cffunction name="testArrayComplex">
		<cfquery name="local.q" dbtype="query" params="#[{value:"b"},{value:2, type:"integer"}]#" result="local.r">
			select * from qry where _varchar=? and _integer=?
		</cfquery>
		
		<cfset assertEquals(1,q.recordcount)>
		<cfset assertEquals("b",q._varchar)>
		<cfset assertEquals(2,q._integer)>
		
		<cfset assertEquals("_varchar,_integer",r.COLUMNLIST)>
		<cfset assertEquals("b",r.sqlparameters[1])>
		<cfset assertEquals(2,r.sqlparameters[2])>
		
		
		<!--- must fail --->
		<cftry>
			<cfquery name="local.q" dbtype="query" params="#[{value:"b"},{value:2, type:"quacks"}]#" result="local.r">
				select * from qry where _varchar=? and _integer=?
			</cfquery>
			<cfset fail("must throw:invalid CF SQL Type [QUACKS]")>
			<cfcatch type="database"></cfcatch>
		</cftry>
		
		<!--- must fail --->
		<cftry>
			<cfquery name="local.q" dbtype="query" params="#[{value:"b"},{}]#" result="local.r">
				select * from qry where _varchar=? and _integer=?
			</cfquery>
			<cfset fail("must throw:key [value] doesn't exist (existing keys:)")>
			<cfcatch type="expression"></cfcatch>
		</cftry>
		
	</cffunction>
	
	
	<cffunction name="testArrayMixed">
		<cfquery name="local.q" dbtype="query" params="#["b",{value:2, type:"integer"}]#" result="local.r">
			select * from qry where _varchar=? and _integer=?
		</cfquery>
		
		<cfset assertEquals(1,q.recordcount)>
		<cfset assertEquals("b",q._varchar)>
		<cfset assertEquals(2,q._integer)>
		
		<cfset assertEquals("_varchar,_integer",r.COLUMNLIST)>
		<cfset assertEquals("b",r.sqlparameters[1])>
		<cfset assertEquals(2,r.sqlparameters[2])>
		
			
		
	</cffunction>
	
	
	<cffunction name="testArrayMixedNamed">
		<cfquery name="local.q" dbtype="query" params="#["b",{name:"susi",value:2, type:"integer"}]#" result="local.r">
			select * from qry where _integer=:susi and _varchar=? and _integer=:susi
		</cfquery>
		
		<cfset assertEquals(1,q.recordcount)>
		<cfset assertEquals("b",q._varchar)>
		<cfset assertEquals(2,q._integer)>
		
		<cfset assertEquals("_varchar,_integer",r.COLUMNLIST)>
		<cfset assertEquals(2,r.sqlparameters[1])>
		<cfset assertEquals("b",r.sqlparameters[2])>
		<cfset assertEquals(2,r.sqlparameters[3])>
	</cffunction>
	
	<cffunction name="testStructSimple">
		<cfquery name="local.q" dbtype="query" params="#{v:'b',i:2}#" result="local.r">
			select * from qry where _varchar=:v and _integer=:i
		</cfquery>
		
		<cfset assertEquals(1,q.recordcount)>
		<cfset assertEquals("b",q._varchar)>
		<cfset assertEquals(2,q._integer)>
		
		<cfset assertEquals("_varchar,_integer",r.COLUMNLIST)>
		<cfset assertEquals("b",r.sqlparameters[1])>
		<cfset assertEquals(2,r.sqlparameters[2])>
	</cffunction>
	
	
	<cffunction name="testStructComplex">
		<cfquery name="local.q" dbtype="query" params="#{v:{value:'b'},i:{value:2,type:"integer"}}#" result="local.r">
			select * from qry where _varchar=:v and _integer=:i
		</cfquery>
		
		<cfset assertEquals(1,q.recordcount)>
		<cfset assertEquals("b",q._varchar)>
		<cfset assertEquals(2,q._integer)>
		
		<cfset assertEquals("_varchar,_integer",r.COLUMNLIST)>
		<cfset assertEquals("b",r.sqlparameters[1])>
		<cfset assertEquals(2,r.sqlparameters[2])>
	</cffunction>
	
	<cffunction name="testStructMixed">
		<cfquery name="local.q" dbtype="query" params="#{v:'b',i:{value:2,type:"integer"}}#" result="local.r">
			select * from qry where _varchar=:v and _integer=:i
		</cfquery>
		
		<cfset assertEquals(1,q.recordcount)>
		<cfset assertEquals("b",q._varchar)>
		<cfset assertEquals(2,q._integer)>
		
		<cfset assertEquals("_varchar,_integer",r.COLUMNLIST)>
		<cfset assertEquals("b",r.sqlparameters[1])>
		<cfset assertEquals(2,r.sqlparameters[2])>
	</cffunction>
</cfcomponent>
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


	<cffunction name="testQoQColumnType">
		
		<cfset local.q1 = queryNew( "price", "decimal"
			,[
				 [ "8.5" ]
			 	,[ "1.75" ]
			 	,[ "3.5" ]
			 	,[ "2.5" ]]
		)>

		<cfquery name="local.q2" dbtype="query">
			select * from q1
		</cfquery>

		<cfset assertEquals( getMetaData( q1 )[ 1 ].typeName, getMetaData( q2 )[ 1 ].typeName )>
	</cffunction>


</cfcomponent>
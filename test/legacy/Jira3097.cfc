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
	
	<cffunction name="testBlobClob">
		<cfquery>
		INSERT INTO T3097(blobi,clobi)
		VALUES(
			<cfqueryparam cfsqltype="cf_sql_blob" value="#"abc".getBytes()#"> 
			,<cfqueryparam cfsqltype="cf_sql_clob" value="abc"> 
		)
		</cfquery>


		<cfquery name="local.qry">
			select * from T3097
		</cfquery>


		<cfset assertEquals("abc",toString(qry.blobi))>
		<cfset assertEquals("abc",qry.clobi)>
	</cffunction>
<cfscript>

	public function beforeTests(){
		defineDatasource();

		try{
			query {
				echo("drop TABLE T3097");
			}
		}
		catch(local.e){}
		
		
		query  {
			echo("CREATE TABLE T3097 (");
			echo("blobi BLOB,");
			echo("clobi TEXT");		
			echo(");");
		}

	}

	private string function defineDatasource(){
		application action="update" 
			datasource="#server.getDatasource( "h2", server._getTempDir( "jira3097" ) )#";
	}

</cfscript>

</cfcomponent>
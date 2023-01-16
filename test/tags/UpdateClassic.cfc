<!--- 
 *
 * Copyright (c) 2015, Lucee Association Switzerland. All rights reserved.
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

	<cffunction name="test" localmode="true">
		<!--- not working in JSR223env --->
		<cfif server.lucee.environment=="servlet">
		
			<!--- insert --->
			<cfset form.i=1>
			<cfinsert tablename="TUpdateX" formfields="i">

			<cfquery  name="data" >
			select i,i is null as isNUll from TUpdateX
			</cfquery>
			<cfset assertEquals(1,data.recordcount)>
			<cfset assertEquals(1,data.id)>
			<cfset assertEquals("",data.i)>
			<cfset assertEquals(true,data.isNull)>

			<cfset form.i=5>
			<cfupdate tablename="TUpdateX" formfields="i,">

			<cfquery  name="data">
			select i,i is null as isNUll from TUpdateX
			</cfquery>
			<cfset assertEquals(1,data.recordcount)>
			<cfset assertEquals(1,data.id)>
			<cfset assertEquals(5,data.i)>
			<cfset assertEquals(false,data.isNull)>

		</cfif>
	</cffunction>
	<cfscript>
		public function beforeTests(){
			defineDatasource();

			try{
				query {
					echo("drop TABLE TUpdateX");
				}
			}
			catch(local.e){}

			query  {
				echo("CREATE TABLE TUpdateX (i int) ");
			}
		}

		private string function defineDatasource(){
			application action="update" 
				datasource="#server.getDatasource( "h2", server._getTempDir( "updateClassic" ) )#";
		}

		public function afterTests(){
			try{
				query {
					echo("drop TABLE TUpdateX");
				}
			}
			catch(local.e){}
		}
	</cfscript>
</cfcomponent>
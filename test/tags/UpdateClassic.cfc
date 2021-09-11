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
			<cfset form.id=1>
			<cfinsert tablename="TUpdateX" formfields="id">

			<cfquery  name="data" >
			select id,i,i is null as isNUll from TUpdateX
			</cfquery>
			<cfset assertEquals(1,data.recordcount)>
			<cfset assertEquals(1,data.id)>
			<cfset assertEquals("",data.i)>
			<cfset assertEquals(true,data.isNull)>

			<cfset form.id=1>
			<cfset form.i=5>
			<cfupdate tablename="TUpdateX" formfields="id,i,">

			<cfquery  name="data">
			select id,i,i is null as isNUll from TUpdateX
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
			echo("CREATE TABLE TUpdateX (");
			echo("id INTEGER IDENTITY NOT NULL,");
			echo("i int,");		
			//echo("dec DECIMAL,");	
			echo("PRIMARY KEY (id)");	
			echo(") ");
		}
	}


	private string function defineDatasource(){
		application action="update" 
			datasource="#{
	  		class: 'org.h2.Driver'
	  		, bundleName: 'org.h2'
			, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/update;MODE=MySQL'
		}#";
	}

	public function afterTests(){
		try{
			query {
				echo("drop TABLE TUpdateX");
			}
		}
		catch(local.e){}
		end();
	}

	function end() {
		var javaIoFile=createObject("java","java.io.File");
		loop array=DirectoryList(
			path=getDirectoryFromPath(getCurrentTemplatePath()), 
			recurse=true, filter="*.db") item="local.path"  {
			fileDeleteOnExit(javaIoFile,path);
		}
	}

	private function fileDeleteOnExit(required javaIoFile, required string path) {
		var file=javaIoFile.init(arguments.path);
		if(!file.isFile())file=javaIoFile.init(expandPath(arguments.path));
		if(file.isFile()) file.deleteOnExit();
	}
</cfscript>



</cfcomponent>
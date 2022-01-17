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
 ---><cfscript>
component extends="org.lucee.cfml.test.LuceeTestCase"	{
	
	public function beforeTests(){
		defineDatasource();

		try{
			query {
				echo("drop TABLE T1031");
			}
		}
		catch(local.e){}
		
		query  {
			echo("CREATE TABLE T1031 (");
			echo("id int NOT NULL,");
			echo("i int,");		
			echo("vc varchar(255)");		
			echo(") ");
		}
	}

	private string function defineDatasource(){
		application action="update" 
			datasource="#{
			class: 'org.h2.Driver'
			, bundleName: 'org.h2'
			, bundleVersion: '1.3.172'
			, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db;MODE=MySQL'
			, connectionLimit:100 // default:-1
		}#";
	}

	public void function test() {
		q = new Query(
			sql = "insert into T1031(id, i, vc) values(1,2,'3')"
		);
		q.execute();
	}

	function afterTests() {
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
} 
</cfscript>
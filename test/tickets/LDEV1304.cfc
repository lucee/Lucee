<!--- 
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
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
	
	
	//public function afterTests(){}
	
	public function setUp(){
		
	}

	public void function test(){
		defineDatasource();
		try{
			query {
				echo("drop TABLE T1304");
			}
		}
		catch(local.e){}
		
		query  {
			echo("CREATE TABLE T1304 (");
			echo("id int NOT NULL,");
			echo("i int,");		
			echo("vc varchar(255)");		
			echo(") ");
		}

		query  {
			echo("insert into T1304(id, i, vc) values(1,2,'3')");
		}


		local.past=createDateTime(2018,11,19,13,35,0);
		local.future=createDateTime(year(now())+1,11,19,13,35,0);

		query name="local.q" cachedWithin=1 cachedAfter=past  {
			echo("select * from T1304 where id=1");
		}
		assertEquals(2,q.i);

		query  {
			echo("update T1304 set i=3 where id=1");
		}

		query name="local.q" cachedWithin=0 cachedAfter=future  {
			echo("select * from T1304 where id=1");
		}
		assertEquals(3,q.i);


		query  {
			echo("update T1304 set i=4 where id=1");
		}


		query name="local.q" cachedWithin=1 cachedAfter=past  {
			echo("select * from T1304 where id=1");
		}
		assertEquals(2,q.i);

		query name="local.q" {
			echo("select * from T1304 where id=1");
		}
		assertEquals(4,q.i);




	}

	private void function defineDatasource(){
		application action="update" 
			datasource={
			class: 'org.h2.Driver'
			, bundleName: 'org.h2'
			, bundleVersion: '1.3.172'
			, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/LDEV1304/db;MODE=MySQL'
			, connectionLimit:100 // default:-1
		};
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
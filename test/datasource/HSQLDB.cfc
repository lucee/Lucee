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

	public void function testConnection180(){
		defineDatasource('hypersonic.hsqldb','1.8.0');
		testConnection();
	}
	public void function testConnection232(){
		defineDatasource('org.hsqldb.hsqldb','2.3.2');
		testConnection();
	}
	public void function testConnection235(){
		defineDatasource('org.hsqldb.hsqldb','2.3.5');
		testConnection();
	}
	public void function testConnection240(){
		defineDatasource('org.hsqldb.hsqldb','2.4.0');
		testConnection();
	}

	private void function testConnection(){
		query name="local.qry" {
			echo("SELECT * FROM INFORMATION_SCHEMA.SYSTEM_USERS");
		}
	}

	private void function defineDatasource(bundle,version){
		application action="update" 
			datasource={
	  		class: 'org.hsqldb.jdbcDriver'
			, bundleName: arguments.bundle
			, bundleVersion: arguments.version
			, connectionString: 'jdbc:hsqldb:file:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db#replace(arguments.version,'.','_','all')#'
		};
	}
} 
</cfscript>
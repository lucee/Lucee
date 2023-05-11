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
component extends="org.lucee.cfml.test.LuceeTestCase" labels="oracle"	{
	
	variables.TABLE_NAME="LDEV0902";

	public function setUp(){
		variables.has=defineDatasource();	
		if(variables.has) createTable();
	}
	public function teardown(){
		deleteTable();		
	}

	public void function testConnection(){
		if(!variables.has) return;
		
		query name="local.qry" {
			echo("select * from "&TABLE_NAME);

		}

		assertEquals("Susi",qry.c_clob);
		assertEquals("Sorglos",qry.c_nclob);
		
	}

	public void function testQofQ(){
		if(!variables.has) return;
		
		query name="local.qry" {
			echo("select * from "&TABLE_NAME);

		}
		query dbtype="query" name="local.qry" {
			echo("select * from qry");

		}
		
	}

	private void function createTable(){
		
		query {
			echo("CREATE TABLE "&TABLE_NAME&"
		    ( tid    NUMBER(6)
		    , c_clob     CLOB
		    , c_nclob    NCLOB
		    )");
		}

		query {
			echo("insert into "&TABLE_NAME&"
		    ( tid,c_clob,c_nclob)
		    values(1,'Susi','Sorglos')");
		}

	}

	private void function deleteTable(){
		try {
			query {
				echo("DROP TABLE "&TABLE_NAME&" PURGE");
			}
		}
		catch(local.e) {}
	}

	private boolean function defineDatasource(){
		var orcl = server.getDatasource("oracle");
		if(orcl.count()==0) return false;

		// otherwise we get the following on travis ORA-00604: error occurred at recursive SQL level 1 / ORA-01882: timezone region not found
		var tz=getTimeZone();
		//var d1=tz.getDefault();
		tz.setDefault(tz);
		//throw d1&":"&tz.getDefault();

		application action="update" datasource="#orcl#";
		return true;
	}

} 
</cfscript>
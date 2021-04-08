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
	
	
	variables.PROCEDURE="testOraclePro";
	variables.TABLE="testOracleTbl";

	public function setUp(){
		variables.has=defineDatasource();
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


	public void function testStoredProcIn(){
		if(!variables.has) return;
		echo(now()&"start:testStoredProcIn
			");
		query name="qry" {
			echo("
CREATE OR REPLACE PROCEDURE procOneINParameter(param1 IN VARCHAR2)
IS
BEGIN
  DBMS_OUTPUT.PUT_LINE('in:' || param1);
END;
			");
		}
		echo(now()&"----testStoredProcIn
			");
		storedproc procedure="procOneINParameter" {
			procparam type="in" value="input1" cfsqltype="cf_sql_varchar";
		}
		
		echo(now()&"end:testStoredProcIn
			");
	}

	public void function testStoredProcOut(){
		if(!variables.has) return;
		
		query name="qry" {
			echo("
CREATE OR REPLACE PROCEDURE procOneOUTParameter(outParam1 OUT VARCHAR2)
IS
BEGIN
  outParam1 := 'out';
END;
			");
		}

		storedproc procedure="procOneOUTParameter" {
			procparam type="out" variable="res" cfsqltype="cf_sql_varchar";
		}

		assertEquals('out',res);
		
	}

	public void function testStoredProcInOut(){
		if(!variables.has) return;
		
		query name="qry" {
			echo("
CREATE OR REPLACE PROCEDURE procOneINOUTParameter(genericParam IN OUT VARCHAR2)
IS
BEGIN
  genericParam := 'out:' || genericParam;
END;
			");
		}
		storedproc procedure="procOneINOUTParameter" {
			procparam type="inout" variable="res" value="in" cfsqltype="cf_sql_varchar";
		}
		assertEquals('out:in',res);
		
	}



	public void function testConnection(){
		if(!variables.has) return;
		
		echo(now()&"start:testConnection
			");
		query name="local.qry" {
			echo("SELECT table_name FROM user_tables where table_name like 'MAP_%'");
		}
		echo(now()&"endpublic:testConnection
			");
		//assertEquals("AA",qry.a);
		
	}

	

} 
</cfscript>
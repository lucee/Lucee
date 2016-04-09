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

	private void function testStoredProcOut(){
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

	private void function testStoredProcInOut(){
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



	private void function testConnection(){
		if(!variables.has) return;
		
		query name="local.qry" {
			echo("SELECT owner, table_name FROM dba_tables where table_name like 'MAP_%'");
		}
		//assertEquals("AA",qry.a);
		
	}

	private boolean function defineDatasource(){
		var orc=getCredencials();
		if(orc.count()==0) return false;

		// otherwise we get the following on travis ORA-00604: error occurred at recursive SQL level 1 / ORA-01882: timezone region not found
		var tz=getTimeZone();
		//var d1=tz.getDefault();
		tz.setDefault(tz);
		//throw d1&":"&tz.getDefault();

		application action="update" 

			datasource="#
			{
	  class: 'oracle.jdbc.OracleDriver'
	, bundleName: 'ojdbc7'
	, bundleVersion: '12.1.0.2'
	, connectionString: 'jdbc:oracle:thin:@#orc.server#:#orc.port#/#orc.database#'
	, username: orc.username
	, password: orc.password
}#";
	
	return true;
	}

	private struct function getCredencials() {
		// getting the credetials from the enviroment variables
		var orc={};
		if(
			!isNull(server.system.environment.ORACLE_SERVER) && 
			!isNull(server.system.environment.ORACLE_USERNAME) && 
			!isNull(server.system.environment.ORACLE_PASSWORD) && 
			!isNull(server.system.environment.ORACLE_PORT) && 
			!isNull(server.system.environment.ORACLE_DATABASE)) {
			orc.server=server.system.environment.ORACLE_SERVER;
			orc.username=server.system.environment.ORACLE_USERNAME;
			orc.password=server.system.environment.ORACLE_PASSWORD;
			orc.port=server.system.environment.ORACLE_PORT;
			orc.database=server.system.environment.ORACLE_DATABASE;
		}
		// getting the credetials from the system variables
		else if(
			!isNull(server.system.properties.ORACLE_SERVER) && 
			!isNull(server.system.properties.ORACLE_USERNAME) && 
			!isNull(server.system.properties.ORACLE_PASSWORD) && 
			!isNull(server.system.properties.ORACLE_PORT) && 
			!isNull(server.system.properties.ORACLE_DATABASE)) {
			orc.server=server.system.properties.ORACLE_SERVER;
			orc.username=server.system.properties.ORACLE_USERNAME;
			orc.password=server.system.properties.ORACLE_PASSWORD;
			orc.port=server.system.properties.ORACLE_PORT;
			orc.database=server.system.properties.ORACLE_DATABASE;
		}
		return orc;
	}




} 
</cfscript>
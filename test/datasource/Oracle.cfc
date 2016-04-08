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


	public void function testStoredProc(){
		if(!variables.has) return;
		
		query name="local.qry" {
			echo("
CREATE OR REPLACE PROCEDURE procPrintHelloWorld
IS
BEGIN  
  DBMS_OUTPUT.PUT_LINE('Hello World!');
END
			");
		}
		

		storedproc procedure="procPrintHelloWorld" {
			procresult name="local.res" resultset="1";
		}
		assertEquals('Hello World!',res);

		//assertEquals("AA",qry.a);
		
	}

	public void function testStoredProcIn(){
		if(!variables.has) return;
		
		query name="local.qry" {
			echo("
CREATE OR REPLACE PROCEDURE procOneINParameter(param1 IN VARCHAR2)
IS
BEGIN
  DBMS_OUTPUT.PUT_LINE('Hello World IN parameter ' || param1);
END
			");
		}
		

		storedproc procedure="procOneINParameter" {
			procparam type="in" cfsqltype="cf_sql_varchar" value="mkyong";
			procresult name="local.res" resultset="1";
		}
		assertEquals('Hello World IN parameter mkyong',res);
		
	}


	public void function testConnection(){
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
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
		if(!variables.has) return;

		query name="qry" {
			echo("
CREATE OR REPLACE package lucee_bug_test as
 PROCEDURE testproc;
 PROCEDURE testproc2(p1 varchar2);
end;
			");
		}	

		query name="qry" {
			echo("
CREATE OR REPLACE package body lucee_bug_test as
  PROCEDURE testproc IS
  BEGIN
    NULL;
  END;
  procedure testproc2(p1 varchar2) is
  begin
    null;
  end;
END;
			");
		}		
	}


	public void function testStoredProc(){
		if(!variables.has) return;
		// calling lucee_bug_test.testproc
		storedproc procedure="lucee_bug_test.testproc";
	}

	public void function testStoredProc2() skip="true"{  // no system access
		if(!variables.has) return;
		// calling people_web_app.lucee_bug_test.testproc
  		storedproc procedure="system.lucee_bug_test.testproc";
	}

	public void function testStoredProcIn(){
		if(!variables.has) return;
		// calling lucee_bug_test.testproc2('foo')
  		storedproc procedure="lucee_bug_test.testproc2" {
			procparam cfsqltype="cf_sql_varchar" value="foo";
		}
	}

	public void function testStoredProcIn2() skip="true"{  // no system access
		if(!variables.has) return;
		// calling people_web_app.lucee_bug_test.testproc2('foo')
  		storedproc procedure="system.lucee_bug_test.testproc2" {
			procparam cfsqltype="cf_sql_varchar" value="foo";
		}
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

			datasource="#server.getDatasource("oracle")#";
	
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
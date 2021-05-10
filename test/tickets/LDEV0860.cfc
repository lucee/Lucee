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


	public function setUp(){
		variables.has=defineDatasource();		
	}

	// read_uncommitted: Allows dirty read, non-repeatable read, and phantom
	private void function testReadUncommitted(){
		if(!variables.has) return;
		transaction isolation="read_uncommitted" {
			query name="local.qry" {
				echo("SELECT table_name FROM user_tables where table_name like 'MAP_%'");
			}
		}
	}

	// read_committed: Allows non-repeatable read and phantom. Does not allow dirty read.
	public void function testReadCommitted(){
		if(!variables.has) return;
		transaction isolation="read_committed" {
			query name="local.qry" {
				echo("SELECT table_name FROM user_tables where table_name like 'MAP_%'");
			}
		}
	}

	// repeatable_read: Allows phantom. Does not allow dirty read or non-repeatable read.
	private void function testRepeatableRead(){
		if(!variables.has) return;
		transaction isolation="repeatable_read" {
			query name="local.qry" {
				echo("SELECT table_name FROM user_tables where table_name like 'MAP_%'");
			}
		}
	}

	// serializable: Does not allow dirty read, non-repeatable read, or phantom.
	public void function testSerializable(){
		if(!variables.has) return;
		transaction isolation="serializable" {
			query name="local.qry" {
				echo("SELECT table_name FROM user_tables where table_name like 'MAP_%'");
			}
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
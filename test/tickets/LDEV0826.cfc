<!--- 
 *
 * Copyright (c) 2015, Lucee Assosication Switzerland. All rights reserved.
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

	public void function testTransaction() {
		if(!variables.has) return;

		names="";
		loop from=1 to=10 index="local.i" {
			names=listAppend(names,"manual#i#");
			names=listAppend(names,"auto#i#");
			thread name="manual#i#" {
				transaction isolation="read_uncommitted" {
				    query name="response.query" {
				    	echo("select 1 as one");
				    }
				}
			}
			thread name="auto#i#" {
				query name="response.query" {
					echo("select 1 as one");
				}
			}
		}
		thread action="join" name="#names#";

		// check the results
		var failures=[];
		loop list="#names#" item="local.name" {
			t=cfthread[name];
			if(t.status!="completed") arrayAppend(failures,t);
		}
		if(failures.len()) throw serialize(failures);

		assertTrue(failures.len()==0);		
	}

	private string function defineDatasource(){
		var mySQL=getCredencials();
		if(mySQL.count()==0) return false;
		application action="update" 
			datasource="#server.getDatasource("mysql")#";
	
		return true;
	}




	private struct function getCredencials() {
		// getting the credetials from the enviroment variables
		var mySQL={};
		if(
			!isNull(server.system.environment.MYSQL_SERVER) && 
			!isNull(server.system.environment.MYSQL_USERNAME) && 
			!isNull(server.system.environment.MYSQL_PASSWORD) && 
			!isNull(server.system.environment.MYSQL_PORT) && 
			!isNull(server.system.environment.MYSQL_DATABASE)) {
			mySQL.server=server.system.environment.MYSQL_SERVER;
			mySQL.username=server.system.environment.MYSQL_USERNAME;
			mySQL.password=server.system.environment.MYSQL_PASSWORD;
			mySQL.port=server.system.environment.MYSQL_PORT;
			mySQL.database=server.system.environment.MYSQL_DATABASE;
		}
		// getting the credetials from the system variables
		else if(
			!isNull(server.system.properties.MYSQL_SERVER) && 
			!isNull(server.system.properties.MYSQL_USERNAME) && 
			!isNull(server.system.properties.MYSQL_PASSWORD) && 
			!isNull(server.system.properties.MYSQL_PORT) && 
			!isNull(server.system.properties.MYSQL_DATABASE)) {
			mySQL.server=server.system.properties.MYSQL_SERVER;
			mySQL.username=server.system.properties.MYSQL_USERNAME;
			mySQL.password=server.system.properties.MYSQL_PASSWORD;
			mySQL.port=server.system.properties.MYSQL_PORT;
			mySQL.database=server.system.properties.MYSQL_DATABASE;
		}
		return mysql;
	}
} 
</cfscript>
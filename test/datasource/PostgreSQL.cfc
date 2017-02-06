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
		variables.has=defineDatasource();
	}

	public void function testConnection(){
		if(!variables.has) return;

		query name="local.qry" {
			echo("select 'AA' as a");
		}
		assertEquals("AA",qry.a);

	}

	public function testLDEV1063a() skip=true{

		if(!variables.has) return;

		// SELECT CAST(:election as date) AS election_date;
		query name="local.qry" params=[election:"2016-11-08"] { echo("
		    SELECT :election::date AS election_date;
		"); }

		assertEquals("2016-11-08", qry.election_date);
	}

	public function testLDEV1063b() skip=true{

		if(!variables.has) return;

		// SELECT CAST(? as date) AS election_date;
		query name="local.qry" params=[election:"2016-11-08"] { echo("
		    SELECT ?::date AS election_date;
		"); }

		assertEquals("2016-11-08", qry.election_date);
	}


	private boolean function defineDatasource(){
		var pgsql=getCredencials();
		if(pgsql.count()==0) return false;
		application action="update"
			datasource="#{
	  class: 'org.postgresql.Driver'
	, bundleName: 'org.postgresql.jdbc42'
	, bundleVersion: '9.4.1212'
	, connectionString: 'jdbc:postgresql://#pgsql.server#:#pgsql.port#/#pgsql.database#'
	, username: pgsql.username
	, password: pgsql.password
}#";
/*
	, bundleName: 'org.lucee.postgresql'
	, bundleVersion: '8.3.0.jdbc4'
*/
	return true;
	}

	private struct function getCredencials() {
		// getting the credetials from the enviroment variables
		var pgsql={};
		if(
			!isNull(server.system.environment.POSTGRE_SERVER) &&
			!isNull(server.system.environment.POSTGRE_USERNAME) &&
			!isNull(server.system.environment.POSTGRE_PASSWORD) &&
			!isNull(server.system.environment.POSTGRE_PORT) &&
			!isNull(server.system.environment.POSTGRE_DATABASE)) {
			pgsql.server=server.system.environment.POSTGRE_SERVER;
			pgsql.username=server.system.environment.POSTGRE_USERNAME;
			pgsql.password=server.system.environment.POSTGRE_PASSWORD;
			pgsql.port=server.system.environment.POSTGRE_PORT;
			pgsql.database=server.system.environment.POSTGRE_DATABASE;
		}
		// getting the credetials from the system variables
		else if(
			!isNull(server.system.properties.POSTGRE_SERVER) &&
			!isNull(server.system.properties.POSTGRE_USERNAME) &&
			!isNull(server.system.properties.POSTGRE_PASSWORD) &&
			!isNull(server.system.properties.POSTGRE_PORT) &&
			!isNull(server.system.properties.POSTGRE_DATABASE)) {
			pgsql.server=server.system.properties.POSTGRE_SERVER;
			pgsql.username=server.system.properties.POSTGRE_USERNAME;
			pgsql.password=server.system.properties.POSTGRE_PASSWORD;
			pgsql.port=server.system.properties.POSTGRE_PORT;
			pgsql.database=server.system.properties.POSTGRE_DATABASE;
		}
		return pgsql;
	}




}
</cfscript>
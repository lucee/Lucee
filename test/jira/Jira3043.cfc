<!--- 
 *
 * Copyright (c) 2016, Lucee Assosication Switzerland. All rights reserved.*
 * Copyright (c) 2014, the Railo Company LLC. All rights reserved.
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
	
	
	public function beforeTests(){
		// stash system timezone
		variables.timezone = getApplicationSettings().timezone;
	}

	public function afterTests(){
		// pop system timezone
		application action="update" timezone="#variables.timezone#";
		setTimeZone(variables.timezone);
	}

	public function setUp(){
		variables.has=defineDatasource();
	}

	public void function testMySQLWithBSTTimezone(){
		if(!variables.has) return;
		

		var tz1=getApplicationSettings().timezone;
		var tz2=getTimeZone();
		try{
			application action="update" timezone="BST";
			setTimeZone("BST");

			query name="local.qry" {
				echo("select 'a' as a");
			}
		}
		finally {
			application action="update" timezone="#tz1#";
			setTimeZone(tz2);
		}
		//assertEquals("","");
		
	}
	public void function testMySQLWithLondonTimezone(){
		if(!variables.has) return;
		
		application action="update" timezone="Europe/London";
		setTimeZone("Europe/London");
		
		query name="local.qry" {
			echo("select 'a' as a");
		}
		//assertEquals("","");
		
	}

	private string function defineDatasource(){
		var mySQL=getCredentials();
		if(mySQL.count()==0) return false;
		application action="update" datasource="#mySQL#";
		return true;
	}

	private struct function getCredentials() {
		return server.getDatasource("mysql");
	}
} 
</cfscript>
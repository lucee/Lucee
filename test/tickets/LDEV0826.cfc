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
component extends="org.lucee.cfml.test.LuceeTestCase"  labels="mysql" {
	
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
		var mySQL=server.getDatasource("mysql");
		if(mySQL.count()==0) return false;
		
		mySQL.connectionLimit = 100; // default:-1
		application action="update" datasource="#mySql#";
		return true;
	}	
} 
</cfscript>
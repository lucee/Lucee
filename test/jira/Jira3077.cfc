<!--- 
 *
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
		defineDatasource();

		try{
			query {
				echo("drop TABLE T3077");
			}
		}
		catch(local.e){}
		
		
		query  {
			echo("CREATE TABLE T3077 (");
			echo("id int NOT NULL,");
			echo("i int,");		
			echo("vc varchar(255)");		
			echo(") ");
		}
		
	}
	private string function defineDatasource(){
		application action="update" 
			datasource="#{
	  		class: 'org.hsqldb.jdbcDriver'
			, connectionString: 'jdbc:hsqldb:file:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db'
		}#";
	}

	public void function testNoSpace() {
		var qry=query(a:[1,2,3]);

		var qry=queryExecute(
		"insert into T3077(id, i, vc) values(:col0,:col1,:col2)",
		{col0:0, col1 = 1, col2 = 2, col3 = 3 }
		); 
	}
} 
</cfscript>
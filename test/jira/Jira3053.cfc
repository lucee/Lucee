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
				echo("drop TABLE T3053");
			}
		}
		catch(local.e){}
		
		
		query  {
			echo("CREATE TABLE T3053 (");
			echo("id int NOT NULL,");
			echo("i int,");		
			echo("vc varchar(255),");		
			echo("c char(1)");		
			echo(") ");
		}
		
	}

	public function afterTests(){
		try{
			query {
				echo("drop TABLE T3053");
			}
		}
		catch(local.e){}
	}

	public void function testQueryExecuteInsert() localMode="modern" {
		queryExecute("insert into T3053 (id,vc,c,i) values(1,'1','1',1)");
	}

	public void function testQueryExecuteUpdate() localMode="modern" {
		queryExecute("update T3053 set c='2'");
	}
	
	private string function defineDatasource(){
		application action="update" 
			datasource="#{
	  		class: 'org.hsqldb.jdbcDriver'
			, connectionString: 'jdbc:hsqldb:file:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/db'
		}#";
	}

} 
</cfscript>
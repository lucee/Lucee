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
	
	variables.suffix="Query";

	public function beforeTests(){
		defineDatasource();

		try{
			query {
				echo("drop TABLE T"&suffix);
			}
		}
		catch(local.e){}
		
		
		query  {
			echo("CREATE TABLE T"&suffix&" (");
			echo("id int NOT NULL,");
			echo("i int,");		
			echo("dec DECIMAL");		
			echo(") ");
		}
	}

	private string function defineDatasource(){
		application action="update" 
			datasource={
	  		class: 'org.h2.Driver'
	  		, bundleName: 'org.h2'
			, connectionString: 'jdbc:h2:#getDirectoryFromPath(getCurrentTemplatePath())#/datasource/update;MODE=MySQL'
		};
	}

	public void function testCachedWithinColumns() {
		query name="local.qry" cachedwithin="#createTimespan(0,0,0,1)#" {
			echo("select * from T"&suffix);
		} 
		var columnList1=qry.columnlist;
		queryAddColumn(qry,"susi");

		query name="local.qry" cachedwithin="#createTimespan(0,0,0,1)#" {
			echo("select * from T"&suffix);
		} 
		var columnList2=qry.columnlist;
		assertEquals(columnList1,columnList2);
		queryAddColumn(qry,"susi2");

	}
} 
</cfscript>
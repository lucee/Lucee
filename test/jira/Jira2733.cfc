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
			query  {
				echo("drop TABLE T2733");
			}
		}
		catch(local.e){}
		
		
		query  {
			echo("CREATE TABLE T2733 (");
			echo("id int NOT NULL,");
			echo("i int,");		
			echo("vc varchar(255),");		
			echo("c char(1)");		
			echo(") ");
		}
		
		query {
			echo("insert into T2733 (id,vc,c,i)");
			echo("values(1,'1','1',1)");
		}
		
		
	}

	public void function test(){
		
		var str="12";
		query name="local.qry" {
			echo("select * from T2733");
		}
		
		
		// char
		assertEquals(1,ListFind(ValueList(qry.c,","),str.charAt(0)));
		assertEquals(1,ListFind(ValueList(qry.vc,","),str.charAt(0)));
		assertEquals(1,ListFind(ValueList(qry.i,","),str.charAt(0)));
		
		// string/varchar
		assertEquals(1,ListFind(ValueList(qry.c,","),"1"));
		assertEquals(1,ListFind(ValueList(qry.vc,","),"1"));
		assertEquals(1,ListFind(ValueList(qry.i,","),"1"));
		
		// number
		assertEquals(1,ListFind(ValueList(qry.c,","),1));
		assertEquals(1,ListFind(ValueList(qry.vc,","),1));
		assertEquals(1,ListFind(ValueList(qry.i,","),1));
		
		// char from query
		assertEquals(1,ListFind(ValueList(qry.c,","),qry.c));
		assertEquals(1,ListFind(ValueList(qry.vc,","),qry.c));
		assertEquals(1,ListFind(ValueList(qry.i,","),qry.c));
		
		// int from query
		assertEquals(1,ListFind(ValueList(qry.c,","),qry.i));
		assertEquals(1,ListFind(ValueList(qry.vc,","),qry.i));
		assertEquals(1,ListFind(ValueList(qry.i,","),qry.i));
		
		// varchar from query
		assertEquals(1,ListFind(ValueList(qry.c,","),qry.vc));
		assertEquals(1,ListFind(ValueList(qry.vc,","),qry.vc));
		assertEquals(1,ListFind(ValueList(qry.i,","),qry.vc));
		
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
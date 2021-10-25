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
	
	
	//public function afterTests(){}
	
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


	public void function testTransactionCommit(){
		if(!variables.has) return;
		
			try {
				query name="qry" {
					echo("SHOW TABLES LIKE 'testCommit'");
				}
				if(qry.recordcount==0) {
					query {
						echo("CREATE TABLE testCommit (name VARCHAR(20));");
					}
				}

				query name="qry" {
					echo("select count(name) as cnt from testCommit");
				}
				assertEquals(0,qry.cnt);
				
				transaction {
					query {
						echo("insert into testCommit(name) values('aaa')");
					}
					transaction action="commit";
				}

				query name="qry" {
					echo("select count(name) as cnt from testCommit");
				}
				assertEquals(1,qry.cnt);
			}
			finally {
				try{
					query name="qry" {
						echo("drop table testCommit");
					}	
				}
				catch(e){}
				
			}
	}

	public void function testTransactionRollback(){
		if(!variables.has) return;
		
			try {
				query name="qry" {
					echo("SHOW TABLES LIKE 'testRollback'");
				}
				if(qry.recordcount==0) {
					query {
						echo("CREATE TABLE testRollback (name VARCHAR(20));");
					}
				}

				query name="qry" {
					echo("select count(name) as cnt from testRollback");
				}
				assertEquals(0,qry.cnt);
				
				transaction {
					query {
						echo("insert into testRollback(name) values('aaa')");
					}
					transaction action="rollback";
				}

				query name="qry" {
					echo("select count(name) as cnt from testRollback");
				}
				assertEquals(0,qry.cnt);
			}
			finally {
				try{
					query name="qry" {
						echo("drop table testRollback");
					}	
				}
				catch(e){}
			}
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

	public void function testStoredProcIn(){
		if(!variables.has) return;
		
		query {
			echo("DROP PROCEDURE IF EXISTS `proc_IN`");
		}
		query {
			echo("
CREATE PROCEDURE `proc_IN` (IN var1 INT)
BEGIN
    SELECT var1 + 2 AS result;
END
			");
		}

		storedproc procedure="proc_IN" {
			procparam type="in" cfsqltype="cf_sql_varchar" value="2";
			procresult name="local.rsIn" resultset="1";

		}
		assertTrue(isQuery(rsIn));
		assertEquals(1,rsIn.recordcount);
		assertEquals("result",rsIn.columnlist);
		assertEquals(4,rsIn.result);
		
	}

	public void function testStoredProcOut(){
		if(!variables.has) return;
		
		query {
			echo("DROP PROCEDURE IF EXISTS `proc_OUT`");
		}
		query {
			echo("
CREATE PROCEDURE `proc_OUT` (OUT var1 VARCHAR(100))
BEGIN
    SET var1 = 'outputvar';
END
			");
		}

		storedproc procedure="proc_OUT" {
			procparam type="out" variable="local.res" cfsqltype="cf_sql_varchar";
		}
		assertEquals("outputvar",res);
		
	}

	public void function testStoredProcInOut(){
		if(!variables.has) return;
		
		query {
			echo("DROP PROCEDURE IF EXISTS `proc_INOUT`");
		}
		query {
			echo("
CREATE PROCEDURE `proc_INOUT` (INOUT var1 INT)
BEGIN
    SET var1 = var1 * 2;
END
			");
		}

		storedproc procedure="proc_INOUT" {
			procparam  type="inout" variable="local.res" cfsqltype="cf_sql_varchar" value="10";
		}
		assertEquals(20,res);


		storedproc procedure="proc_INOUT" datasource=getDatasource() {
			procparam  type="inout" variable="local.res" cfsqltype="cf_sql_varchar" value="10";
		}
		
	}

	public void function testType(){
		if(!defineDatasourceX()) return;
		
		query datasource="x" { 
			echo("show tables");
		}
		
	}

	function testExceptionOnAccessDenied(){
		// test mysql user cannot access or drop other databases
		if(!variables.has) return;
		expect(function(){
			query  {
				echo( "DROP DATABASE IF EXISTS `database_doesnt_exist` ");
			}
		}).toThrow();
	}

	private boolean function defineDatasource(){
		var sct=getDatasource();
		if(sct.count()==0) return false;
		application action="update" datasource=sct;
		return true;
	}
	private boolean function defineDatasourceX(){
		var sct=getDatasource2();
		if(sct.count()==0) return false;
		application action="update" datasources={'x':sct};
		return true;
	}


	private struct function getDatasource(){
			var mySQL=getCredencials();
			if(mySQL.count()==0) return {};
			
			return server.getDatasource("mysql");
	}

	private struct function getDatasource2(){
			var mySQL=getCredencials();
			if(mySQL.count()==0 || isEmpty(mySQL.server?:"")) return {};
			
			return {
			  type= 'mysql'
			, host=mySQL.server
			, port=mySQL.port
			, database=mySQL.database
			, username= mySQL.username
			, password= mySQL.password
		 	, custom= { useUnicode:true }
			};

	}

	private struct function getCredencials() {
		return server.getDatasource("mysql");
	}

} 
</cfscript>
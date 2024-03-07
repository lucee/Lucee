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
component extends="org.lucee.cfml.test.LuceeTestCase"  labels="mysql" 	{
	
	processingdirective pageEncoding="UTF-8";

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

	public void function testEmojisDefault() {
		testEmojis();
	}

	public void function testEmojis8019() {
		testEmojis("8.0.19");
	}

	public void function testEmojis8033() {
		testEmojis("8.0.33");
	}


	/**
	 * Verify that the MySQL JDBC driver correctly handles, stores, and retrieves emojis in a MySQL database.
	 */
	private void function testEmojis(version="") {
		if(!variables.has) return;

			var datasourceName="ds"&createUniqueID();
			defineDatasource(arguments.version,datasourceName);

			try {
				// starting with a clean slate
				query datasource=datasourceName {```
					DROP TABLE IF EXISTS emoji_test;
				```}

				query datasource=datasourceName {```
					CREATE TABLE IF NOT EXISTS emoji_test (
						id INT AUTO_INCREMENT PRIMARY KEY,
						varchar_reg VARCHAR(255) NOT NULL,
						varchar_utf8mb4 VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL
					);
				```}
			

				var emojis="👋🌍";

				query datasource=datasourceName {
					echo("INSERT INTO emoji_test (varchar_reg,varchar_utf8mb4) VALUES ('#emojis#','#emojis#');");
				}
				
				query datasource=datasourceName name="local.qry" {```
					SELECT * FROM emoji_test;
				```}
				
				assertEquals(emojis,qry.varchar_reg);
				assertEquals(emojis,qry.varchar_utf8mb4);
				debug(qry);
			}
			finally {
				try { // we don't care if that fails
					query datasource=datasourceName {```
						DROP TABLE IF EXISTS emoji_test;
					```}
				}
				catch(e){}
			}
	}


	public void function testTypesDefault() {
		testTypes();
	}

	public void function testTypes8019() {
		testTypes("8.0.19");
	}

	public void function testTypes8033() {
		testTypes("8.0.33");
	}

	/**
	 * test types
	 */
	private void function testTypes(version="") {
		if(!variables.has) return;

			var datasourceName="ds"&createUniqueID();
			defineDatasource(arguments.version,datasourceName);

			var MinInt="-2147483648";
			var MaxInt="2147483647";
			var MinUnsignedInt="0";
			var UnsignedMaxInt="4294967295";
			var MinBigInt="-9223372036854775808";
			var MaxBigInt="9223372036854775807";
			var MinUnsignedBigInt="0";
			var UnsignedMaxBigInt="18446744073709551615";
			var MinFloat="-3.402823466E+38";
			var MaxFloat="3.402823466E+38";
			var MinDouble="-1.7976931348623157E+308";
			var MaxDouble="1.7976931348623157E+308";
			var MinDecimal="-99999999999999.9999";
			var MaxDecimal="99999999999999.9999";


			try {

				query name="local.qry" datasource=datasourceName {
					echo("
					SELECT 
						CAST(#MinInt# AS SIGNED INTEGER) AS MinInt, 
						CAST(#MaxInt# AS SIGNED INTEGER) AS MaxInt, -- INT (signed)
						CAST(#MinUnsignedInt# AS UNSIGNED INTEGER) AS MinUnsignedInt, 
						CAST(#UnsignedMaxInt# AS UNSIGNED INTEGER) AS UnsignedMaxInt, -- INT (unsigned)
						CAST(#MinBigInt# AS SIGNED) AS MinBigInt, 
						CAST(#MaxBigInt# AS SIGNED) AS MaxBigInt, -- BIGINT (signed)
						CAST(#MinUnsignedBigInt# AS UNSIGNED) AS MinUnsignedBigInt, 
						CAST(#UnsignedMaxBigInt# AS UNSIGNED) AS UnsignedMaxBigInt, -- BIGINT (unsigned)
						CAST(#MinFloat# AS FLOAT) AS MinFloat, 
						CAST(#MaxFloat# AS FLOAT) AS MaxFloat, -- FLOAT
						CAST(#MinDouble# AS DOUBLE) AS MinDouble, 
						CAST(#MaxDouble# AS DOUBLE) AS MaxDouble, -- DOUBLE
						CAST(#MinDecimal# AS DECIMAL(18,4)) AS MinDecimal, 
						CAST(#MaxDecimal# AS DECIMAL(18,4)) AS MaxDecimal -- DECIMAL
					;");
				}
				assertEquals("java.lang.String",qry.MinInt[1].getClass().getName());
				assertEquals(MaxInt,qry.MaxInt);
				assertEquals(MaxInt,""&qry.MaxInt);

				assertEquals("java.lang.Double",qry.MinUnsignedInt[1].getClass().getName());
				assertEquals(MinUnsignedInt,qry.MinUnsignedInt);
				assertEquals(MinUnsignedInt,""&qry.MinUnsignedInt);

				assertEquals("java.lang.String",qry.UnsignedMaxInt[1].getClass().getName());
				assertEquals(UnsignedMaxInt,qry.UnsignedMaxInt);
				assertEquals(UnsignedMaxInt,""&qry.UnsignedMaxInt);

				assertEquals("java.lang.String",qry.MinBigInt[1].getClass().getName());
				assertEquals(MinBigInt,qry.MinBigInt);
				assertEquals(MinBigInt,""&qry.MinBigInt);
				
				assertEquals("java.lang.String",qry.MaxBigInt[1].getClass().getName());
				assertEquals(MaxBigInt,qry.MaxBigInt);
				assertEquals(MaxBigInt,""&qry.MaxBigInt);
				
				assertEquals("java.lang.Double",qry.MinUnsignedBigInt[1].getClass().getName());
				assertEquals(MinUnsignedBigInt,qry.MinUnsignedBigInt);
				assertEquals(MinUnsignedBigInt,""&qry.MinUnsignedBigInt);
				
				assertEquals("java.lang.String",qry.UnsignedMaxBigInt[1].getClass().getName());
				assertEquals(UnsignedMaxBigInt,qry.UnsignedMaxBigInt);
				assertEquals(UnsignedMaxBigInt,""&qry.UnsignedMaxBigInt);
				
				// ATM we only test the types, because there is an issue with float that need fixing first
				assertEquals("java.lang.Float",qry.MinFloat[1].getClass().getName());
				//assertEquals(MinFloat,qry.MinFloat);
				//assertEquals(MinFloat,""&qry.MinFloat);
				
				// ATM we only test the types, because there is an issue with float that need fixing first
				assertEquals("java.lang.Float",qry.MaxFloat[1].getClass().getName());
				//assertEquals(MaxFloat,qry.MaxFloat);
				//assertEquals(MaxFloat,""&qry.MaxFloat);
				
				assertEquals("java.lang.Double",qry.MinDouble[1].getClass().getName());
				assertEquals(MinDouble,qry.MinDouble);
				assertEquals(MinDouble,""&qry.MinDouble);
				
				assertEquals("java.lang.Double",qry.MaxDouble[1].getClass().getName());
				assertEquals(MaxDouble,qry.MaxDouble);
				assertEquals(MaxDouble,""&qry.MaxDouble);
				
				assertEquals("java.math.BigDecimal",qry.MinDecimal[1].getClass().getName());
				assertEquals(MinDecimal,qry.MinDecimal);
				assertEquals(MinDecimal,""&qry.MinDecimal);
				
				assertEquals("java.math.BigDecimal",qry.MaxDecimal[1].getClass().getName());
				assertEquals(MaxDecimal,qry.MaxDecimal);
				assertEquals(MaxDecimal,""&qry.MaxDecimal);
			}
			finally {
				
			}
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
		if(!defineDatasource("","x")) return;
		
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

	private boolean function defineDatasource(version="",datasourceName=""){
		var sct=getDatasource(arguments.version);
		if(sct.count()==0) return false;

		// no specific version, just use whatever is installed
		if(isEmpty(arguments.datasourceName)) {
			application action="update" datasource=sct;
			return true;
		}
		// we have a specific version
		else {
			var datasources={};
			datasources[datasourceName]=sct;
			application action="update" datasources=datasources;
			return true;
		}
		
	}

	private struct function getDatasource(version="", useUnicode="") {
		var data = server.getDatasource("mysql");
		
		// let's inject a specific version
		if(!isEmpty(arguments.version)) {
			data.bundleVersion=arguments.version;
		}
		if(!isEmpty(arguments.useUnicode)) {
			data.custom= { useUnicode:arguments.useUnicode };
			data.type= 'mysql';
		}
		return data;
	}
} 
</cfscript>

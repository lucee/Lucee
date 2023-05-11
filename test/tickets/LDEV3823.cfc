component extends = "org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults, textbox ) {

		describe("testcase for LDEV-3823", function(){

			it(title="Select the same column twice without error in QoQ ORDER BY using an ordinal position ORDER BY", body=function( currentSpec ){
				var employees = queryNew( 'name,age,email', 'varchar,integer,varchar',[
					['Brad',20,'brad@test.com'],
					['Luis',10,'luis@test.com']
				]);

				var actual = QueryExecute(
					sql = "SELECT DISTINCT age, email, age from employees ORDER BY 1",
					options = { dbtype: 'query' }
				);
				
				expect( actual ).toBeQuery();
				expect( actual.recordCount ).toBe( 2 );
				expect( queryColumnData( actual, 'age' )[2] ).toBe( 20 );

			});

			it(title="QoQ ORDER BY column named with actual number", body=function( currentSpec ){
				var employees = queryNew( 'name,45', 'varchar,integer',[
					['Brad',20],
					['Luis',10]
				]);

				// make sure we don't confuse the Literal 45 with the actual column named "45"
				var actual = QueryExecute(
					sql = "SELECT name, 45, [45] from employees ORDER BY [45]",
					options = { dbtype: 'query' }
				);
				
				expect( actual ).toBeQuery();
				expect( queryColumnData( actual, '45' )[2] ).toBe( 20 );

			});

			it(title="QoQ ORDER BY column named with actual boolean", body=function( currentSpec ){
				var employees = queryNew( 'name,true,false', 'varchar,varchar,varchar',[
					['Brad','yeah','nah1'],
					['Luis','yeah','nah2']
				]);

				// make sure we don't confuse the Literal true/false with the actual column named "true"/"false"
				var actual = QueryExecute(
					sql = "SELECT name, true, false, [true], [false] from employees ORDER BY [true], [false] desc",
					options = { dbtype: 'query' }
				);
				
				expect( actual ).toBeQuery();
				expect( queryColumnData( actual, 'false' )[1] ).toBe( 'nah2' );
				expect( queryColumnData( actual, 'false' )[2] ).toBe( 'nah1' );

			});

			it(title="QoQ ORDER BY using an ordinal position ORDER BY out of range", body=function( currentSpec ){
				var employees = queryNew( 'name,age,email', 'varchar,integer,varchar',[
					['Brad',20,'brad@test.com'],
					['Luis',10,'luis@test.com']
				]);

				expect( ()=>QueryExecute(
					sql = "SELECT age from employees ORDER BY 99",
					options = { dbtype: 'query' }
				) ).toThrow();

			});

			it(title="QoQ ORDER BY using a string constant", body=function( currentSpec ){
				var employees = queryNew( 'name,age,email', 'varchar,integer,varchar',[
					['Brad',20,'brad@test.com'],
					['Luis',10,'luis@test.com']
				]);

				expect( ()=>QueryExecute(
					sql = "SELECT age from employees ORDER BY 'test'",
					options = { dbtype: 'query' }
				) ).toThrow();

			});

			it(title="QoQ ORDER BY using a boolean constant", body=function( currentSpec ){
				var employees = queryNew( 'name,age,email', 'varchar,integer,varchar',[
					['Brad',20,'brad@test.com'],
					['Luis',10,'luis@test.com']
				]);

				// Lucee will try to convert 'false' to 0 so make sure that isn't allowed
				expect( ()=>QueryExecute(
					sql = "SELECT age from employees ORDER BY false",
					options = { dbtype: 'query' }
				) ).toThrow();

				// Lucee will try to convert 'true' to 1 so make sure that isn't allowed
				expect( ()=>QueryExecute(
					sql = "SELECT age from employees ORDER BY true",
					options = { dbtype: 'query' }
				) ).toThrow();

			});

		});

	}

}
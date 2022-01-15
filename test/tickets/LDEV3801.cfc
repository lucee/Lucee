component extends = "org.lucee.cfml.test.LuceeTestCase" {

    function run( testResults, textbox ) {

        describe("testcase for LDEV-3801", function(){

            it(title="can select the same column twice without error in QoQ ORDER BY", body=function( currentSpec ){
                var employees = queryNew( 'name,age,email', 'varchar,integer,varchar',[
					['Brad',10,'brad@test.com'],
					['Luis',20,'luis@test.com']
				]);

				var actual = QueryExecute(
					sql = "SELECT name, age, name from employees ORDER BY age, email",
					options = { dbtype: 'query' }
				);
			
				expect( actual ).toBeQuery();
				expect( actual.recordCount ).toBe( 2 );

            });

            it(title="can select the same column twice without error in QoQ ORDER BY again", body=function( currentSpec ){
                var employees = queryNew( 'name,age,email', 'varchar,integer,varchar',[
					['Brad',10,'brad@test.com'],
					['Luis',20,'luis@test.com']
				]);

				var actual = QueryExecute(
					sql = "SELECT DISTINCT age, name, age, email from employees ORDER BY age, email",
					options = { dbtype: 'query' }
				);
			
				expect( actual ).toBeQuery();
				expect( actual.recordCount ).toBe( 2 );

            });


        
		});

    }

}
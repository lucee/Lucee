component extends = "org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults, textbox ) {

		describe("testcase for LDEV-3822", function(){

			it(title="throws exception when using select distinct and ordering by a column not in the select list", body=function( currentSpec ){
				var employees = queryNew( 'name,age', 'varchar,integer',
					[ [ 'John Doe',28 ],
					[ 'Jane Doe',28 ],
					[ 'Bane Doe',28 ]] );
				
				expect( ()=>QueryExecute(
					sql = "SELECT DISTINCT age from employees ORDER BY age, name",
					queryoptions = { dbtype: 'query' }
				) ).toThrow();

			});

		});

	}

}
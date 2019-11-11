component extends = "org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "test suite for LDEV-2541", function() {
			it(title = "Query of Query UNION doesn't distinct the resultset ", body = function( currentSpec ) {
				qry1 = queryNew( 'Name,File', 'varchar,varchar', [{'Name':'Lucee','File':'test'}] );
				qry2 = queryNew( 'Name,File', 'varchar,varchar', [{'Name':'Lucee','File':'test'}] );
				query name="qry" dbtype="query" {
					echo(
						'SELECT * FROM qry1 UNION SELECT * FROM qry2'
					);
				}
				expect(qry.recordcount).tobe('1');
			});
		});
	}
} 
component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {

	function run( testResults , testBox ) {
		describe( title='LDEV-5067', body=function(){

			it( title='test QOQ query parsing, with comments in quotes ', body=function() {

				var params = {
					id: {
						value: 1, sqltype="numeric"
					}
				};

				var qry= queryNew( "engine,id", "varchar,numeric", [
					[ "lucee", 1 ],
					[ "ralio" , 2 ]
				]);

				query name="local.q" dbtype="query" params="#params#" result="local.result" {
					echo ( "SELECT engine, '/* multi */  // -- single' AS comments FROM qry WHERE id = :id");
				};

				expect( q.recordcount ).toBe( 1 );
				expect( q.engine ).toBe( "lucee" );
				expect( q.comments ).toBe( "/* multi */  // -- single" );
			});

		});
	}

}
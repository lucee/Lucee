// this is simply crappy code, and will never be supported by lucee, WONT FIX
component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq" {
	function run( testResults , testBox ) {
		describe( "test suite for LDEV-2530", function() {
			it(title = "Query returns single column instead of multiple columns with the same name (WONTFIX)", body = function( currentSpec ) {
				a = querynew("id","integer",[{"id": 1},{"id": 2},{"id": 3}]);
				b = duplicate(a);
				query name="qry" dbtype="query" {
					echo(
						"SELECT a.id, b.id FROM a,b where b.ID = a.ID and b.ID IN (2)"
					);
				}
				expect(listlen(qry.columnlist)).tobe('2');
			});
		});
	}
}
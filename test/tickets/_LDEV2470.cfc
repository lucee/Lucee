component extends = "org.lucee.cfml.test.LuceeTestCase" {
	function run(){
		describe( "Test suite for LDEV-2470", function() {
			it( title = "QoQ joins on columns with type integer does work", body = function( currentSpec ){
				cfloop( list = "integer",item = "int_type" ){
					a = querynew("id","#int_type#",[{"id": 1},{"id": 2},{"id": 3}]);
					b = duplicate(a);
					cfquery(name = "int_query",dbtype = "query") {
						echo(
							"SELECT a.id, b.id FROM a,b where b.ID = a.ID and b.ID IN (1)"
						)
					}
					expect(int_query.id).tobe(1);
				}
			});

			it( title = "QoQ joins on columns with type numeric doesn't work", body = function( currentSpec ){
				cfloop( list = "numeric",item = "num_type" ){
					a = querynew("id","#num_type#",[{"id": 4},{"id": 5},{"id": 6}]);
					b = duplicate(a);
					cfquery(name = "num_query",dbtype = "query") {
						echo(
							"SELECT a.id, b.id FROM a,b where b.ID = a.ID and b.ID IN (4)"
						)
					}
					expect(num_query.id).tobe(4);
				}
			});
		});
	}
}
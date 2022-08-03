component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq"{
	function run( testResults , testBox ) {
		describe( "test case for LDEV-2128", function() {
			it(title = "Query of Queries UNION returns incorrect results with cfqueryparam", body = function( currentSpec ) {
        		var testQuery = QueryNew('abcd');
        		QueryAddRow(testQuery);
        		query name="local.qry" dbtype="query" params={paramValue:{type:'cf_sql_integer',value:'1'}} {
					echo("
						select :paramValue as Number, 'a' as Letter from testQuery
						union
						select :paramValue as Number, 'b' as Letter from testQuery

					");
				}
				expect(local.qry.Number[1]).toBe(1);
				expect(local.qry.Number[2]).toBe(1);
				expect(local.qry.letter[1]).toBe('a');
				expect(local.qry.letter[2]).toBe('b');
			});

			it(title = "Query of Queries UNION returns correct results without cfqueryparam", body = function( currentSpec ) {
        		var testQuery = QueryNew('abcd');
        		QueryAddRow(testQuery);
        		query name="local.qry" dbtype="query" {
					echo("
						select 1 as Number, 'a' as Letter from testQuery
						union
						select 1 as Number, 'b' as Letter from testQuery

					");
				}
				expect(local.qry.Number[1]).toBe(1);
				expect(local.qry.Number[2]).toBe(1);
				expect(local.qry.letter[1]).toBe('a');
				expect(local.qry.letter[2]).toBe('b');
			});
		});
	}
}
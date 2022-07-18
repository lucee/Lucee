component extends="org.lucee.cfml.test.LuceeTestCase" labels="qoq"{
	function beforeAll(){
		variables.record1 = QueryNew('zap,bar,foo','integer,integer,integer',[[1,2,3],[4,5,6]]);
		variables.record2 = QueryNew('zap,bar,foo','integer,integer,integer',[[1,2,3],[4,5,6]]);
	}

	function run( testResults , testBox ) {
		describe( title="Test suite for LDEV-1867", body=function() {
			it(title = "Checking alias name as different from column name", body = function( currentSpec ) {
				query name="local.qry" dbtype="query"{
					echo( "select record2.foo as bar
							    from record1, record2
							    where record1.bar = record2.bar");
				}
				expect(local.qry.RecordCount).toBe("2");
			});

			it(title = "Checking alias name same as column a name", body = function( currentSpec ) {
				query name="local.qry" dbtype="query"{
					echo( "select record2.foo as foo
						from record1, record2
						where record1.foo = record2.foo");
				}
				expect(local.qry.RecordCount).toBe("2");
			});

			it(title = "Checking alias name as not used in record set", body = function( currentSpec ) {
				query name="local.qry" dbtype="query"{
					echo( "select record2.bar as test1
						from record1, record2
						where record1.zap = record2.zap");
				}
				expect(local.qry.RecordCount).toBe("2");
			});
		});
	}
}
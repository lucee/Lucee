component extends="org.lucee.cfml.test.LuceeTestCase" {
	function run( testResults , testBox ) {
		describe(title="Testcase for LDEV-3826", body=function() {
			it(title="Checking QoQ with [] regex charsets in LIKE operator", body=function( currentSpec ) {
				var employees = queryNew('name,foo', 'varchar,varchar', [['Brad','cm_4test5'], ['Luis','yeah']]);
				var actual = queryExecute(
					sql = "SELECT * FROM employees WHERE foo LIKE 'cm_[0-9]%[0-9]'",
					queryoptions = { dbtype: 'query' }
				);
				expect(actual.recordcount).tobe('1');
				expect(actual.name).tobe('Brad');
				expect(actual.foo).tobe('cm_4test5');
			});
		});
	}
}
component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults , testBox ) {

		describe(title="Testcase for LDEV-3826", body=function() {
			
			it(title="Checking QoQ with [] regex charsets in LIKE operator", body=function( currentSpec ) {
				var employees = queryNew('name,foo', 'varchar,varchar', [['Brad','cm_4test5'], ['Luis','yeah']]);
				var actual = queryExecute(
					sql = "SELECT * FROM employees WHERE foo LIKE 'cm_[0-9]%[0-9]'",
					queryoptions = { dbtype: 'query' }
				);
				expect(actual.recordcount).tobe(1);
				expect(actual.name).tobe('Brad');
				expect(actual.foo).tobe('cm_4test5');
			});
			
			it(title="Can escape legit [] chars in like pattern", body=function( currentSpec ) {
				var employees = queryNew('name,foo', 'varchar,varchar', [['Brad','brad [the guy] wood'], ['Luis','yeah']]);
				var actual = queryExecute(
					sql = "SELECT * FROM employees WHERE foo LIKE '%\[the guy\]%' ESCAPE '\'",
					queryoptions = { dbtype: 'query' }
				);
				expect(actual.recordcount).tobe(1);
				expect(actual.name).tobe('Brad');
				expect(actual.foo).tobe('brad [the guy] wood');
			});
			
			it(title="Spurious escape chars are ignored", body=function( currentSpec ) {
				var employees = queryNew('name,foo', 'varchar,varchar', [['Brad','bradwood'], ['Luis','yeah']]);
				var actual = queryExecute(
					sql = "SELECT * FROM employees WHERE foo LIKE 'brad\wood' ESCAPE '\'",
					queryoptions = { dbtype: 'query' }
				);
				expect(actual.recordcount).tobe(1);
				expect(actual.name).tobe('Brad');
				expect(actual.foo).tobe('bradwood');
			});
			
			it(title="Extra escape chars at end of pattern are treated as litearl", body=function( currentSpec ) {
				var employees = queryNew('name,foo', 'varchar,varchar', [['Brad','bradwood\'], ['Luis','yeah']]);
				var actual = queryExecute(
					sql = "SELECT * FROM employees WHERE foo LIKE 'bradwood\' ESCAPE '\'",
					queryoptions = { dbtype: 'query' }
				);
				expect(actual.recordcount).tobe(1);
				expect(actual.name).tobe('Brad');
				expect(actual.foo).tobe('bradwood\');
			});
			
			it(title="Use custom escape char", body=function( currentSpec ) {
				var employees = queryNew('name,foo', 'varchar,varchar', [['Brad','b%r_a[d]wood'], ['Luis','yeah']]);
				var actual = queryExecute(
					sql = "SELECT * FROM employees WHERE foo LIKE 'b^%r^_a^[d^]wood' ESCAPE '^'",
					queryoptions = { dbtype: 'query' }
				);
				expect(actual.recordcount).tobe(1);
				expect(actual.name).tobe('Brad');
				expect(actual.foo).tobe('b%r_a[d]wood');
			});
			
			it(title="testing char sets", body=function( currentSpec ) {
				var employees = queryNew('name,foo', 'varchar,varchar', [['Brad','brad'], ['Luis','bred']]);
				var actual = queryExecute(
					sql = "SELECT * FROM employees WHERE foo LIKE 'br[ae]d'",
					queryoptions = { dbtype: 'query' }
				);
				expect(actual.recordcount).tobe(2);
			});
			
			it(title="testing more char sets", body=function( currentSpec ) {
				var employees = queryNew('name,foo', 'varchar,varchar', [['Brad','brad'], ['Luis','foobar']]);
				var actual = queryExecute(
					sql = "SELECT * FROM employees WHERE foo LIKE '[f][o][o][b][a][r]'",
					queryoptions = { dbtype: 'query' }
				);
				expect(actual.recordcount).tobe(1);
				expect(actual.name).tobe('Luis');
				expect(actual.foo).tobe('foobar');
			});
			
			it(title="negated char sets", body=function( currentSpec ) {
				var employees = queryNew('name,foo', 'varchar,varchar', [['Brad','brad'], ['Luis','bred']]);
				var actual = queryExecute(
					sql = "SELECT * FROM employees WHERE foo LIKE 'br[^a]d'",
					queryoptions = { dbtype: 'query' }
				);
				expect(actual.recordcount).tobe(1);
				expect(actual.name).tobe('Luis');
				expect(actual.foo).tobe('bred');
			});
			
			it(title="no default escape char", body=function( currentSpec ) {
				var employees = queryNew('name,foo', 'varchar,varchar', [['Brad','br\ud'], ['Luis','yeah']]);
				// The char set will still be used, but the backslash chars will be used as litearls
				var actual = queryExecute(
					sql = "SELECT * FROM employees WHERE foo LIKE 'br\[aeiou\]d'",
					queryoptions = { dbtype: 'query' }
				);
				expect(actual.recordcount).tobe(1);
				expect(actual.name).tobe('Brad');
				expect(actual.foo).tobe('br\ud');
			});

		});

	}

} 
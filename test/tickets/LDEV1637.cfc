component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults , testBox ) {
		describe( "Test Case for LDEV1637", function() {
			it( title='Checking evaluate() with keyArray', body=function( currentSpec ) {
				var hasError = false;
				try {
					evaluate( '( {foo:2}.keyArray() )' );
				} catch( any e ) {
					var hasError = true;
				}
				expect(hasError).toBe('false');
			});
			it( title='Checking evaluate() with tolist', body=function( currentSpec ) {
				var hasError = false;
				try {
					evaluate( '( [1,2,3].tolist() )' );
				} catch( any e ) {
					var hasError = true;
				}
				expect(hasError).toBe('false');
			});
			it( title='Checking evaluate() with listToArray', body=function( currentSpec ) {
				var hasError = false;
				try {
					evaluate( '( "1,2,3".listToArray() )' );
				} catch( any e ) {
					var hasError = true;
				}
				expect(hasError).toBe('false');
			});
			it( title='Checking evaluate() with count', body=function( currentSpec ) {
				var hasError = false;
				try {
					evaluate('({foo:2}.count())');
				} catch( any e ) {
					var hasError = true;
				}
				expect(hasError).toBe('false');
			});
			it( title='Checking evaluate() with append', body=function( currentSpec ) {
				var hasError = false;
				try {
					evaluate('({foo:2}.append({bar:3}))');
				} catch( any e ) {
					var hasError = true;
				}
				expect(hasError).toBe('false');
			});
			it( title='Checking evaluate() with keyList', body=function( currentSpec ) {
				var hasError = false;
				try {
					evaluate('({aaa:2, bbb:3, ccc=4}.keyList())');
				} catch( any e ) {
					var hasError = true;
				}
				expect(hasError).toBe('false');
			});
			it( title='Checking evaluate() with findkey', body=function( currentSpec ) {
				var hasError = false;
				try {
					evaluate('({foo:{test1:"aa",test2:"bb"},bar:{test1:"aa"}}.findkey("test2","all"))');
				} catch( any e ) {
					var hasError = true;
				}
				expect(hasError).toBe('false');
			});
		});
	}
}

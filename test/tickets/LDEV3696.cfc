component extends="org.lucee.cfml.test.LuceeTestCase" labels="closure" {

	function run( testResults, testBox ) {
		describe( title="LDEV-3696 variables scope inside a closure", body=function( currentSpec ) {

			it( title="variables.x in a closure does not return an argument of the enclosing function", body=function( currentSpec, foo="FOO from argument scope" ) {
				variables.foo = "FOO from variables scope";
				variables.bar = "BAR from variables scope";
				var res1 = variables.foo;
				var res2 = "";
				var res3 = "";
				function() {
					res2 = variables.bar;
					res3 = variables.foo;
				}();
				expect( res1 ).toBe( "FOO from variables scope" );
				expect( res2 ).toBe( "BAR from variables scope" );
				expect( res3 ).toBe( "FOO from variables scope" );
			});

			it( title="variables.x in a closure does not return a local of the enclosing function", body=function( currentSpec ) {
				variables.ldev3696_local = "variables";
				var ldev3696_local = "local";
				var res = ( function() {
					return variables.ldev3696_local;
				})();
				expect( res ).toBe( "variables" );
			});

			it( title="nested closures and arrow functions", body=function( currentSpec ) {
				expect( testNested( "arguments" ) ).toBe( "variables|variables|arguments" );
			});

			it( title="variables.x = in a closure writes the variables scope", body=function( currentSpec, ldev3696_write="arguments" ) {
				variables.ldev3696_write = "variables";
				function() {
					variables.ldev3696_write = "changed";
				}();
				expect( arguments.ldev3696_write ).toBe( "arguments" );
				expect( variables.ldev3696_write ).toBe( "changed" );
			});

			it( title="structKeyExists(variables) in a closure only sees the variables scope", body=function( currentSpec, ldev3696_arg="arguments" ) {
				var res = ( function() {
					return structKeyExists( variables, "ldev3696_arg" ) & "|" & isNull( variables.ldev3696_arg );
				})();
				expect( res ).toBe( "false|true" );
			});

			it( title="unscoped access in a closure still finds the enclosing arguments and local", body=function( currentSpec, ldev3696_un="arguments" ) {
				variables.ldev3696_un = "variables";
				var ldev3696_loc = "local";
				var res = ( function() {
					return ldev3696_un & "|" & ldev3696_loc;
				})();
				expect( res ).toBe( "arguments|local" );
				function() {
					ldev3696_un = "changed";
				}();
				expect( arguments.ldev3696_un ).toBe( "changed" );
				expect( variables.ldev3696_un ).toBe( "variables" );
			});
		});
	}

	private function testNested( ldev3696_nested ) {
		variables.ldev3696_nested = "variables";
		var outer = function( ldev3696_nested="outerArg" ) {
			var innermost = () => variables.ldev3696_nested;
			var inner = function() {
				return variables.ldev3696_nested & "|" & innermost();
			};
			return inner();
		};
		return outer() & "|" & ldev3696_nested;
	}
}

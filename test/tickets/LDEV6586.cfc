component extends="org.lucee.cfml.test.LuceeTestCase" labels="config" {

	function beforeAll() {
		variables.cfg = getPageContext().getConfig();
		variables.configUtil = createObject( "java", "lucee.runtime.config.ConfigUtil" );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-6586 config placeholder replacement keeps ordered structs ordered", function() {

			it( title="keeps the key order of an ordered struct", body=function( currentSpec ) {
				var repl = variables.configUtil.replacePlaceHolders( variables.cfg, [ "z": 1, "c": 2, "a": 3, "m": 4 ] );
				expect( structKeyArray( repl ) ).toBe( [ "z", "c", "a", "m" ] );
			});

			it( title="keeps the key order of nested ordered structs, i.e. an AI connection's custom", body=function( currentSpec ) {
				var src = [
					"component": "Engine",
					"custom": [ "options": [ "y": 1, "b": 2, "x": 3 ] ]
				];
				var repl = variables.configUtil.replacePlaceHolders( variables.cfg, src );
				expect( structKeyArray( repl ) ).toBe( [ "component", "custom" ] );
				expect( structKeyArray( repl.custom.options ) ).toBe( [ "y", "b", "x" ] );
			});

			it( title="still replaces placeholders while keeping the order", body=function( currentSpec ) {
				// $$$ is an invalid name, so it always falls back to the default
				var repl = variables.configUtil.replacePlaceHolders( variables.cfg, [ "z": "${$$$:haha}", "c": 2, "a": 3 ] );
				expect( structKeyArray( repl ) ).toBe( [ "z", "c", "a" ] );
				expect( repl.z ).toBe( "haha" );
			});

		});
	}
}

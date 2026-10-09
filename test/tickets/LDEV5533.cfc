component extends="org.lucee.cfml.test.LuceeTestCase" labels="syntax" {

	function beforeAll() {
		variables.dir = getDirectoryFromPath( getCurrentTemplatePath() ) & "LDEV5533/";
		afterAll();
		directoryCreate( variables.dir & "withCfc/", true );
		directoryCreate( variables.dir & "noCfc/", true );
		// a component that is literally called "component", and a caller next to it.
		// "component" also matches the built-in org.lucee.cfml.Component (default import org.lucee.cfml.*),
		// which wins once it is in the import cache, so the specs compare both spellings instead of expecting this file
		fileWrite( variables.dir & "withCfc/component.cfc", 'component { function hi() { return "component.cfc"; } }' );
		fileWrite( variables.dir & "withCfc/Caller.cfc", 'component {
			function withSpace() { var x = new component (); return getMetadata( x ).path; }
			function withoutSpace() { var x = new component(); return getMetadata( x ).path; }
		}' );
		// syntax from the ticket, there is no component.cfc in this directory
		fileWrite( variables.dir & "noCfc/Ticket.cfc", 'component { function make() { cfc = new component () { }; return cfc; } }' );
		fileWrite( variables.dir & "noCfc/Inline.cfc", 'component {
			function plain() { var x = new component { function hi() { return "inline"; } }; return x.hi(); }
			function withAttr() { var x = new component accessors=true { property name="p" default="attr"; }; return x.getP(); }
		}' );
	}

	function afterAll() {
		if ( directoryExists( variables.dir ) ) directoryDelete( variables.dir, true );
	}

	function run( testResults, testBox ) {
		describe( "LDEV-5533 NPE on invalid inline component syntax", function() {

			it( title="new component () { } (ticket syntax) fails with a regular error, not a NullPointerException", body=function( currentSpec ) {
				var err = "";
				try {
					new LDEV5533.noCfc.Ticket().make();
				}
				catch ( any e ) {
					err = e;
				}
				expect( isSimpleValue( err ) ).toBeFalse( "an error was expected" );
				expect( err.type ).notToBe( "java.lang.NullPointerException" );
				expect( err.message ).notToInclude( "getFactory()" );
			});

			it( title="new component () with a space is a regular new, same as new component()", body=function( currentSpec ) {
				var caller = new LDEV5533.withCfc.Caller();
				var withoutSpace = caller.withoutSpace();
				expect( withoutSpace ).toInclude( "component.cfc" );
				expect( caller.withSpace() ).toBe( withoutSpace );
			});

			it( title="inline components still work", body=function( currentSpec ) {
				var inline = new LDEV5533.noCfc.Inline();
				expect( inline.plain() ).toBe( "inline" );
				expect( inline.withAttr() ).toBe( "attr" );
			});

		});
	}
}

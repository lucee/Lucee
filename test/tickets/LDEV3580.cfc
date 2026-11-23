component extends="org.lucee.cfml.test.LuceeTestCase" labels="LDEV3580" {

	private function engine() {
		return createObject( "java", "lucee.runtime.script.CFMLScriptEngineFactory" ).init().getScriptEngine();
	}

	function run( testResults, testBox ) {
		describe( "LDEV-3580 JSR-223 include of a relative template", function() {

			it( title="engine found", body=function( currentSpec ) {
				expect( isNull( engine() ) ).toBeFalse();
			});

			it( title="include of a missing relative template gives a missing template error, not NPE", body=function( currentSpec ) {
				var msg = "";
				var type = "";
				var e = engine();
				try {
					e.eval( 'include "ldev3580-does-not-exist.cfm";' );
				}
				catch ( any ex ) {
					msg = ex.message;
					type = ex.type;
					systemOutput( "LDEV3580 type=[#type#] msg=[#msg#]", true );
					if ( !isNull( ex.cause ) ) systemOutput( "LDEV3580 cause=[#ex.cause.toString()#]", true );
				}
				expect( msg ).notToInclude( "NullPointerException" );
				expect( type ).notToInclude( "NullPointerException" );
				expect( msg ).toInclude( "ldev3580-does-not-exist.cfm" );
			});

			it( title="include of an existing relative template is resolved from the webroot", body=function( currentSpec ) {
				structDelete( server, "ldev3580" );
				var path = replace( mid( createURI( "LDEV3580/inc.cfm" ), 2 ), "\", "/", "all" );
				engine().eval( 'include "#path#";' );
				expect( server.ldev3580 ?: "" ).toBe( "included" );
				structDelete( server, "ldev3580" );
			});
		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrentTemplatePath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}
}

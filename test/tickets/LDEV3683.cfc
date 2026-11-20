component extends="org.lucee.cfml.test.LuceeTestCase" labels="static" {

	function run( testResults, testBox ) {
		describe( "LDEV-3683 static final functions", function() {

			it( title="public static final string function", body=function( currentSpec ) {
				expect( new LDEV3683.StaticFinal() ).toBeComponent();
				expect( LDEV3683.StaticFinal::g() ).toBe( "g" );
			});

			it( title="modifier order and no access modifier", body=function( currentSpec ) {
				expect( LDEV3683.StaticFinal::sum( 1, 2 ) ).toBe( 3 );
				expect( LDEV3683.StaticFinal::noAccess() ).toBe( "noAccess" );
				expect( LDEV3683.StaticFinal::finalFirst() ).toBe( "finalFirst" );
				expect( LDEV3683.StaticFinal::onlyStatic() ).toBe( "onlyStatic" );
				expect( new LDEV3683.StaticFinal().instanceFinal() ).toBe( "instanceFinal" );
			});

			it( title="metadata has static and final", body=function( currentSpec ) {
				var functions = getComponentMetadata( "LDEV3683.StaticFinal" ).functions;
				var found = {};
				loop array=functions item="local.f" {
					found[ f.name ] = f;
				}
				expect( found.g.static ).toBeTrue();
				expect( found.g.modifier ).toBe( "final" );
				expect( found.finalFirst.static ).toBeTrue();
				expect( found.finalFirst.modifier ).toBe( "final" );
				expect( found.onlyStatic.static ).toBeTrue();
				expect( found.instanceFinal.static ).toBeFalse();
				expect( found.instanceFinal.modifier ).toBe( "final" );
			});

			it( title="a sub component can override a static function that is not final", body=function( currentSpec ) {
				expect( LDEV3683.Child::onlyStatic() ).toBe( "child" );
				expect( LDEV3683.Child::g() ).toBe( "g" );
			});

			it( title="a static final function cannot be overridden", body=function( currentSpec ) {
				expect( function() {
					LDEV3683.ChildOverridesFinal::g();
				}).toThrow( regex="final" );
			});
		});
	}
}

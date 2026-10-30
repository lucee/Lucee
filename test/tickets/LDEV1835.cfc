component extends="org.lucee.cfml.test.LuceeTestCase" labels="interface" {

	function run( testResults, testBox ) {
		describe( title="LDEV-1835 default methods in interfaces", body=function() {

			it( title="component inherits the default function of the interface", body=function( currentSpec ) {
				var c = new LDEV1835.Inherit();
				expect( c.greet( "Susi" ) ).toBe( "Hello Susi" );
				expect( c.plain() ).toBe( "plain" );
				expect( c.required() ).toBe( "inherit" );
			});

			it( title="default function can call the abstract functions of the component", body=function( currentSpec ) {
				expect( new LDEV1835.Inherit().useRequired() ).toBe( "default uses inherit" );
				expect( new LDEV1835.Override().useRequired() ).toBe( "default uses override" );
			});

			it( title="component can override the default function", body=function( currentSpec ) {
				expect( new LDEV1835.Override().greet( "Susi" ) ).toBe( "Hi Susi" );
			});

			it( title="function of the super component wins over the default function", body=function( currentSpec ) {
				expect( new LDEV1835.SuperChild().greet( "Susi" ) ).toBe( "Super Susi" );
				// the default functions that are not defined by the super component are still inherited
				expect( new LDEV1835.SuperChild().plain() ).toBe( "plain" );
			});

			it( title="child of a component implementing the interface inherits and can override the default function", body=function( currentSpec ) {
				expect( new LDEV1835.InheritedChild().greet( "Susi" ) ).toBe( "Hello Susi" );
				expect( new LDEV1835.OverrideChild().greet( "Susi" ) ).toBe( "Child Susi" );
			});

			it( title="default function is available as member of the component", body=function( currentSpec ) {
				var c = new LDEV1835.Inherit();
				expect( structKeyExists( c, "greet" ) ).toBeTrue();
				expect( isCustomFunction( c.greet ) ).toBeTrue();
				var fn = c.greet;
				expect( fn( "Susi" ) ).toBe( "Hello Susi" );
			});

			it( title="abstract functions of the interface must still be implemented", body=function( currentSpec ) {
				expect( function() {
					new LDEV1835.MissingRequired();
				}).toThrow( type="application" );
			});

			it( title="interface extending an interface inherits the default functions", body=function( currentSpec ) {
				var c = new LDEV1835.ImplChild();
				expect( c.greet( "Susi" ) ).toBe( "Hello Susi" );
				expect( c.extra() ).toBe( "extra" );
			});

			it( title="abstract function of the extending interface must still be implemented", body=function( currentSpec ) {
				expect( function() {
					new LDEV1835.ImplChildMissingExtra();
				}).toThrow( type="application" );
			});

			it( title="interface extending an interface can override the default function", body=function( currentSpec ) {
				expect( new LDEV1835.ImplChildOverride().greet( "Susi" ) ).toBe( "Welcome Susi" );
			});

			it( title="interface extending an interface can make a default function abstract again", body=function( currentSpec ) {
				expect( function() {
					new LDEV1835.ImplChildAbstractMissing();
				}).toThrow( type="application" );
				expect( new LDEV1835.ImplChildAbstract().greet( "Susi" ) ).toBe( "Abstract Susi" );
			});

			it( title="an overriding function must still match the signature of the interface", body=function( currentSpec ) {
				expect( function() {
					new LDEV1835.ImplBadOverride();
				}).toThrow( type="application" );
			});

			it( title="2 interfaces with the same default function require the component to define the function", body=function( currentSpec ) {
				expect( function() {
					new LDEV1835.Ambiguous();
				}).toThrow( type="application" );
				expect( new LDEV1835.AmbiguousResolved().who() ).toBe( "resolved" );
			});

			it( title="default function without a body is rejected", body=function( currentSpec ) {
				expect( function() {
					new LDEV1835.ImplDefaultNoBody();
				}).toThrow();
			});

			it( title="metadata of the interface lists the default function", body=function( currentSpec ) {
				var md = getComponentMetadata( "LDEV1835.IDefault" );
				var names = [];
				loop array=md.functions item="local.f" {
					arrayAppend( names, f.name );
				}
				expect( names ).toInclude( "greet" );
				expect( names ).toInclude( "required" );
				// the component can still be inspected
				expect( getMetadata( new LDEV1835.Inherit() ) ).toBeStruct();
			});

			it( title="default function in a tag based interface", body=function( currentSpec ) {
				var c = new LDEV1835.ImplTag();
				expect( c.tagDefault( "Susi" ) ).toBe( "Tag Susi" );
				expect( c.required() ).toBe( "tag" );
			});

		});
	}

}

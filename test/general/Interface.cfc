component extends="org.lucee.cfml.test.LuceeTestCase" {

	// CFC interface contracts: implements + super dispatch, checkInterface enforcement,
	// metadata reporting on declaring vs inheriting CFCs, multiple-implements, and interface inheritance.

	function run( testResults, testBox ){

		describe( "CFC interface — implements + super dispatch", function(){

			it( "child overriding interface method calls super and returns combined result", function(){
				var c = new interface.IChild( tag="alpha" );
				expect( c.ifaceMethod() ).toBe( "ichild:alpha:ibase:alpha" );
			});

			it( "duplicate of interface-implementing child preserves super dispatch", function(){
				var c = new interface.IChild( tag="bravo" );
				var d = duplicate( c );
				expect( d.ifaceMethod() ).toBe( "ichild:bravo:ibase:bravo" );
				expect( d.tag ).toBe( c.tag );
			});

		});

		describe( "checkInterface — CFC declares implements but is missing required methods", function(){

			it( "new throws when a required interface method is not defined", function(){
				expect( function(){ new interface.IncompleteImplementer(); } ).toThrow();
			});
			it( "createObject parity — same rejection", function(){
				expect( function(){ createObject( "component", "interface.IncompleteImplementer" ); } ).toThrow();
			});
			it( "error message names the missing function", function(){
				try {
					new interface.IncompleteImplementer();
					fail( "expected throw" );
				} catch ( any e ) {
					expect( e.message ).toInclude( "ifaceMethod" );
				}
			});

		});

		describe( "getMetaData.implements is reported on the declaring CFC, not the inheriting dispatcher", function(){

			it( "declaring CFC (IBase) reports IFace in implements", function(){
				var meta = getMetaData( new interface.IBase() );
				expect( structKeyExists( meta.implements, "IFace" ) ).toBeTrue();
			});
			it( "inheriting CFC (IChild) reports an empty implements struct on itself", function(){
				var meta = getMetaData( new interface.IChild() );
				expect( structKeyExists( meta.implements, "IFace" ) ).toBeFalse();
			});
			it( "inheriting CFC reports IFace via the extends chain — extends.implements", function(){
				var meta = getMetaData( new interface.IChild() );
				expect( structKeyExists( meta.extends.implements, "IFace" ) ).toBeTrue();
			});

		});

		describe( "multiple interfaces — implements with a comma-separated list", function(){

			it( "CFC implementing two interfaces — both methods callable", function(){
				var m = new interface.MultiImplementer();
				expect( m.ifaceMethod() ).toBe( "ifaceMethod-result" );
				expect( m.anotherMethod() ).toBe( "anotherMethod-result" );
			});

			it( "isInstanceOf returns true for each implemented interface", function(){
				var m = new interface.MultiImplementer();
				expect( isInstanceOf( m, "IFace" ) ).toBeTrue();
				expect( isInstanceOf( m, "IAnotherFace" ) ).toBeTrue();
			});

			it( "getMetaData.implements lists both interfaces", function(){
				var meta = getMetaData( new interface.MultiImplementer() );
				expect( structKeyExists( meta.implements, "IFace" ) ).toBeTrue();
				expect( structKeyExists( meta.implements, "IAnotherFace" ) ).toBeTrue();
			});

		});

		describe( "interface inheritance — interface extends interface", function(){

			it( "CFC implementing only the derived interface satisfies both inherited and direct methods", function(){
				var d = new interface.DerivedImplementer();
				expect( d.ifaceMethod() ).toBe( "ifaceMethod-from-derived-impl" );
				expect( d.derivedMethod() ).toBe( "derivedMethod-result" );
			});

			it( "isInstanceOf returns true for the derived AND the base interface", function(){
				var d = new interface.DerivedImplementer();
				expect( isInstanceOf( d, "IDerivedFace" ) ).toBeTrue();
				expect( isInstanceOf( d, "IFace" ) ).toBeTrue();
			});

			it( "checkInterface walks the inheritance chain — missing IFace method on a derived implementer throws", function(){
				expect( function(){ new interface.IncompleteDerived(); } ).toThrow();
			});

			it( "missing-inherited error message names the missing IFace function", function(){
				try {
					new interface.IncompleteDerived();
					fail( "expected throw" );
				} catch ( any e ) {
					expect( e.message ).toInclude( "ifaceMethod" );
				}
			});

		});

	}
}

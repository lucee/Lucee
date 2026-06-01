component extends="org.lucee.cfml.test.LuceeTestCase" {

	// abstract / final / interface modifiers on CFCs — instantiation, extension, and metadata contracts.

	function run( testResults, testBox ){

		describe( "concrete components instantiate normally", function(){

			it( "Direct (implements Interface) — new succeeds", function(){
				expect( function(){ new abstract.Direct(); } ).notToThrow();
			});
			it( "Direct — createObject parity with new", function(){
				expect( function(){ createObject( "component", "abstract.Direct" ); } ).notToThrow();
			});
			it( "InDirect (extends Direct) — new succeeds", function(){
				expect( function(){ new abstract.InDirect(); } ).notToThrow();
			});
			it( "Final (extends abstract Abs, provides abstract method) — new succeeds", function(){
				expect( function(){ new abstract.Final(); } ).notToThrow();
			});

		});

		describe( "abstract component cannot be instantiated directly", function(){

			it( "new abstract.Abs() throws with the abstract-component error", function(){
				try {
					new abstract.Abs();
					fail( "expected throw" );
				} catch ( any e ) {
					expect( e.message ).toInclude( "you cannot instantiate an abstract component" );
				}
			});
			it( "createObject parity — same rejection", function(){
				expect( function(){ createObject( "component", "abstract.Abs" ); } ).toThrow();
			});

		});

		describe( "interface cannot be instantiated as a component", function(){

			it( "new abstract.Interface() throws with the interface-not-component error", function(){
				try {
					new abstract.Interface();
					fail( "expected throw" );
				} catch ( any e ) {
					expect( e.message ).toInclude( "the interface [" );
					expect( e.message ).toInclude( "] cannot be used as a component." );
				}
			});
			it( "createObject parity — same rejection", function(){
				expect( function(){ createObject( "component", "abstract.Interface" ); } ).toThrow();
			});

		});

		describe( "final modifier prevents extension", function(){

			it( "extending a final component throws", function(){
				try {
					new abstract.CannotExtendFinalComponent();
					fail( "expected throw" );
				} catch ( any e ) {
					expect( e.message ).toInclude( "you cannot extend a final component [" );
				}
			});
			it( "createObject parity — same rejection", function(){
				expect( function(){ createObject( "component", "abstract.CannotExtendFinalComponent" ); } ).toThrow();
			});

		});

		describe( "abstract function placement and implementation rules", function(){

			it( "abstract functions only allowed in abstract components", function(){
				try {
					new abstract.AbstractFunctionsOnlyInAbstractComponents();
					fail( "expected throw" );
				} catch ( any e ) {
					expect( e.message ).toInclude( "is not allowed within the no abstract component" );
				}
			});
			it( "concrete component must implement inherited abstract functions", function(){
				try {
					new abstract.AbstractFunction();
					fail( "expected throw" );
				} catch ( any e ) {
					expect( e.message ).toInclude( "does not implement the function" );
				}
			});

		});

		describe( "final method cannot be overridden", function(){

			it( "overriding a final method throws (variant 1)", function(){
				try {
					new abstract.OverwriteFinalMethod1();
					fail( "expected throw" );
				} catch ( any e ) {
					expect( e.message ).toInclude( "tries to override a final method" );
				}
			});
			it( "overriding a final method throws (variant 2)", function(){
				try {
					new abstract.OverwriteFinalMethod2();
					fail( "expected throw" );
				} catch ( any e ) {
					expect( e.message ).toInclude( "tries to override a final method" );
				}
			});

		});

		describe( "alternative-syntax final modifier", function(){

			it( "final alt-syntax 1 — succeeds when contract is met", function(){
				expect( function(){ new abstract.FinalAltSyntax1(); } ).notToThrow();
			});
			it( "final alt-syntax 2 — succeeds when contract is met", function(){
				expect( function(){ new abstract.FinalAltSyntax2(); } ).notToThrow();
			});
			it( "final alt-syntax 1 fail — throws on violation", function(){
				expect( function(){ new abstract.FinalAltSyntax1Fail(); } ).toThrow();
			});
			it( "final alt-syntax 2 fail — throws on violation", function(){
				expect( function(){ new abstract.FinalAltSyntax2Fail(); } ).toThrow();
			});

		});

		describe( "getMetaData reports abstract modifier on inherited base", function(){

			it( "Final's extends struct has abstract=true (Final extends abstract Abs)", function(){
				var meta = getMetaData( new abstract.Final() );
				expect( meta.extends.abstract ).toBeTrue();
			});
			it( "Direct (non-abstract base) — extends struct does NOT carry an abstract key", function(){
				var meta = getMetaData( new abstract.Direct() );
				expect( structKeyExists( meta.extends, "abstract" ) ).toBeFalse();
			});

		});

		describe( "isInstanceOf walks the abstract chain", function(){

			it( "Final is recognized as instance of Abs (bare-name match)", function(){
				expect( isInstanceOf( new abstract.Final(), "Abs" ) ).toBeTrue();
			});
			it( "Final is recognized as instance of full-path Abs", function(){
				// path varies by mapping (test71 via curl, test via mvn) — derive from metadata
				var fullAbsPath = getMetaData( new abstract.Final() ).extends.fullname;
				expect( isInstanceOf( new abstract.Final(), fullAbsPath ) ).toBeTrue();
			});
			it( "Direct is NOT an instance of Abs (Direct only implements Interface)", function(){
				expect( isInstanceOf( new abstract.Direct(), "Abs" ) ).toBeFalse();
			});

		});

	}

}

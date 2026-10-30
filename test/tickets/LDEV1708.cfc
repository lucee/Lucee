component extends="org.lucee.cfml.test.LuceeTestCase" labels="component,static" {

	function run( testResults, testBox ) {

		describe( "LDEV-1708: new self() creates the component the function was called on", function() {

			describe( "static functions", function() {

				it( "returns the base component when called on the base", function() {
					expect( getMetadata( LDEV1708.Base::create() ).name ).toInclude( "Base" );
				});

				it( "returns the subcomponent when called on the subcomponent", function() {
					var cfc = LDEV1708.Sub::create();
					expect( isInstanceOf( cfc, "LDEV1708.Sub" ) ).toBeTrue();
					expect( getMetadata( cfc ).name ).toInclude( "Sub" );
				});

				it( "follows the called component over several levels of inheritance", function() {
					expect( getMetadata( LDEV1708.SubSub::create() ).name ).toInclude( "SubSub" );
				});

				it( "passes named arguments to the constructor", function() {
					var cfc = LDEV1708.Sub::createNamed( 5 );
					expect( getMetadata( cfc ).name ).toInclude( "Sub" );
					expect( cfc.getA() ).toBe( 5 );
				});

				it( "passes positional arguments to the constructor", function() {
					var cfc = LDEV1708.Sub::createPositional( 6 );
					expect( getMetadata( cfc ).name ).toInclude( "Sub" );
					expect( cfc.getA() ).toBe( 6 );
				});

				it( "uses the default constructor argument when there are none", function() {
					expect( LDEV1708.Sub::create().getA() ).toBe( 0 );
				});

				it( "throws for an abstract called component and works for a concrete one", function() {
					expect( function() {
						LDEV1708.AbstractBase::create();
					}).toThrow( regex="abstract" );
					expect( getMetadata( LDEV1708.Concrete::create() ).name ).toInclude( "Concrete" );
				});

			});

			describe( "instance functions", function() {

				it( "returns the base component for a base instance", function() {
					expect( getMetadata( new LDEV1708.Base().make() ).name ).toInclude( "Base" );
				});

				it( "returns the subcomponent for a subcomponent instance", function() {
					var cfc = new LDEV1708.Sub().make();
					expect( isInstanceOf( cfc, "LDEV1708.Sub" ) ).toBeTrue();
					expect( getMetadata( cfc ).name ).toInclude( "Sub" );
				});

				it( "returns the subcomponent for a call on an instance of a deeper subcomponent", function() {
					expect( getMetadata( new LDEV1708.SubSub().make() ).name ).toInclude( "SubSub" );
				});

				it( "passes arguments to the constructor", function() {
					var cfc = new LDEV1708.Sub().makeNamed( 3 );
					expect( getMetadata( cfc ).name ).toInclude( "Sub" );
					expect( cfc.getA() ).toBe( 3 );
				});

				it( "keeps the called component when going through super", function() {
					expect( getMetadata( new LDEV1708.Sub().makeViaSuper() ).name ).toInclude( "Sub" );
				});

				it( "works inside a closure", function() {
					expect( getMetadata( new LDEV1708.Sub().makeViaClosure() ).name ).toInclude( "Sub" );
				});

				it( "works inside evaluate()", function() {
					expect( getMetadata( new LDEV1708.Sub().makeViaEvaluate() ).name ).toInclude( "Sub" );
					expect( new LDEV1708.Sub().makeViaEvaluateArgs().getA() ).toBe( 7 );
				});

				it( "is case insensitive", function() {
					expect( getMetadata( new LDEV1708.Sub().makeUpperCase() ).name ).toInclude( "Sub" );
				});

			});

			describe( "outside of a component", function() {

				it( "throws a clear error", function() {
					var template = createURI( "LDEV1708/outside.cfm" );
					expect( function() {
						_internalRequest(
							template = template,
							throwonerror = true
						);
					}).toThrow( regex="can only be used inside a component" );
				});

			});

		});

		describe( "LDEV-1708: self is only a keyword directly after new", function() {

			it( "still allows self as a static function name", function() {
				expect( getMetadata( LDEV1708.SelfNames::self() ).name ).toInclude( "Other" );
			});

			it( "still allows self as a variable and an argument name", function() {
				var cfc = new LDEV1708.SelfNames();
				expect( getMetadata( cfc.viaVar() ).name ).toInclude( "SelfNames" );
				expect( cfc.viaStruct() ).toBe( 1 );
				expect( cfc.viaClosure() ).toBe( 42 );
			});

			it( "still allows a local function called self", function() {
				expect( getMetadata( new LDEV1708.SelfNames().viaLocalFunction() ).name ).toInclude( "Other" );
			});

			it( "new self() ignores a component called self in the same directory", function() {
				expect( getMetadata( new LDEV1708.Sub().make() ).name ).toInclude( "Sub" );
				expect( getMetadata( new LDEV1708.Sub().make() ).name ).notToInclude( "self" );
			});

			it( "a quoted name is not the keyword and still finds the component called self", function() {
				expect( listLast( getMetadata( new LDEV1708.Sub().makeQuoted() ).name, "." ) ).toBe( "self" );
			});

			it( "a component called self is still reachable with its full path", function() {
				expect( listLast( getMetadata( new LDEV1708.self() ).name, "." ) ).toBe( "self" );
			});

		});

	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & calledName;
	}

}

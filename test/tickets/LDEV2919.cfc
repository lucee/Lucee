component extends="org.lucee.cfml.test.LuceeTestCase" labels="localmode" {

	function beforeAll() {
		variables.uri = createURI( "LDEV2919" );
	}

	private string function run_( required string app, required string cfc, string fn = "probe" ) {
		return _InternalRequest( template: "#variables.uri#/app-#arguments.app#/run.cfm", url: { cfc: arguments.cfc, fn: arguments.fn } ).fileContent.trim();
	}

	function run( testResults, testBox ) {

		var CLASSIC = "variables=true;local=false";
		var MODERN = "variables=false;local=true";

		describe( "LDEV-2919 localmode per component", function() {

			describe( "no localmode on the component, the application setting is used", function() {

				it( "application classic", function() {
					expect( run_( "classic", "None" ) ).toBe( CLASSIC );
				});

				it( "application modern", function() {
					expect( run_( "modern", "None" ) ).toBe( MODERN );
				});

			});

			describe( "script component", function() {

				it( "localmode=modern beats this.localmode=classic", function() {
					expect( run_( "classic", "Modern" ) ).toBe( MODERN );
				});

				it( "localmode=classic beats this.localmode=modern", function() {
					expect( run_( "modern", "Classic" ) ).toBe( CLASSIC );
				});

				it( "localmode=modern with this.localmode=modern", function() {
					expect( run_( "modern", "Modern" ) ).toBe( MODERN );
				});

				it( "localmode=classic with this.localmode=classic", function() {
					expect( run_( "classic", "Classic" ) ).toBe( CLASSIC );
				});

				it( "localmode=true is the same as modern", function() {
					expect( run_( "classic", "TrueMode" ) ).toBe( MODERN );
				});

				it( "localmode=false is the same as classic", function() {
					expect( run_( "modern", "FalseMode" ) ).toBe( CLASSIC );
				});

				it( "works together with other component attributes", function() {
					expect( run_( "classic", "Accessors" ) ).toBe( MODERN );
				});

			});

			describe( "tag component", function() {

				it( "cfcomponent localmode=modern beats this.localmode=classic", function() {
					expect( run_( "classic", "TagModern" ) ).toBe( MODERN );
				});

				it( "cfcomponent localmode=classic beats this.localmode=modern", function() {
					expect( run_( "modern", "TagClassic" ) ).toBe( CLASSIC );
				});

			});

			describe( "the localmode of the function has precedence", function() {

				it( "script: modern component, function classic", function() {
					expect( run_( "classic", "ModernWithClassicFunction", "probeClassic" ) ).toBe( CLASSIC );
					expect( run_( "modern", "ModernWithClassicFunction", "probeClassic" ) ).toBe( CLASSIC );
				});

				it( "script: modern component, function without localmode", function() {
					expect( run_( "classic", "ModernWithClassicFunction", "probeDefault" ) ).toBe( MODERN );
				});

				it( "script: classic component, function modern", function() {
					expect( run_( "modern", "ClassicWithModernFunction", "probeModern" ) ).toBe( MODERN );
					expect( run_( "classic", "ClassicWithModernFunction", "probeModern" ) ).toBe( MODERN );
				});

				it( "script: classic component, function without localmode", function() {
					expect( run_( "modern", "ClassicWithModernFunction", "probeDefault" ) ).toBe( CLASSIC );
				});

				it( "tag: modern component, cffunction classic", function() {
					expect( run_( "classic", "TagModernWithClassicFunction" ) ).toBe( CLASSIC );
					expect( run_( "modern", "TagModernWithClassicFunction" ) ).toBe( CLASSIC );
				});

			});

			describe( "closures", function() {

				it( "a closure in a component with localmode=modern uses the component setting", function() {
					expect( run_( "classic", "ModernClosure", "closureProbe" ) ).toBe( MODERN );
				});

			});

			describe( "inheritance: the setting belongs to the component file that declares it", function() {

				it( "functions inherited from a modern parent run modern", function() {
					expect( run_( "classic", "ChildOfModern", "probe" ) ).toBe( MODERN );
				});

				it( "functions of the child without localmode do not inherit the setting of the parent, the application setting is used", function() {
					expect( run_( "classic", "ChildOfModern", "childProbe" ) ).toBe( CLASSIC );
					expect( run_( "modern", "ChildOfModern", "childProbe" ) ).toBe( MODERN );
				});

				it( "a modern child does not change the functions of the parent", function() {
					expect( run_( "classic", "ModernChildOfNone", "childProbe" ) ).toBe( MODERN );
					expect( run_( "classic", "ModernChildOfNone", "probe" ) ).toBe( CLASSIC );
				});

			});

			describe( "invalid values", function() {

				it( "a value that is not valid throws a compile error naming the attribute", function() {
					var res = run_( "classic", "Invalid" );
					expect( res ).toInclude( "error=" );
					expect( res ).toInclude( "localMode" );
					expect( res ).toInclude( "nonsense" );
				});

			});

		});
	}

	private string function createURI( string calledName ) {
		var baseURI = "/test/#listLast( getDirectoryFromPath( getCurrenttemplatepath() ), "\/" )#/";
		return baseURI & "" & calledName;
	}

}

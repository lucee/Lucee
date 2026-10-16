component extends="org.lucee.cfml.test.LuceeTestCase" {

	// Non-declarative content in CFC files: pseudo-constructor body runs per new/createObject;
	// pre-declaration and post-declaration code (outside the component block) is silently discarded.

	variables.appKeys = [
		"looseBetweenCounter",
		"looseBeforeCounter",
		"looseAfterCounter",
		"looseBeforeUuid",
		"looseAfterUuid"
	];

	function beforeAll(){
		for ( var k in variables.appKeys ) structDelete( application, k );
	}

	function afterAll(){
		for ( var k in variables.appKeys ) structDelete( application, k );
	}

	function run( testResults, testBox ){

		describe( "pseudo-constructor body — pinned True-bucket contract", function(){
			it( "script body runs on every new — counter advances by 1", function(){
				var a = new loose.BetweenFunctions();
				var c1 = a.getBetweenCounter();
				var b = new loose.BetweenFunctions();
				expect( b.getBetweenCounter() ).toBe( c1 + 1 );
			});
			it( "script body produces fresh expression values per new", function(){
				var a = new loose.BetweenFunctions();
				var b = new loose.BetweenFunctions();
				expect( a.getBetweenUuid() ).notToBe( b.getBetweenUuid() );
			});
			it( "tag body runs on every new — counter advances by 1", function(){
				var a = new loose.TagBetweenFunctions();
				var c1 = a.getBetweenCounter();
				var b = new loose.TagBetweenFunctions();
				expect( b.getBetweenCounter() ).toBe( c1 + 1 );
			});
			it( "tag body produces fresh expression values per new", function(){
				var a = new loose.TagBetweenFunctions();
				var b = new loose.TagBetweenFunctions();
				expect( a.getBetweenUuid() ).notToBe( b.getBetweenUuid() );
			});
		});

		describe( "pre/post-declaration code — does NOT execute on instantiation", function(){
			it( "script pre-declaration counter stays unset after new", function(){
				new loose.BeforeDeclaration();
				expect( structKeyExists( application, "looseBeforeCounter" ) ).toBeFalse();
			});
			it( "script post-declaration counter stays unset after new", function(){
				new loose.AfterDeclaration();
				expect( structKeyExists( application, "looseAfterCounter" ) ).toBeFalse();
			});
			it( "tag pre-declaration counter stays unset after new", function(){
				new loose.TagBeforeDeclaration();
				expect( structKeyExists( application, "looseBeforeCounter" ) ).toBeFalse();
			});
			it( "tag post-declaration counter stays unset after new", function(){
				new loose.TagAfterDeclaration();
				expect( structKeyExists( application, "looseAfterCounter" ) ).toBeFalse();
			});
		});

		describe( "instantiation path parity — createObject vs new", function(){
			it( "createObject body code runs — same as new", function(){
				var a = createObject( "component", "loose.BetweenFunctions" );
				var c1 = a.getBetweenCounter();
				var b = createObject( "component", "loose.BetweenFunctions" );
				expect( b.getBetweenCounter() ).toBe( c1 + 1 );
			});
			it( "createObject pre-decl stays unset — same as new", function(){
				createObject( "component", "loose.BeforeDeclaration" );
				expect( structKeyExists( application, "looseBeforeCounter" ) ).toBeFalse();
			});
		});

		describe( "scope leak — pre/post-decl writes are compile-time dead", function(){
			it( "VariablesProbe component sees no pre-decl variable internally", function(){
				var cfc = new loose.VariablesProbe();
				expect( cfc.getSelfPre() ).toBe( "missing-in-component" );
			});
		});

		describe( "pseudo-constructor body throws propagate through new and createObject", function(){

			it( "cfthrow in body halts new with the same exception type and message", function(){
				try {
					new loose.ThrowsInBody();
					fail( "expected throw" );
				} catch ( LooseTest e ) {
					expect( e.message ).toBe( "body-threw-on-purpose" );
				}
			});

			it( "cfthrow in body halts createObject — same exception type", function(){
				expect( function(){ createObject( "component", "loose.ThrowsInBody" ); } ).toThrow( type="LooseTest" );
			});

		});

	}

}

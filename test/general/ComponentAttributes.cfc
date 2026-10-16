component extends="org.lucee.cfml.test.LuceeTestCase" {

	// Compile-time-baked component-level attributes: hint, output, accessors, persistent.
	// Non-literal hints are surprising — Lucee replaces them with a "[runtime expression]" placeholder
	// rather than evaluating the expression at metadata time.

	function run( testResults, testBox ){

		describe( "component-level hint attribute", function(){

			it( "literal hint is preserved verbatim in metadata", function(){
				var meta = getMetaData( new componentAttributes.HintLiteral() );
				expect( meta.hint ).toBe( "literal-hint" );
			});

			it( "expression-form hint is replaced with [runtime expression] placeholder, NOT evaluated", function(){
				var meta = getMetaData( new componentAttributes.HintExpression() );
				expect( meta.hint ).toBe( "[runtime expression]" );
			});

			it( "concatenated hint (literal + expression) is replaced with the placeholder — literal portion discarded", function(){
				var meta = getMetaData( new componentAttributes.HintConcatenated() );
				expect( meta.hint ).toBe( "[runtime expression]" );
			});

			it( "component with no hint attribute — hint key absent from metadata", function(){
				var meta = getMetaData( new componentAttributes.NoHint() );
				expect( structKeyExists( meta, "hint" ) ).toBeFalse();
			});

			it( "hint is class-level — siblings see the same value", function(){
				var a = new componentAttributes.HintLiteral();
				var b = new componentAttributes.HintLiteral();
				expect( getMetaData( a ).hint ).toBe( getMetaData( b ).hint );
			});

		});

		describe( "component-level output attribute — gates body writeOutput", function(){

			it( "output=true — body writeOutput is captured to the caller's output buffer", function(){
				savecontent variable="local.captured" {
					new componentAttributes.OutputTrue();
				}
				expect( local.captured ).toInclude( "hello-from-output-true" );
			});

			it( "output=false — body writeOutput is silenced", function(){
				savecontent variable="local.captured" {
					new componentAttributes.OutputFalse();
				}
				expect( local.captured ).notToInclude( "hello-from-output-false" );
			});

			it( "no output attribute — default is output enabled (body writeOutput is captured)", function(){
				savecontent variable="local.captured" {
					new componentAttributes.OutputAbsent();
				}
				expect( local.captured ).toInclude( "hello-from-output-absent" );
			});

			it( "no output attribute — metadata does NOT carry the output key (default is implicit)", function(){
				var meta = getMetaData( new componentAttributes.OutputAbsent() );
				expect( structKeyExists( meta, "output" ) ).toBeFalse();
			});

			it( "getMetaData reports output=true on the component", function(){
				var meta = getMetaData( new componentAttributes.OutputTrue() );
				expect( meta.output ).toBeTrue();
			});

			it( "getMetaData reports output=false on the component", function(){
				var meta = getMetaData( new componentAttributes.OutputFalse() );
				expect( meta.output ).toBeFalse();
			});

		});

		describe( "component-level accessors attribute — gates auto getter/setter generation", function(){

			it( "accessors=true — getX/setX are auto-generated for each property", function(){
				var cfc = new componentAttributes.AccessorsTrue();
				expect( cfc.getTitle() ).toBe( "initial-title" );
				cfc.setTitle( "mutated" );
				expect( cfc.getTitle() ).toBe( "mutated" );
			});

			it( "accessors=false — getX/setX are NOT generated", function(){
				var cfc = new componentAttributes.AccessorsFalse();
				expect( structKeyExists( cfc, "getTitle" ) ).toBeFalse();
				expect( structKeyExists( cfc, "setTitle" ) ).toBeFalse();
			});

			it( "no accessors attribute — default is no auto-accessors (matches accessors=false)", function(){
				var cfc = new componentAttributes.AccessorsAbsent();
				expect( structKeyExists( cfc, "getTitle" ) ).toBeFalse();
			});

			it( "getMetaData reports accessors=true", function(){
				var meta = getMetaData( new componentAttributes.AccessorsTrue() );
				expect( meta.accessors ).toBeTrue();
			});

			it( "getMetaData reports accessors=false", function(){
				var meta = getMetaData( new componentAttributes.AccessorsFalse() );
				expect( meta.accessors ).toBeFalse();
			});

			it( "no accessors attribute — getMetaData reports accessors=false (the default)", function(){
				var meta = getMetaData( new componentAttributes.AccessorsAbsent() );
				expect( meta.accessors ).toBeFalse();
			});

		});

		describe( "component-level persistent attribute", function(){

			it( "persistent=true — accessors are auto-generated even without explicit accessors=true", function(){
				var cfc = new componentAttributes.PersistentTrue();
				expect( cfc.getTitle() ).toBe( "initial-title" );
				cfc.setTitle( "mutated" );
				expect( cfc.getTitle() ).toBe( "mutated" );
			});

			it( "getMetaData reports persistent=true", function(){
				var meta = getMetaData( new componentAttributes.PersistentTrue() );
				expect( meta.persistent ).toBeTrue();
			});

			// surprising contract: persistent=true generates accessors behaviourally but the metadata
			// still reports accessors=false (it reflects the literal attribute, not the implied state).
			it( "metadata reports accessors=false even though persistent=true generates accessors", function(){
				var meta = getMetaData( new componentAttributes.PersistentTrue() );
				expect( meta.accessors ).toBeFalse();
			});

			it( "non-persistent CFC reports persistent=false", function(){
				var meta = getMetaData( new componentAttributes.AccessorsTrue() );
				expect( meta.persistent ).toBeFalse();
			});

		});

	}

}

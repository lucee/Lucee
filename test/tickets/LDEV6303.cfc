component extends="org.lucee.cfml.test.LuceeTestCase" labels="orm" {

	function beforeAll() {
		variables.uri = getDirectoryFromPath( contractPath( getCurrentTemplatePath() ) ) & "LDEV6303/orm";
	}

	private function noOrm() {
		// works under mvn (server.getTestService) and http (graceful fallback)
		try {
			return ( structCount( server.getTestService( "orm" ) ) eq 0 );
		} catch ( any e ) {
			return false;
		}
	}

	function run( testResults, testBox ){
		describe( "LDEV-6303: cfproperty default expression-form metadata contract", function(){

			describe( "metadata.default surface for expression-form defaults", function(){

				it( "exposes a default key for non-foldable expression defaults", function(){
					var inst = new LDEV6303.ExpressionDefaultsCfc();
					var meta = findProperty( getMetadata( inst ).properties, "nowDef" );
					expect( structKeyExists( meta, "default" ) ).toBeTrue(
						"metadata.default key should exist for expression-form properties"
					);
				});

				it( "metadata.default for literal-form is a simple value", function(){
					var inst = new LDEV6303.ExpressionDefaultsCfc();
					var meta = findProperty( getMetadata( inst ).properties, "literalDef" );
					expect( isSimpleValue( meta.default ) ).toBeTrue();
					expect( meta.default ).toBe( "hello" );
				});

				it( "metadata.default for expression-form is not a simple value (programmatic discriminability)", function(){
					var inst = new LDEV6303.ExpressionDefaultsCfc();
					var meta = findProperty( getMetadata( inst ).properties, "nowDef" );
					expect( isSimpleValue( meta.default ) ).toBeFalse(
						"metadata.default for now() should be programmatically distinguishable from a literal-form default"
					);
				});

				it( "metadata.default for expression-form is an object (isObject=true)", function(){
					var inst = new LDEV6303.ExpressionDefaultsCfc();
					var nowMeta     = findProperty( getMetadata( inst ).properties, "nowDef" );
					var literalMeta = findProperty( getMetadata( inst ).properties, "literalDef" );
					expect( isObject( nowMeta.default ) ).toBeTrue(
						"expression-form metadata.default should be an object wrapper"
					);
					expect( isObject( literalMeta.default ) ).toBeFalse(
						"literal-form metadata.default should not be an object — it's a simple string"
					);
				});

				it( "metadata.default for expression-form stringifies to source CFML", function(){
					var inst = new LDEV6303.ExpressionDefaultsCfc();
					var meta = findProperty( getMetadata( inst ).properties, "nowDef" );
					var asString = "" & meta.default;
					expect( asString ).toInclude( "now" );
				});

				it( "metadata.default for #now()# is not a date (catches 7.0 frozen-evaluated-value)", function(){
					// 7.0 stored the evaluated DateTimeImpl in _default; metadata.default was
					// therefore a Date and isDate() returned true. After fix: metadata.default
					// is the source CFML string, not a Date.
					var inst = new LDEV6303.ExpressionDefaultsCfc();
					var meta = findProperty( getMetadata( inst ).properties, "nowDef" );
					expect( isDate( meta.default ) ).toBeFalse(
						"metadata.default for #now()# must not be a Date — that means an evaluated value was stored class-level"
					);
				});

			});

			describe( "cross-instance isolation under mutation", function(){

				it( "instance2 variables scope is independent of instance1 mutations", function(){
					var instance1 = new LDEV6303.ExpressionDefaultsCfc();
					var instance2 = new LDEV6303.ExpressionDefaultsCfc();

					var s1 = instance1.getNowStructDef();
					s1.n++;
					s1.n++;
					s1.n++;

					expect( instance1.getNowStructDef().n ).toBe( 4 );
					expect( instance2.getNowStructDef().n ).toBe( 1 );
				});

				it( "duplicate() variables scope is independent of source mutations", function(){
					var src = new LDEV6303.ExpressionDefaultsCfc();
					var dup = duplicate( src );

					var ss = src.getNowStructDef();
					ss.n++;
					ss.n++;
					ss.n++;

					expect( src.getNowStructDef().n ).toBe( 4 );
					expect( dup.getNowStructDef().n ).toBe( 1 );
				});

				it( "mutating instance1's struct does not propagate to instance2's metadata.default", function(){
					var instance1 = new LDEV6303.ExpressionDefaultsCfc();
					var instance2 = new LDEV6303.ExpressionDefaultsCfc();

					var s1 = instance1.getNowStructDef();
					s1.n++;
					s1.n++;
					s1.n++;

					var meta2 = findProperty( getMetadata( instance2 ).properties, "nowStructDef" );
					expect( structKeyExists( meta2, "default" ) ).toBeTrue();

					// 7.0 latent bug: metadata.default was a shared mutable struct, n became 4 here.
					// After fix: metadata.default is an immutable wrapper, no mutation surface.
					if ( isStruct( meta2.default ) ) {
						expect( meta2.default.n ).toBe( 1,
							"metadata.default for instance2 must not see instance1's variables-scope mutations"
						);
					}
				});

				it( "first-instance mutation does not propagate to second-instance metadata (catches 7.0 shared _default leak)", function(){
					// LeakProbeCfc must NOT be instantiated outside this test — otherwise
					// the first-instance-aliased-with-_default behaviour has already
					// happened on a different instance, and this test silently passes.
					var instance1 = new LDEV6303.LeakProbeCfc();
					var bag1 = instance1.getBag();
					bag1.n++;
					bag1.n++;
					bag1.n++;

					var instance2 = new LDEV6303.LeakProbeCfc();
					var meta2 = findProperty( getMetadata( instance2 ).properties, "bag" );
					expect( structKeyExists( meta2, "default" ) ).toBeTrue();

					// On 7.0 with the leak: instance1.variables.bag === _default,
					// mutating bag.n leaks into _default which is read by metadata for
					// any other instance. instance2's metadata.bag.default.n would be 4.
					// After fix: metadata.default is an immutable wrapper, no leak path.
					// Serialize defensively — meta2.default may be a String wrapper or a struct.
					var defaultStr = isSimpleValue( meta2.default ) ? "" & meta2.default : serializeJson( meta2.default );
					expect( defaultStr ).notToInclude( """n"":4",
						"metadata.default for instance2 must not carry instance1's mutated counter"
					);

					// instance2's variables scope must also be unaffected
					expect( instance2.getBag().n ).toBe( 1 );
				});

				it( "metadata.default for nowDef is stable across instances and reads", function(){
					// 7.0 latent bug: metadata.default was frozen at first-eval time of any instance,
					// returning the same value across all instances of the class — even though each
					// instance's own variables-scope nowDef is correctly fresh per construction.
					// After fix: metadata.default is the source CFML, identical and stable across reads.
					var instance1 = new LDEV6303.ExpressionDefaultsCfc();
					sleep( 1100 );
					var instance2 = new LDEV6303.ExpressionDefaultsCfc();

					var meta1 = findProperty( getMetadata( instance1 ).properties, "nowDef" );
					var meta2 = findProperty( getMetadata( instance2 ).properties, "nowDef" );

					// instance variables-scope values must differ (per-instance freshness preserved)
					expect( dateCompare( instance2.getNowDef(), instance1.getNowDef(), "s" ) ).toBeGT( 0,
						"per-instance nowDef must be fresh per construction"
					);

					// metadata.default values must stringify equally — stable contract,
					// source string, not a frozen evaluated value that differs between instances
					expect( "" & meta1.default ).toBe( "" & meta2.default );
				});

			});

			describe( "ORM null-substitution end-to-end (LDEV-4121 contract)", function(){

			it( "expression-form default is reachable via variables scope, the canonical source for ORM null-substitution", function(){
				// Construction-time evaluated value in variables scope is the authoritative
				// answer for ORM null-substitution. This is what direction-3 in the Hibernate
				// extension's CFCSetter.set reads; this test confirms scope holds the right
				// value for that read to find.
				var inst = new LDEV6303.ExpressionDefaultsCfc();
				expect( inst.getExpressionDef() ).toBe( "xxxxx" );
				expect( isStruct( inst.getNowStructDef() ) ).toBeTrue();
				expect( inst.getNowStructDef().n ).toBe( 1 );
			});

			it( "construction-time evaluation captures the evaluated value, not the source string", function(){
				// Distinguishes "the wrapper landed in variables scope" (wrong) from
				// "the evaluated expression result landed in variables scope" (correct).
				// If the bytecode mistakenly stamps the wrapper into variables scope
				// instead of the evaluated value, this test catches it.
				var inst = new LDEV6303.ExpressionDefaultsCfc();
				expect( isSimpleValue( inst.getNowStructDef() ) ).toBeFalse(
					"per-instance variables scope must hold the evaluated struct, not the source-string wrapper"
				);
				expect( isDate( inst.getNowDef() ) ).toBeTrue(
					"per-instance variables scope for #now()# must hold the evaluated DateTime"
				);
			});

			it( title="entityLoad with NULL DB column substitutes the literal-form default", skip=noOrm(), body=function(){
				var result = _InternalRequest( template: "#variables.uri#/test.cfm", forms: { scene: "literal" } );
				expect( result.filecontent.trim() ).toBe( "literal-default" );
			});

			it( title="entityLoad with NULL DB column substitutes the expression-form default (LDEV-4121b)", skip=noOrm(), body=function(){
				var result = _InternalRequest( template: "#variables.uri#/test.cfm", forms: { scene: "expression" } );
				expect( result.filecontent.trim() ).toBe( "xxxxx" );
			});

		});

		describe( "per-instance variables scope is correctly seeded at construction", function(){

				it( "expression-form default is evaluated and seeded into variables scope", function(){
					var inst = new LDEV6303.ExpressionDefaultsCfc();
					expect( inst.getExpressionDef() ).toBe( "xxxxx" );
				});

				it( "non-deterministic expression evaluates fresh per construction", function(){
					var t1 = new LDEV6303.ExpressionDefaultsCfc().getNowDef();
					sleep( 1100 );
					var t2 = new LDEV6303.ExpressionDefaultsCfc().getNowDef();
					expect( dateCompare( t2, t1, "s" ) ).toBeGT( 0 );
				});

			});

			describe( "source-slice precision for expression-form defaults — locked invariants", function(){

				it( "function-call source captured verbatim (start at function name)", function(){
					var inst = new LDEV6303.NullProbeCfc();
					var meta = getMetadata( inst );
					var prop = "";
					for ( var p in meta.properties ) {
						if ( p.name == "funcCallDef" ) { prop = p; break; }
					}
					expect( "" & prop.default ).toBe( "repeatString( 'x', 5 )" );
				});

				it( "empty struct literal source includes both braces", function(){
					var inst = new LDEV6303.NullProbeCfc();
					var meta = getMetadata( inst );
					var prop = "";
					for ( var p in meta.properties ) {
						if ( p.name == "emptyStructDef" ) { prop = p; break; }
					}
					expect( "" & prop.default ).toBe( "{}" );
				});

				it( "empty array literal source includes both brackets", function(){
					var inst = new LDEV6303.NullProbeCfc();
					var meta = getMetadata( inst );
					var prop = "";
					for ( var p in meta.properties ) {
						if ( p.name == "emptyArrayDef" ) { prop = p; break; }
					}
					expect( "" & prop.default ).toBe( "[]" );
				});

				it( "populated struct literal source includes opening brace", function(){
					var inst = new LDEV6303.NullProbeCfc();
					var meta = getMetadata( inst );
					var prop = "";
					for ( var p in meta.properties ) {
						if ( p.name == "twoKeyStruct" ) { prop = p; break; }
					}
					expect( "" & prop.default ).toBe( "{a:1,b:2}" );
				});

				it( "populated array literal source includes opening bracket", function(){
					var inst = new LDEV6303.NullProbeCfc();
					var meta = getMetadata( inst );
					var prop = "";
					for ( var p in meta.properties ) {
						if ( p.name == "threeItemArray" ) { prop = p; break; }
					}
					expect( "" & prop.default ).toBe( "[""x"",""y"",""z""]" );
				});

			});

			describe( "null-edge probe — observation only, surfaces actual current behavior to test output", function(){

				it( "probes null-edge cases for the null-evaluating-expression vs no-default vs empty-string distinction", function(){
					var inst = new LDEV6303.NullProbeCfc();
					var props = getMetadata( inst ).properties;

					systemOutput( chr(10) & "===== LDEV-6303 null-edge probe =====" & chr(10), true );
					for ( var p in props ) {
						var sub = inst._probe( p.name );
						var line = "  [" & p.name & "]"
							& " metaHasDefault=" & structKeyExists( p, "default" )
							& " metaIsSimple=" & ( structKeyExists( p, "default" ) ? ( isNull( p.default ) ? "<null>" : isSimpleValue( p.default ) ) : "<absent>" )
							& " metaIsObject=" & ( structKeyExists( p, "default" ) ? ( isNull( p.default ) ? "<null>" : isObject( p.default ) ) : "<absent>" )
							& " metaDefault=" & ( structKeyExists( p, "default" ) ? ( isNull( p.default ) ? "<null>" : "[" & ( isSimpleValue( p.default ) ? p.default : serializeJson( p.default ) ) & "]" ) : "<absent>" )
							& " varsHasKey=" & sub.varsHasKey
							& " varsValue=" & sub.varsValue;
						systemOutput( line, true );
					}
					systemOutput( "===== probe end =====" & chr(10), true );

					// Trivial assertion to keep the spec green; the probe is observation-only.
					expect( arrayLen( props ) ).toBeGTE( 5 );
				});

			});

			describe( "inherited expression-form defaults — child must see evaluated values, not wrappers", function(){

				it( "child instance scope holds the evaluated value for inherited deterministic expression default", function(){
					var inst = new LDEV6303.ChildExprDefCfc();
					expect( inst.getParentExprDef() ).toBe( "ppppp",
						"inherited expression-form default must be evaluated, not the wrapper source string"
					);
				});

				it( "child instance scope holds the evaluated value for inherited literal default", function(){
					var inst = new LDEV6303.ChildExprDefCfc();
					expect( inst.getParentLiteralDef() ).toBe( "parent-literal" );
				});

				it( "child instance scope holds an evaluated DateTime for inherited #now()# default", function(){
					var inst = new LDEV6303.ChildExprDefCfc();
					expect( isDate( inst.getParentNowDef() ) ).toBeTrue(
						"inherited #now()# default must be evaluated to a DateTime, not stay as wrapper"
					);
				});

				it( "child's own expression-form default still works alongside inherited ones", function(){
					var inst = new LDEV6303.ChildExprDefCfc();
					expect( inst.getChildExprDef() ).toBe( "ccc" );
				});

				it( "metadata.default for inherited expression-form is preserved on meta.extends.properties", function(){
					// Inherited properties live under meta.extends.properties, not meta.properties —
					// standard CFML inheritance metadata shape. The wrapper's source CFML must be
					// preserved through inheritance.
					var inst = new LDEV6303.ChildExprDefCfc();
					var meta = getMetadata( inst );

					expect( structKeyExists( meta, "extends" ) ).toBeTrue();
					expect( structKeyExists( meta.extends, "properties" ) ).toBeTrue();

					var inherited = "";
					for ( var p in meta.extends.properties ) {
						if ( p.name == "parentExprDef" ) { inherited = p; break; }
					}
					expect( isStruct( inherited ) ).toBeTrue(
						"parentExprDef must be in meta.extends.properties for the child instance"
					);
					expect( structKeyExists( inherited, "default" ) ).toBeTrue(
						"metadata.default must be preserved on the inherited expression-form property"
					);
					expect( "" & inherited.default ).toInclude( "repeatString",
						"inherited metadata.default should stringify to source CFML"
					);
				});

			});

			describe( "duplicate() — deep-copy semantic, no re-fire (option 6 contract)", function(){
				// Core duplicate is a deep copy of state, not a re-construction. Hibernate (which
				// needs fresh per-entity evaluation) handles its template→entity re-firing at the
				// extension layer. See test/functions/Duplicate.cfc for the broader user-mutation
				// preservation contract.

				it( "deterministic expression-form value round-trips through duplicate as deep copy", function(){
					var src = new LDEV6303.ExpressionDefaultsCfc();
					var dup = duplicate( src );
					expect( dup.getExpressionDef() ).toBe( "xxxxx" );
					expect( dup.getExpressionDef() ).toBe( src.getExpressionDef() );
				});

				it( "deep duplicate gives dup its own array — mutating dup does not leak to src", function(){
					// Even without re-fire, deep duplicate must give dup a fresh array reference
					// (Duplicator.duplicate handles container deep-copy).
					var src = new LDEV6303.ExpressionDefaultsCfc();
					var dup = duplicate( src );

					arrayAppend( dup.getFreshArrayDef(), "added-to-dup" );

					expect( arrayLen( src.getFreshArrayDef() ) ).toBe( 0,
						"src's array must be unaffected by mutation on dup's deep-copy array"
					);
					expect( arrayLen( dup.getFreshArrayDef() ) ).toBe( 1 );
				});

				it( "explicit assignment after duplicate wins over the deep-copied value", function(){
					var src = new LDEV6303.ExpressionDefaultsCfc();
					var dup = duplicate( src );

					var freshTs = now();
					dup.setNowDef( freshTs );

					expect( dateCompare( dup.getNowDef(), freshTs, "s" ) ).toBe( 0 );
					expect( dateCompare( src.getNowDef(), freshTs, "s" ) ).toBeLTE( 0,
						"src's nowDef must be unchanged by mutation on the duplicate"
					);
				});

			});

			describe( "serializeJson shape — programmatic discriminability for consumers", function(){

				it( "literal-form metadata.default serialises as a bare string in JSON", function(){
					var inst = new LDEV6303.ExpressionDefaultsCfc();
					var json = serializeJson( getMetadata( inst ) );
					var parsed = deserializeJson( json );
					var meta = findProperty( parsed.properties, "literalDef" );
					expect( meta.default ).toBe( "hello" );
					expect( isSimpleValue( meta.default ) ).toBeTrue(
						"literal-form must round-trip through JSON as a simple string"
					);
				});

				it( "expression-form metadata.default serialises as a struct with key 'expression' in JSON", function(){
					var inst = new LDEV6303.ExpressionDefaultsCfc();
					var json = serializeJson( getMetadata( inst ) );
					var parsed = deserializeJson( json );
					var meta = findProperty( parsed.properties, "expressionDef" );
					expect( isStruct( meta.default ) ).toBeTrue(
						"expression-form must round-trip through JSON as a struct (programmatically discriminable from a literal default)"
					);
					expect( structKeyExists( meta.default, "expression" ) ).toBeTrue(
						"the JSON struct must use the lowercase key 'expression'"
					);
					expect( meta.default.expression ).toBe( "repeatString( 'x', 5 )" );
				});

				it( "JSON for #now()# expression-form is a struct with the source CFML, not an evaluated DateTime", function(){
					var inst = new LDEV6303.ExpressionDefaultsCfc();
					var json = serializeJson( getMetadata( inst ) );
					var parsed = deserializeJson( json );
					var meta = findProperty( parsed.properties, "nowDef" );
					expect( isStruct( meta.default ) ).toBeTrue();
					expect( meta.default.expression ).toBe( "now()",
						"JSON for #now()# must carry the source string, not a frozen evaluated DateTime"
					);
				});

				it( "JSON for struct-literal expression-form preserves the source verbatim with embedded quotes escaped", function(){
					var inst = new LDEV6303.ExpressionDefaultsCfc();
					var json = serializeJson( getMetadata( inst ) );
					var parsed = deserializeJson( json );
					var meta = findProperty( parsed.properties, "nowStructDef" );
					expect( isStruct( meta.default ) ).toBeTrue();
					// The CFC declares default='#{"ts":now(),"n":1}#' so source is {"ts":now(),"n":1}
					expect( meta.default.expression ).toInclude( "now()" );
					expect( meta.default.expression ).toInclude( "ts" );
					expect( meta.default.expression ).toInclude( "n" );
				});

			});

			describe( "Castable behaviour — wrapper coerces to source string in CFML coercion paths", function(){

				it( "string concat coerces metadata.default to the source CFML", function(){
					var inst = new LDEV6303.ExpressionDefaultsCfc();
					var meta = findProperty( getMetadata( inst ).properties, "expressionDef" );
					var concat = meta.default & "";
					expect( concat ).toBe( "repeatString( 'x', 5 )",
						"string-concat with empty string must coerce wrapper to source via toString()"
					);
				});

				it( "len() of the wrapper returns the source string length", function(){
					var inst = new LDEV6303.ExpressionDefaultsCfc();
					var meta = findProperty( getMetadata( inst ).properties, "nowDef" );
					expect( len( meta.default ) ).toBe( len( "now()" ) );
				});

				it( "ucase()/lcase() coerce the wrapper through string casting", function(){
					var inst = new LDEV6303.ExpressionDefaultsCfc();
					var meta = findProperty( getMetadata( inst ).properties, "expressionDef" );
					expect( ucase( meta.default ) ).toBe( "REPEATSTRING( 'X', 5 )" );
				});

			});

			describe( "external serialisation — ExpressionDefault survives objectSave/objectLoad rehydration", function(){

				it( "objectSave + objectLoad round-trips an instance and metadata.default for expression-form is still an Expression wrapper", function(){
					var src = new LDEV6303.ExpressionDefaultsCfc();
					var bin = objectSave( src );
					var dup = objectLoad( bin );

					// instance scope values must round-trip (variables-scope per-instance evaluation)
					expect( dup.getExpressionDef() ).toBe( "xxxxx",
						"variables-scope evaluated value must survive objectSave/objectLoad"
					);

					// metadata.default for expression-form must still be the wrapper after rehydration
					var meta = findProperty( getMetadata( dup ).properties, "expressionDef" );
					expect( structKeyExists( meta, "default" ) ).toBeTrue();
					expect( isObject( meta.default ) ).toBeTrue(
						"rehydrated metadata.default for expression-form must still be an Expression wrapper, not flattened to a string"
					);
					expect( "" & meta.default ).toBe( "repeatString( 'x', 5 )",
						"rehydrated wrapper must stringify to the same source CFML"
					);
				});

				it( "objectSave + objectLoad preserves literal-form metadata.default as a simple string", function(){
					var src = new LDEV6303.ExpressionDefaultsCfc();
					var bin = objectSave( src );
					var dup = objectLoad( bin );

					var meta = findProperty( getMetadata( dup ).properties, "literalDef" );
					expect( meta.default ).toBe( "hello" );
					expect( isSimpleValue( meta.default ) ).toBeTrue();
				});

			});

		});
	}

	private struct function findProperty( required array properties, required string name ) {
		for ( var p in arguments.properties ) {
			if ( p.name == arguments.name ) return p;
		}
		throw "property [#arguments.name#] not found in metadata";
	}

}

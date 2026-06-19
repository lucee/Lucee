component extends="org.lucee.cfml.test.LuceeTestCase" labels="java,component" {

	// LDEV-6300 — structural invariants underpinning the LDEV-6298 v2 flyweight share.
	//
	// CFML can't observe these contracts directly. A violation breaks the share gate at
	// duplicateUTFMap / addUDFS without breaking dispatch, so functional bedrock under
	// test/general/Accessors.cfc wouldn't catch it. The original investigation
	// (setOwner-corrupts-shared-UDFGSProperty) used Java-reflection tripwire scripts; this
	// file ports the durable invariant probes into TestBox so future refactors that touch
	// UDFGSProperty / ComponentImpl write paths land against red tests, not silent perf
	// regressions in tests-orm bench runs.

	function run( testResults, testBox ) {

		describe( "LDEV-6300 / LDEV-6298 v2 — flyweight share invariants", function(){

			// Locks in current behaviour: PropertyFactory.createGetter allocates fresh UDFGetterProperty
			// per init via setProperty (LDEV-3335 static pool emitted but unconsumed; LDEV-6236 no-owner
			// share branch in addUDFS doesn't fire). If LDEV-3335 Option 2 ever revives the pool, this
			// spec flips and the assertion gets inverted.
			it( title="fresh same-class siblings get distinct UDFGSProperty Java instances — current contract, no static-pool consumption", body=function( currentSpec ){
				var A = new LDEV6300.Person();
				var B = new LDEV6300.Person();
				expect( idOf( probeUdf( A, "getName" ) ) ).notToBe( idOf( probeUdf( B, "getName" ) ) );
			});

			// Same locked behaviour for explicit-extends instantiation: ComponentLoader.searchComponent
			// allocates a fresh ComponentImpl for the base on every call, with its own PropertyFactory'd
			// accessors. LDEV-6300 phase 3 (share-base) is what would change this.
			it( title="explicit-extends subclass and bare base get distinct UDFGSProperty Java instances — current contract, no base-instance reuse", body=function( currentSpec ){
				var base = new LDEV6300.BasePerson();
				var sub = new LDEV6300.InheritedPerson();
				expect( idOf( probeUdf( base, "getName" ) ) ).notToBe( idOf( probeUdf( sub, "getName" ) ) );
				// child's own accessor still works — sanity check the fixture
				sub.setRole( "admin" );
				expect( sub.getRole() ).toBe( "admin" );
			});

			it( title="Duplicate(cfc) shares the UDFGSProperty Java instance with source — LDEV-6298 v2 contract", body=function( currentSpec ){
				var A = new LDEV6300.Person();
				A.setName( "alpha" );
				var D = duplicate( A );
				expect( idOf( probeUdf( A, "getName" ) ) ).toBe( idOf( probeUdf( D, "getName" ) ) );
				expect( D.getName() ).toBe( "alpha" );
			});

			it( title="shared flyweight srcComponent stable across sibling instantiation — fresh new() must not mutate the share", body=function( currentSpec ){
				var A = new LDEV6300.Person();
				var srcBefore = idOf( srcOf( probeUdf( A, "getName" ) ) );
				var B = new LDEV6300.Person();
				expect( idOf( srcOf( probeUdf( A, "getName" ) ) ) ).toBe( srcBefore );
			});

			it( title="shared flyweight srcComponent stable across Duplicate(cfc) — duplicate path must not mutate the share", body=function( currentSpec ){
				var A = new LDEV6300.Person();
				A.setName( "alpha" );
				var srcBefore = idOf( srcOf( probeUdf( A, "getName" ) ) );
				var D = duplicate( A );
				expect( idOf( srcOf( probeUdf( A, "getName" ) ) ) ).toBe( srcBefore );
			});

			it( title="shared flyweight srcComponent stable across sibling ObjectSave/ObjectLoad — original LDEV-6298 investigation contract", body=function( currentSpec ){
				var A = new LDEV6300.Person();
				A.setName( "alpha" );
				var B = new LDEV6300.Person();
				B.setName( "bravo" );
				var srcA = idOf( srcOf( probeUdf( A, "getName" ) ) );
				var srcB = idOf( srcOf( probeUdf( B, "getName" ) ) );

				var C = new LDEV6300.Person();
				C.setName( "charlie" );
				var D = ObjectLoad( ObjectSave( C ) );

				expect( idOf( srcOf( probeUdf( A, "getName" ) ) ) ).toBe( srcA );
				expect( idOf( srcOf( probeUdf( B, "getName" ) ) ) ).toBe( srcB );
				expect( D.getName() ).toBe( "charlie" );
				expect( A.getName() ).toBe( "alpha" );
				expect( B.getName() ).toBe( "bravo" );
			});

		});
	}

	// === Java-reflection helpers ===

	// _udfs is a private field on ComponentImpl; srcComponent is a private field declared on
	// UDFGSProperty. Both are needed to assert structural invariants that have no CFML surface.

	private any function probeUdf( required any cfc, required string key ) {
		var clazz = createObject( "java", "java.lang.Class" ).forName( "lucee.runtime.ComponentImpl" );
		var udfsField = clazz.getDeclaredField( "_udfs" );
		udfsField.setAccessible( true );
		var keyImpl = createObject( "java", "lucee.runtime.type.KeyImpl" ).init( arguments.key );
		return udfsField.get( arguments.cfc ).get( keyImpl );
	}

	private any function srcOf( required any udf ) {
		var clazz = createObject( "java", "java.lang.Class" ).forName( "lucee.runtime.type.UDFGSProperty" );
		var srcField = clazz.getDeclaredField( "srcComponent" );
		srcField.setAccessible( true );
		return srcField.get( arguments.udf );
	}

	private numeric function idOf( required any obj ) {
		return createObject( "java", "java.lang.System" ).identityHashCode( arguments.obj );
	}

}

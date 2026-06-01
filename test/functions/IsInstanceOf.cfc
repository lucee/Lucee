component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, textbox ) {

		describe( "isInstanceOf()", function() {

			describe( "java types", function() {

				it( title="matches by fully-qualified Java class name", body=function( currentSpec ) {
					expect( isInstanceOf( {}, "java.util.Map" ) ).toBeTrue();
					expect( isInstanceOf( "Lucee", "java.lang.String" ) ).toBeTrue();
					expect( isInstanceOf( "Lucee", "java.lang.Object" ) ).toBeTrue();
					expect( isInstanceOf( "Lucee", "java.lang.CharSequence" ) ).toBeTrue();
				});

				it( title="bare names from ClassUtil.checkPrimaryTypes shortlist resolve", body=function( currentSpec ) {
					// Lucee maps a hardcoded set of bare names to Java classes — Object,
					// String, Integer, Character, Numeric, Null, plus the primitives.
					// Other bare Java names (CharSequence, Map, Comparable...) won't resolve.
					expect( isInstanceOf( "Lucee", "Object" ) ).toBeTrue();
					expect( isInstanceOf( "Lucee", "String" ) ).toBeTrue();
				});

				it( title="bare Java names outside the shortlist return false", body=function( currentSpec ) {
					expect( isInstanceOf( "Lucee", "CharSequence" ) ).toBeFalse();
					expect( isInstanceOf( "Lucee", "Comparable" ) ).toBeFalse();
					expect( isInstanceOf( {}, "Map" ) ).toBeFalse();
					expect( isInstanceOf( {}, "HashMap" ) ).toBeFalse();
				});

				it( title="returns false for unrelated Java type", body=function( currentSpec ) {
					expect( isInstanceOf( "Lucee", "java.util.Map" ) ).toBeFalse();
				});

				it( title="returns false for non-existent Java type", body=function( currentSpec ) {
					expect( isInstanceOf( "java", "java.system.lang" ) ).toBeFalse();
				});

			});

			describe( "cfc — accepted type-string forms", function() {

				it( title="matches the bare classname (no dots)", body=function( currentSpec ) {
					// matches ComponentImpl._getName() — the last segment of callPath
					var c = new IsInstanceOf.Child();
					expect( isInstanceOf( c, "Child" ) ).toBeTrue();
				});

				it( title="bare classname is case-insensitive", body=function( currentSpec ) {
					var c = new IsInstanceOf.Child();
					expect( isInstanceOf( c, "child" ) ).toBeTrue();
					expect( isInstanceOf( c, "CHILD" ) ).toBeTrue();
				});

				it( title="matches the fully-qualified name reported by getMetaData", body=function( currentSpec ) {
					// matches ComponentImpl.properties.name — the path from the web root
					var c = new IsInstanceOf.Child();
					expect( isInstanceOf( c, getMetaData( c ).name ) ).toBeTrue();
				});

				it( title="matches the loading-context-relative path used at instantiation", body=function( currentSpec ) {
					// matches ComponentImpl.properties.callPath — what was passed to `new`
					var c = new IsInstanceOf.Child();
					expect( isInstanceOf( c, "IsInstanceOf.Child" ) ).toBeTrue();
				});

				it( title="matches the Component root", body=function( currentSpec ) {
					var c = new IsInstanceOf.Child();
					expect( isInstanceOf( c, "Component" ) ).toBeTrue();
				});

			});

			describe( "cfc — inheritance chain", function() {

				it( title="matches a parent classname (extends)", body=function( currentSpec ) {
					var c = new IsInstanceOf.Child();
					expect( isInstanceOf( c, "Base" ) ).toBeTrue();
					expect( isInstanceOf( c, getMetaData( c ).extends.name ) ).toBeTrue();
				});

				it( title="matches an interface declared on an ancestor", body=function( currentSpec ) {
					var c = new IsInstanceOf.Child();
					expect( isInstanceOf( c, "Iface" ) ).toBeTrue();
				});

			});

			describe( "cfc — duplicate preserves identity", function() {

				it( title="every accepted form still matches after duplicate", body=function( currentSpec ) {
					var d = duplicate( new IsInstanceOf.Child() );
					expect( isInstanceOf( d, "Child" ) ).toBeTrue();
					expect( isInstanceOf( d, "Base" ) ).toBeTrue();
					expect( isInstanceOf( d, "Iface" ) ).toBeTrue();
					expect( isInstanceOf( d, getMetaData( d ).name ) ).toBeTrue();
				});

			});

			describe( "cfc — negative cases", function() {

				it( title="rejects a path slice that's neither callPath nor full name nor bare", body=function( currentSpec ) {
					// `Bogus.Child` is not the loading path, not the FQ name, not the bare classname
					var c = new IsInstanceOf.Child();
					expect( isInstanceOf( c, "Bogus.Child" ) ).toBeFalse();
					expect( isInstanceOf( c, "Child.Bogus" ) ).toBeFalse();
				});

				it( title="rejects an unknown type name", body=function( currentSpec ) {
					var c = new IsInstanceOf.Child();
					expect( isInstanceOf( c, "DoesNotExist" ) ).toBeFalse();
				});

				it( title="rejects an unrelated java type", body=function( currentSpec ) {
					var c = new IsInstanceOf.Child();
					expect( isInstanceOf( c, "java.util.Map" ) ).toBeFalse();
				});

				it( title="cfc-style query against non-cfc value returns false", body=function( currentSpec ) {
					expect( isInstanceOf( "hello", "Child" ) ).toBeFalse();
					expect( isInstanceOf( {}, "Child" ) ).toBeFalse();
				});

			});

		});

	}

}

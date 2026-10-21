component extends="org.lucee.cfml.test.LuceeTestCase" labels="xml" {

	function run( testResults, testBox ) {
		describe( "LDEV-6122 XMLNodeStruct.equals() must compare, not assign", function() {

			it( "the same node is equal to itself when not case sensitive", function() {
				var doc = xmlParse( "<root><child/></root>", false );
				var a = doc.xmlRoot;
				var b = doc.xmlRoot;
				var objects = createObject( "java", "java.util.Objects" );
				expect( objects.equals( a, b ) ).toBeTrue();
			});

			it( "the same node is equal to itself when case sensitive", function() {
				var doc = xmlParse( "<root><child/></root>", true );
				var objects = createObject( "java", "java.util.Objects" );
				expect( objects.equals( doc.xmlRoot, doc.xmlRoot ) ).toBeTrue();
			});

			it( "equals() does not change the case sensitivity of the other node", function() {
				var ci = xmlParse( "<root><Child>x</Child></root>", false );
				var cs = xmlParse( "<root><Child>x</Child></root>", true );
				var objects = createObject( "java", "java.util.Objects" );
				var csRoot = cs.xmlRoot;
				objects.equals( ci.xmlRoot, csRoot );
				// csRoot must still be case sensitive, so the wrong case doesn't resolve
				expect( structKeyExists( csRoot, "child" ) ).toBeFalse();
				expect( structKeyExists( csRoot, "Child" ) ).toBeTrue();
			});
		});
	}
}

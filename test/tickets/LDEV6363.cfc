component extends="org.lucee.cfml.test.LuceeTestCase" labels="xml" {

	function run( testResults, testBox ) {

		describe( "LDEV-6363 XMLUtil.getProperty defaultValue must not throw on missing child", function() {

			variables.xml = xmlParse( "<root><present>here</present></root>" );

			describe( "structKeyExists semantics", function() {

				it( "returns false for a missing named child", function() {
					expect( structKeyExists( variables.xml.xmlRoot, "missing" ) ).toBeFalse();
				} );

				it( "returns true for a present named child", function() {
					expect( structKeyExists( variables.xml.xmlRoot, "present" ) ).toBeTrue();
				} );

				it( "returns true for the xmlChildren special key (no throw)", function() {
					expect( structKeyExists( variables.xml.xmlRoot, "xmlChildren" ) ).toBeTrue();
				} );

				it( "returns false on an empty Document for any key", function() {
					var emptyDoc = xmlNew();
					expect( structKeyExists( emptyDoc, "anything" ) ).toBeFalse();
				} );

			} );

			describe( "dot access — read path", function() {

				it( "throws with the canonical message for a missing named child", function() {
					var msg = "";
					try {
						var ignored = variables.xml.xmlRoot.missing;
					}
					catch( any e ) {
						msg = e.message;
					}
					expect( msg ).toBe( "Attribute [missing] not found" );
				} );

				it( "returns the child struct for a present named child", function() {
					expect( variables.xml.xmlRoot.present.xmlText ).toBe( "here" );
				} );

			} );

			describe( "bracket access — parity with dot", function() {

				it( "throws for a missing named child", function() {
					var msg = "";
					try {
						var ignored = variables.xml.xmlRoot[ "missing" ];
					}
					catch( any e ) {
						msg = e.message;
					}
					expect( msg ).toBe( "Attribute [missing] not found" );
				} );

				it( "returns the child struct for a present named child", function() {
					expect( variables.xml.xmlRoot[ "present" ].xmlText ).toBe( "here" );
				} );

			} );

			describe( "retained throws — message contract preserved by the patch", function() {

				it( "integer-index out of range throws with the existing OOB message", function() {
					var msg = "";
					try {
						var ignored = variables.xml.xmlRoot.present[ 99 ];
					}
					catch( any e ) {
						msg = e.message;
					}
					expect( msg ).toBe( "invalid index [99] for Element with name [present], there is only 1 Element with this name" );
				} );

			} );

		} );
	}
}

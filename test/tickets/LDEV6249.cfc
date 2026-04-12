component extends="org.lucee.cfml.test.LuceeTestCase" {

	function run( testResults, testBox ) {

		describe( "LDEV-6249 XML struct DOM methods must not use reflection on JDK internals", function() {

			describe( "XMLNodeStruct — getUserData, getTextContent, setUserData", function() {

				it( "getTextContent() works on XML element node", function() {
					var xml = xmlParse( "<root><child>hello</child></root>" );
					var result = xml.xmlRoot.xmlChildren[ 1 ].getTextContent();
					expect( result ).toBe( "hello" );
				} );

				it( "getTextContent() returns null on document node per DOM spec", function() {
					var xml = xmlParse( "<root>hello</root>" );
					var result = xml.getTextContent();
					expect( isNull( result ) ).toBeTrue();
				} );

				it( "getUserData() returns null for unset key", function() {
					var xml = xmlParse( "<root/>" );
					var result = xml.xmlRoot.getUserData( "noSuchKey" );
					expect( isNull( result ) ).toBeTrue();
				} );

				it( "setUserData() roundtrips with null handler", function() {
					var xml = xmlParse( "<root/>" );
					xml.xmlRoot.setUserData( "testKey", "testValue", javaCast( "null", "" ) );
					var result = xml.xmlRoot.getUserData( "testKey" );
					expect( result ).toBe( "testValue" );
				} );

			} );

			describe( "XMLDocumentStruct — Document-level DOM Level 3 methods", function() {

				it( "getDocumentURI() does not throw", function() {
					var xml = xmlParse( "<root/>" );
					xml.getDocumentURI();
				} );

				it( "setDocumentURI() and getDocumentURI() roundtrip", function() {
					var xml = xmlParse( "<root/>" );
					xml.setDocumentURI( "http://example.com/test.xml" );
					expect( xml.getDocumentURI() ).toBe( "http://example.com/test.xml" );
				} );

				it( "getDomConfig() returns a DOMConfiguration", function() {
					var xml = xmlParse( "<root/>" );
					var config = xml.getDomConfig();
					expect( isNull( config ) ).toBeFalse();
				} );

				it( "getInputEncoding() does not throw", function() {
					var xml = xmlParse( "<root/>" );
					xml.getInputEncoding();
				} );

				it( "getXmlVersion() returns 1.0", function() {
					var xml = xmlParse( "<root/>" );
					expect( xml.getXmlVersion() ).toBe( "1.0" );
				} );

				it( "setXmlVersion() roundtrips", function() {
					var xml = xmlParse( "<root/>" );
					xml.setXmlVersion( "1.1" );
					expect( xml.getXmlVersion() ).toBe( "1.1" );
				} );

				it( "getXmlStandalone() returns false by default", function() {
					var xml = xmlParse( "<root/>" );
					expect( xml.getXmlStandalone() ).toBe( false );
				} );

				it( "setXmlStandalone() roundtrips", function() {
					var xml = xmlParse( "<root/>" );
					xml.setXmlStandalone( true );
					expect( xml.getXmlStandalone() ).toBe( true );
				} );

				it( "getStrictErrorChecking() returns true by default", function() {
					var xml = xmlParse( "<root/>" );
					expect( xml.getStrictErrorChecking() ).toBe( true );
				} );

				it( "setStrictErrorChecking() roundtrips", function() {
					var xml = xmlParse( "<root/>" );
					xml.setStrictErrorChecking( false );
					expect( xml.getStrictErrorChecking() ).toBe( false );
				} );

				it( "getXmlEncoding() does not throw", function() {
					var xml = xmlParse( "<root/>" );
					xml.getXmlEncoding();
				} );

				it( "normalizeDocument() does not throw", function() {
					var xml = xmlParse( "<root>  <child/>  </root>" );
					xml.normalizeDocument();
				} );

				it( "renameNode() renames an element", function() {
					var xml = xmlParse( "<root><old>data</old></root>" );
					// must unwrap to the underlying DOM node — Xerces casts to its own ElementImpl
					var child = xml.xmlRoot.xmlChildren[ 1 ].toNode();
					xml.renameNode( child, "", "new" );
					expect( child.getNodeName() ).toBe( "new" );
				} );

			} );

			describe( "XMLElementStruct — Element-level DOM Level 3 methods", function() {

				it( "getSchemaTypeInfo() does not throw", function() {
					var xml = xmlParse( "<root/>" );
					var info = xml.xmlRoot.getSchemaTypeInfo();
					expect( isNull( info ) ).toBeFalse();
				} );

				it( "setIdAttribute() does not throw", function() {
					var xml = xmlParse( '<root id="123"/>' );
					xml.xmlRoot.setIdAttribute( "id", true );
				} );

				it( "setIdAttributeNS() does not throw", function() {
					var xml = xmlParse( '<root xmlns:t="http://test" t:id="123"/>' );
					xml.xmlRoot.setIdAttributeNS( "http://test", "id", true );
				} );

			} );

			describe( "XMLCDATASectionStruct — Text-level DOM Level 3 methods", function() {

				// NOTE: XMLStructFactory checks (node instanceof Text) before
				// (node instanceof CDATASection), so CDATA sections parsed from XML
				// get wrapped as XMLTextStruct, not XMLCDATASectionStruct.
				// We construct XMLCDATASectionStruct directly via Java to test it.

				it( "getWholeText() on CDATA section", function() {
					var xml = xmlParse( "<root/>" );
					var cdataNode = xml.createCDATASection( "hello world" );
					var cdata = createObject( "java", "lucee.runtime.text.xml.struct.XMLCDATASectionStruct" )
						.init( cdataNode, javaCast( "boolean", true ) );
					expect( cdata.getWholeText() ).toBe( "hello world" );
				} );

				it( "isElementContentWhitespace() on CDATA section", function() {
					var xml = xmlParse( "<root/>" );
					var cdataNode = xml.createCDATASection( "hello" );
					var cdata = createObject( "java", "lucee.runtime.text.xml.struct.XMLCDATASectionStruct" )
						.init( cdataNode, javaCast( "boolean", true ) );
					expect( cdata.isElementContentWhitespace() ).toBe( false );
				} );

				it( "replaceWholeText() on CDATA section", function() {
					var xml = xmlParse( "<root/>" );
					var cdataNode = xml.createCDATASection( "old" );
					xml.xmlRoot.appendChild( cdataNode );
					var cdata = createObject( "java", "lucee.runtime.text.xml.struct.XMLCDATASectionStruct" )
						.init( cdataNode, javaCast( "boolean", true ) );
					cdata.replaceWholeText( "new" );
					expect( cdataNode.getWholeText() ).toBe( "new" );
				} );

			} );

		} );
	}

}

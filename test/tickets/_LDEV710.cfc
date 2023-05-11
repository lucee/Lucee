component extends="org.lucee.cfml.test.LuceeTestCase" labels="xml" {
	function run( testResults , testBox ) {
		describe( "Test Case for LDEV710", function() {
				var xmlOrig = fileRead(expandPath("./LDEV710/xmlOrig.xml"));
				var doc = xmlParse(xmlOrig);
				var xmlCopy = toString( doc );
			it( title='Checking XmlText with XML Node', body=function( currentSpec ) {
				if(Len( doc.xmlNodes[1].XmlText) > 0) {
					result1 = "true";
				} else{
					result1 = "false";
				}
				expect(result1).toBe('true');
			});
			it( title='Checking XmlText with XML Element', body=function( currentSpec ) {
				if(Len( doc.xmlNodes[2].XmlText) > 0) {
					result2 = "true";
				} else{
					result2 = "false";
				}
				expect(result2).toBe('true');
			});
			it( title='Checking XmlAttributes with XML Node', body=function( currentSpec ) {
				if(structKeyExists(doc.xmlNodes[1],'XmlAttributes')) {
					result3 = "true";
				} else{
					result3 = "false";
				}
				expect(result3).toBe('true');
			});
			it( title='Checking XmlAttributes with XML Element', body=function( currentSpec ) {
				if(structKeyExists(doc.xmlNodes[2],'XmlAttributes')) {
					result4 = "true";
				} else{
					result4 = "false";
				}
				expect(result4).toBe('true');
			});
		});
	}
}
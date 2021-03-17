component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults, testBox ){
		describe( "Test case for LDEV-3353", function() {
            xmlObject = xmlParse("<office><employee>Test Employee</employee></office>");
            it(title="Validate Xml Object", body=function( currentSpec ){
                expect(isXMLDoc(xmlObject)).toBe(true);
                expect(structKeyExists(xmlObject,"xmlroot")).toBe(true);
            });
            it(title="Extract xmlRoot from the xmlObject", body=function( currentSpec ){
                expect(serializeJSON(xmlObject.xmlroot)).toBe('"<?xml version=\"1.0\" encoding=\"UTF-8\"?><office><employee>test Employee</employee></office>"');
            });
            it(title="Validate XmlRoot object", body=function( currentSpec ){
                expect(isXMLDoc(xmlObject.xmlRoot)).toBe(false);
                expect(structKeyExists(xmlObject.xmlRoot,"xmlroot")).toBe(false);
            });
            it(title="Extract xmlRoot from the xmlRoot of xmlObject", body=function( currentSpec ){
                try{
                    res = serializeJSON(xmlObject.xmlRoot.xmlRoot);
                }
                catch(any e){
                    res = e.message;
                }
                expect(res).toBe('Attribute [XMLROOT] not found');
            });
        });
    }
}
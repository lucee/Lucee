component extends="org.lucee.cfml.test.LuceeTestCase"{
	function run( testResults, testBox ){
		describe( "Test case for LDEV-3387", function() {
            path = getDirectoryFromPath(getCurrentTemplatePath());
            it(title="xmlTransform() with simple xsl file", body=function(){
                try{
                    hasError = false;
                    xsl_String = fileread("#path#LDEV3387/xml-xsl.xsl");
                    xml_String = '<?xml version="1.0" encoding="UTF-8"?><notes><note><to>Alice</to><from>Bob</from></note><note><to>note</to><from>parse</from></note></notes>';
                    res = xmlTransform(xml_String,xsl_String);
                }
                catch(any e){
                    hasError = e.message;
                }
                expect(hasError).toBe(false);
                expect(isValid("string",res)).toBe(true);
            });
            it(title="xmlTransform() with xml-to-json.xsl file", body=function(){
                try{
                    hasError = false;
                    xsl_String = fileread("#path#LDEV3387/xml-to-json.xsl");
                    xml_String = '<?xml version="1.0" encoding="UTF-8"?><notes><note><to>Alice</to><from>Bob</from></note><note><to>note</to><from>parse</from></note></notes>';
                    res = xmlTransform(xml_String,xsl_String);
                }
                catch(any e){
                    hasError = e.message;
                }
                expect(hasError).toBe(false);
                expect(isValid("string",res)).toBe(true);
            });
        });
    }
}
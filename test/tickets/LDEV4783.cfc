component extends = "org.lucee.cfml.test.LuceeTestCase" {

    function run( testResults, testBox ){
        describe( "Testcase for LDEV-4783", function(){
            it( title="check for 'xml' returnFormat", body=function( currentSpec ) {
            	var meta = getMetadata(this.returnXML);
            	expect(structKeyExists(meta, "returnFormat")).toBe(true);
            	expect(meta.returnFormat).toBe("xml");
            });
        });
    }

    remote function returnXML() returnFormat="xml"{
    	return '<root><item>testing</item></root>';
    }

}
